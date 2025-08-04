#!/usr/bin/env python3
"""
Serviço de Detecção de Mãos usando OpenCV (versão alternativa)
Comunica com o sistema Java via HTTP REST API
"""
import cv2
import numpy as np
import json
import time
import threading
import requests
from flask import Flask, jsonify, request
import logging

# Configuração de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class HandDetectionService:
    def __init__(self, camera_index=0, fps=30):
        self.camera_index = camera_index
        self.fps = fps
        self.is_running = False
        self.camera = None
        self.thread = None
        
        # Configuração da API REST
        self.java_api_url = "http://localhost:8082/api/hand-detection"
        
        # Histórico para detecção de gestos
        self.gesture_history = []
        self.last_gesture = "NONE"
        self.gesture_confidence = 0.0
        
        logger.info("Servico de deteccao de maos Python (OpenCV) inicializado")

    def start_camera(self):
        """Inicia a câmera com múltiplas tentativas"""
        try:
            # Lista de backends para tentar (Windows)
            backends = [
                cv2.CAP_DSHOW,  # DirectShow (mais confiável no Windows)
                cv2.CAP_MSMF,   # Media Foundation
                cv2.CAP_ANY     # Qualquer backend disponível
            ]
            
            # Tenta diferentes índices de câmera
            camera_indices = [0, 1, 2]
            
            for camera_index in camera_indices:
                logger.info(f"Tentando camera no indice {camera_index}")
                
                for backend in backends:
                    try:
                        logger.info(f"Tentando backend: {backend}")
                        
                        # Libera câmera anterior se existir
                        if self.camera is not None:
                            self.camera.release()
                        
                        # Cria nova captura com backend específico
                        self.camera = cv2.VideoCapture(camera_index, backend)
                        
                        if not self.camera.isOpened():
                            logger.warning(f"Backend {backend} falhou para indice {camera_index}")
                            continue
                        
                        # Configura propriedades
                        self.camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
                        self.camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
                        self.camera.set(cv2.CAP_PROP_FPS, self.fps)
                        self.camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)
                        
                        # Testa se consegue capturar um frame
                        ret, test_frame = self.camera.read()
                        if ret and test_frame is not None:
                            width = int(self.camera.get(cv2.CAP_PROP_FRAME_WIDTH))
                            height = int(self.camera.get(cv2.CAP_PROP_FRAME_HEIGHT))
                            fps_actual = self.camera.get(cv2.CAP_PROP_FPS)
                            
                            logger.info(f"✅ Camera funcionando! Indice: {camera_index}, Backend: {backend}")
                            logger.info(f"Resolucao: {width}x{height}, FPS: {fps_actual}")
                            
                            self.camera_index = camera_index
                            return True
                        else:
                            logger.warning(f"Backend {backend} nao conseguiu capturar frame")
                            self.camera.release()
                            self.camera = None
                            
                    except Exception as e:
                        logger.warning(f"Erro com backend {backend}: {e}")
                        if self.camera is not None:
                            self.camera.release()
                            self.camera = None
                        continue
            
            # Se chegou aqui, nenhuma combinação funcionou
            logger.error("❌ NENHUMA CAMERA ENCONTRADA!")
            logger.error("💡 Soluções:")
            logger.error("   1. Verifique se a webcam está conectada")
            logger.error("   2. Teste o app Câmera do Windows")
            logger.error("   3. Reinicie o computador")
            logger.error("   4. Execute como Administrador")
            return False
            
        except Exception as e:
            logger.error(f"Erro ao iniciar camera: {e}")
            return False

    def detect_gesture(self, landmarks, frame):
        """Detecta gestos específicos baseado nos landmarks"""
        if not landmarks or len(landmarks) == 0:
            return "NONE", 0.0
        
        try:
            # Pega o primeiro conjunto de landmarks (primeira mão)
            hand_landmarks = landmarks[0]
            
            if len(hand_landmarks) < 21:
                return "NONE", 0.0
            
            # Extrai pontos importantes dos dedos
            thumb_tip = hand_landmarks[4]  # Polegar ponta
            index_tip = hand_landmarks[8]  # Indicador ponta
            middle_tip = hand_landmarks[12]  # Médio ponta
            ring_tip = hand_landmarks[16]  # Anelar ponta
            pinky_tip = hand_landmarks[20]  # Mínimo ponta
            
            # Pontos base dos dedos
            thumb_base = hand_landmarks[2]  # Polegar base
            index_base = hand_landmarks[5]  # Indicador base
            middle_base = hand_landmarks[9]  # Médio base
            ring_base = hand_landmarks[13]  # Anelar base
            pinky_base = hand_landmarks[17]  # Mínimo base
            
            # Calcula distâncias para determinar se dedos estão estendidos
            def is_finger_extended(tip, base):
                return tip['y'] < base['y'] - 0.05  # Dedos estendidos para cima
            
            # Conta dedos estendidos
            extended_fingers = 0
            if is_finger_extended(thumb_tip, thumb_base): extended_fingers += 1
            if is_finger_extended(index_tip, index_base): extended_fingers += 1
            if is_finger_extended(middle_tip, middle_base): extended_fingers += 1
            if is_finger_extended(ring_tip, ring_base): extended_fingers += 1
            if is_finger_extended(pinky_tip, pinky_base): extended_fingers += 1
            
            # Detecta gestos baseado no número de dedos estendidos
            gesture = "NONE"
            confidence = 0.0
            
            if extended_fingers == 1 and is_finger_extended(index_tip, index_base):
                gesture = "CLICK"
                confidence = 0.8
            elif extended_fingers == 2 and is_finger_extended(index_tip, index_base) and is_finger_extended(middle_tip, middle_base):
                gesture = "RIGHT_CLICK"
                confidence = 0.8
            elif extended_fingers == 0:
                gesture = "DRAG"
                confidence = 0.7
            elif extended_fingers == 3:
                gesture = "SCROLL"
                confidence = 0.6
            elif extended_fingers == 4:
                gesture = "DOUBLE_CLICK"
                confidence = 0.7
            else:
                gesture = "NONE"
                confidence = 0.3
            
            # Atualiza histórico de gestos
            self.gesture_history.append(gesture)
            if len(self.gesture_history) > 5:
                self.gesture_history.pop(0)
            
            # Verifica se o gesto é consistente
            if len(self.gesture_history) >= 3:
                most_common = max(set(self.gesture_history), key=self.gesture_history.count)
                if self.gesture_history.count(most_common) >= 2:
                    gesture = most_common
                    confidence += 0.1
            
            self.last_gesture = gesture
            self.gesture_confidence = min(1.0, confidence)
            
            return gesture, self.gesture_confidence
            
        except Exception as e:
            logger.error(f"Erro ao detectar gesto: {e}")
            return "NONE", 0.0

    def draw_hand_visualization(self, frame, landmarks, gesture):
        """Desenha visualização da mão detectada"""
        if not landmarks or len(landmarks) == 0:
            return frame
        
        try:
            hand_landmarks = landmarks[0]
            height, width = frame.shape[:2]
            
            # Desenha retângulo ao redor da mão
            x_coords = [int(landmark['x'] * width) for landmark in hand_landmarks]
            y_coords = [int(landmark['y'] * height) for landmark in hand_landmarks]
            
            if x_coords and y_coords:
                x_min, x_max = min(x_coords), max(x_coords)
                y_min, y_max = min(y_coords), max(y_coords)
                
                # Adiciona margem
                margin = 20
                x_min = max(0, x_min - margin)
                y_min = max(0, y_min - margin)
                x_max = min(width, x_max + margin)
                y_max = min(height, y_max + margin)
                
                # Cor baseada no gesto
                color_map = {
                    "CLICK": (0, 255, 0),      # Verde
                    "RIGHT_CLICK": (0, 0, 255), # Vermelho
                    "DOUBLE_CLICK": (255, 0, 0), # Azul
                    "DRAG": (255, 255, 0),      # Ciano
                    "SCROLL": (255, 0, 255),    # Magenta
                    "NONE": (128, 128, 128)     # Cinza
                }
                
                color = color_map.get(gesture, (128, 128, 128))
                
                # Desenha retângulo
                cv2.rectangle(frame, (x_min, y_min), (x_max, y_max), color, 2)
                
                # Desenha texto do gesto
                text = f"{gesture} ({self.gesture_confidence:.1f})"
                cv2.putText(frame, text, (x_min, y_min - 10), 
                           cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)
                
                # Desenha landmarks como pontos
                for landmark in hand_landmarks:
                    x = int(landmark['x'] * width)
                    y = int(landmark['y'] * height)
                    cv2.circle(frame, (x, y), 3, color, -1)
            
            return frame
            
        except Exception as e:
            logger.error(f"Erro ao desenhar visualização: {e}")
            return frame

    def detect_hands_opencv(self, frame):
        """Detecta mãos usando OpenCV"""
        try:
            # Converte para HSV
            hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
            
            # Define range para detecção de pele
            lower_skin = np.array([0, 20, 70], dtype=np.uint8)
            upper_skin = np.array([20, 255, 255], dtype=np.uint8)
            
            # Cria máscara para pele
            mask = cv2.inRange(hsv, lower_skin, upper_skin)
            
            # Aplica operações morfológicas
            kernel = np.ones((3,3), np.uint8)
            mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
            mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)
            
            # Encontra contornos
            contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            
            landmarks = []
            if contours:
                # Pega o maior contorno (provavelmente a mão)
                largest_contour = max(contours, key=cv2.contourArea)
                area = cv2.contourArea(largest_contour)
                
                if area > 5000:  # Filtra por área mínima
                    # Calcula o centro do contorno
                    M = cv2.moments(largest_contour)
                    if M["m00"] != 0:
                        cx = int(M["m10"] / M["m00"])
                        cy = int(M["m11"] / M["m00"])
                        
                        # Normaliza coordenadas
                        height, width = frame.shape[:2]
                        cx_norm = cx / width
                        cy_norm = cy / height
                        
                        # Gera landmarks simulados baseados no centro
                        hand_data = []
                        for i in range(21):  # 21 landmarks como MediaPipe
                            # Distribui landmarks ao redor do centro
                            angle = (i / 21) * 2 * np.pi
                            radius = 0.1 + (i % 5) * 0.02
                            x = cx_norm + radius * np.cos(angle)
                            y = cy_norm + radius * np.sin(angle)
                            
                            hand_data.append({
                                'x': max(0, min(1, x)),
                                'y': max(0, min(1, y)),
                                'z': 0.0,
                                'confidence': 0.7
                            })
                        
                        landmarks.append(hand_data)
            
            return landmarks
            
        except Exception as e:
            logger.error(f"Erro ao processar frame: {e}")
            return []

    def process_frame(self, frame):
        """Processa um frame e detecta mãos"""
        landmarks = self.detect_hands_opencv(frame)
        
        if landmarks:
            # Detecta gesto
            gesture, confidence = self.detect_gesture(landmarks, frame)
            
            # Desenha visualização
            frame = self.draw_hand_visualization(frame, landmarks, gesture)
            
            # Mostra frame processado (opcional)
            cv2.imshow('Hand Detection', frame)
            cv2.waitKey(1)
        
        return landmarks

    def send_to_java(self, landmarks):
        """Envia landmarks para o sistema Java"""
        try:
            data = {
                'timestamp': int(time.time() * 1000),
                'landmarks': landmarks,
                'hand_count': len(landmarks),
                'confidence': 0.7 if landmarks else 0.0,
                'gesture': self.last_gesture,
                'gesture_confidence': self.gesture_confidence
            }
            
            response = requests.post(
                self.java_api_url,
                json=data,
                headers={'Content-Type': 'application/json'},
                timeout=1
            )
            
            if response.status_code != 200:
                logger.warning(f"Erro ao enviar para Java: {response.status_code}")
                
        except requests.exceptions.RequestException as e:
            logger.debug(f"Erro de comunicacao com Java: {e}")
        except Exception as e:
            logger.error(f"Erro ao enviar dados: {e}")

    def detection_loop(self):
        """Loop principal de detecção"""
        logger.info("Iniciando loop de deteccao...")
        
        while self.is_running:
            try:
                if self.camera is None or not self.camera.isOpened():
                    logger.error("Camera nao esta disponivel")
                    break
                
                ret, frame = self.camera.read()
                if not ret:
                    logger.warning("Nao foi possivel capturar frame")
                    time.sleep(0.1)
                    continue
                
                # Processa frame e detecta mãos
                landmarks = self.process_frame(frame)
                
                # Envia para o sistema Java
                if landmarks:
                    self.send_to_java(landmarks)
                
                # Controla FPS
                time.sleep(1.0 / self.fps)
                
            except Exception as e:
                logger.error(f"Erro no loop de deteccao: {e}")
                time.sleep(1)

    def start(self):
        """Inicia o serviço de detecção"""
        if self.is_running:
            logger.warning("Servico ja esta rodando")
            return
            
        if not self.start_camera():
            logger.error("Falha ao iniciar camera")
            return
            
        self.is_running = True
        self.thread = threading.Thread(target=self.detection_loop)
        self.thread.daemon = True
        self.thread.start()
        
        logger.info("Servico de deteccao iniciado")

    def stop(self):
        """Para o serviço de detecção"""
        logger.info("Parando servico de deteccao...")
        
        self.is_running = False
        
        if self.thread:
            self.thread.join(timeout=5)
            
        if self.camera:
            self.camera.release()
        
        cv2.destroyAllWindows()
            
        logger.info("Servico de deteccao parado")

    def get_status(self):
        """Retorna status do serviço"""
        return {
            'running': self.is_running,
            'camera_opened': self.camera is not None and self.camera.isOpened(),
            'camera_index': self.camera_index,
            'fps': self.fps,
            'last_gesture': self.last_gesture,
            'gesture_confidence': self.gesture_confidence
        }

# API REST para controle do serviço
app = Flask(__name__)
hand_service = None

@app.route('/api/hand-detection/start', methods=['POST'])
def start_detection():
    """Inicia o serviço de detecção"""
    global hand_service
    
    try:
        data = request.get_json() or {}
        camera_index = data.get('camera_index', 0)
        fps = data.get('fps', 30)
        
        if hand_service is None:
            hand_service = HandDetectionService(camera_index, fps)
        
        hand_service.start()
        return jsonify({'status': 'success', 'message': 'Servico iniciado'})
        
    except Exception as e:
        logger.error(f"Erro ao iniciar servico: {e}")
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/api/hand-detection/stop', methods=['POST'])
def stop_detection():
    """Para o serviço de detecção"""
    global hand_service
    
    try:
        if hand_service:
            hand_service.stop()
        return jsonify({'status': 'success', 'message': 'Servico parado'})
        
    except Exception as e:
        logger.error(f"Erro ao parar servico: {e}")
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/api/hand-detection/status', methods=['GET'])
def get_status():
    """Retorna status do serviço"""
    global hand_service
    
    if hand_service:
        return jsonify(hand_service.get_status())
    else:
        return jsonify({'running': False, 'camera_opened': False})

@app.route('/api/hand-detection/health', methods=['GET'])
def health_check():
    """Health check"""
    return jsonify({'status': 'healthy', 'timestamp': int(time.time() * 1000)})

if __name__ == '__main__':
    logger.info("Iniciando servico de deteccao de maos Python (OpenCV)...")
    app.run(host='0.0.0.0', port=5000, debug=False) 