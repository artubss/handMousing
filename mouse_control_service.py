#!/usr/bin/env python3
"""
Serviço de Controle do Mouse via Detecção de Mãos usando MediaPipe
Controla o cursor do mouse baseado na posição da mão detectada com profundidade
"""
import cv2
import numpy as np
import time
import threading
import requests
from flask import Flask, jsonify, request
import logging
import pyautogui
import mediapipe as mp

# Configuração de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Desabilita o fail-safe do pyautogui
pyautogui.FAILSAFE = False

class MouseControlService:
    def __init__(self, camera_index=0, fps=30):
        self.camera_index = camera_index
        self.fps = fps
        self.is_running = False
        self.camera = None
        self.thread = None
        
        # Configuração da tela
        self.screen_width, self.screen_height = pyautogui.size()
        logger.info(f"Resolução da tela: {self.screen_width}x{self.screen_height}")
        
        # Configuração da câmera
        self.camera_width = 640
        self.camera_height = 480
        
        # Estado do mouse
        self.last_hand_position = None
        self.is_dragging = False
        self.drag_start_position = None
        self.last_gesture = "NONE"
        self.gesture_cooldown = 0.3  # 300ms entre gestos
        self.last_gesture_time = 0
        
        # Configurações de profundidade
        self.depth_threshold = 0.1  # Distância mínima para detecção
        self.last_depth = 0.0
        self.depth_smoothing = 0.8  # Suavização da profundidade
        self.movement_smoothing = 0.7  # Suavização do movimento
        
        # MediaPipe Hands - seguindo documentação oficial
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            model_complexity=1,
            min_detection_confidence=0.7,
            min_tracking_confidence=0.7,
            max_num_hands=1
        )
        self.mp_drawing = mp.solutions.drawing_utils
        self.mp_drawing_styles = mp.solutions.drawing_styles
        
        # Configuração da API REST
        self.java_api_url = "http://localhost:8082/api/hand-detection"
        
        logger.info("Serviço de controle do mouse inicializado com MediaPipe e profundidade")

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
                        self.camera.set(cv2.CAP_PROP_FRAME_WIDTH, self.camera_width)
                        self.camera.set(cv2.CAP_PROP_FRAME_HEIGHT, self.camera_height)
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
            
            logger.error("❌ NENHUMA CAMERA ENCONTRADA!")
            return False
            
        except Exception as e:
            logger.error(f"Erro ao iniciar camera: {e}")
            return False

    def detect_hand_landmarks(self, frame):
        """Detecta landmarks da mão usando MediaPipe com profundidade"""
        try:
            # Converte BGR para RGB (MediaPipe requer RGB)
            rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            
            # Para melhorar performance, marca a imagem como não gravável
            rgb_frame.flags.writeable = False
            results = self.hands.process(rgb_frame)
            rgb_frame.flags.writeable = True
            
            if results.multi_hand_landmarks:
                # Pega a primeira mão detectada
                hand_landmarks = results.multi_hand_landmarks[0]
                
                # Pega a ponta do dedo indicador (landmark 8) para posição
                index_finger_tip = hand_landmarks.landmark[8]
                
                # Pega o pulso (landmark 0) para profundidade
                wrist = hand_landmarks.landmark[0]
                
                # Converte coordenadas normalizadas para pixels da câmera
                x = int(index_finger_tip.x * self.camera_width)
                y = int(index_finger_tip.y * self.camera_height)
                
                # Normaliza coordenadas para controle do mouse
                x_norm = index_finger_tip.x
                y_norm = index_finger_tip.y
                
                # Calcula profundidade baseada na posição Z do pulso
                # MediaPipe retorna valores Z entre -1 (mais próximo) e 1 (mais distante)
                depth = wrist.z
                
                # Normaliza profundidade para 0-1 (0 = mais próximo, 1 = mais distante)
                depth_normalized = (depth + 1) / 2
                
                # Suaviza a profundidade
                self.last_depth = (self.depth_smoothing * self.last_depth + 
                                 (1 - self.depth_smoothing) * depth_normalized)
                
                # Detecta gestos baseado nos landmarks e profundidade
                gesture = self.detect_gesture_from_landmarks(hand_landmarks, self.last_depth)
                
                logger.info(f"✅ Mão detectada em ({x}, {y}) - Profundidade: {self.last_depth:.3f} - Gesto: {gesture}")
                
                return (x_norm, y_norm, gesture, hand_landmarks, (x, y), self.last_depth)
            else:
                logger.debug("❌ Nenhuma mão detectada")
            
            return None
            
        except Exception as e:
            logger.error(f"Erro ao detectar landmarks da mão: {e}")
            return None

    def detect_gesture_from_landmarks(self, landmarks, depth):
        """Detecta gestos baseado nos landmarks da mão e profundidade"""
        try:
            # Pontas dos dedos (landmarks MediaPipe)
            thumb_tip = landmarks.landmark[4]   # Polegar
            index_tip = landmarks.landmark[8]   # Indicador
            middle_tip = landmarks.landmark[12] # Médio
            ring_tip = landmarks.landmark[16]   # Anelar
            pinky_tip = landmarks.landmark[20]  # Mínimo
            
            # Pontas das juntas médias
            thumb_ip = landmarks.landmark[3]    # Polegar IP
            index_pip = landmarks.landmark[6]   # Indicador PIP
            middle_pip = landmarks.landmark[10] # Médio PIP
            ring_pip = landmarks.landmark[14]   # Anelar PIP
            pinky_pip = landmarks.landmark[18]  # Mínimo PIP
            
            # Verifica quais dedos estão estendidos
            fingers_extended = []
            
            # Polegar (verifica se está estendido horizontalmente)
            if thumb_tip.x > thumb_ip.x:
                fingers_extended.append(1)
            else:
                fingers_extended.append(0)
            
            # Outros dedos (verifica se estão estendidos verticalmente)
            for tip, pip in [(index_tip, index_pip), (middle_tip, middle_pip), 
                            (ring_tip, ring_pip), (pinky_tip, pinky_pip)]:
                if tip.y < pip.y:  # Se a ponta está acima da junta, está estendido
                    fingers_extended.append(1)
                else:
                    fingers_extended.append(0)
            
            # Detecta gestos baseado no número de dedos estendidos e profundidade
            extended_count = sum(fingers_extended)
            
            # Log para debug
            logger.info(f"🤚 Dedos estendidos: {fingers_extended}, Total: {extended_count}, Profundidade: {depth:.3f}")
            
            # GESTOS BASEADOS EM PROFUNDIDADE E DEDOS:
            
            # CLICK: Aproximação da tela (profundidade alta) + apenas indicador
            if depth < 0.7 and extended_count == 1 and fingers_extended[1] == 1:
                logger.info("🎯 Gesto detectado: CLICK (aproximação + apenas indicador)")
                return "CLICK"
            
            # DOUBLE_CLICK: Aproximação da tela + indicador e médio
            elif depth < 0.7 and extended_count == 2 and fingers_extended[1] == 1 and fingers_extended[2] == 1:
                logger.info("🎯 Gesto detectado: DOUBLE_CLICK (aproximação + indicador e médio)")
                return "DOUBLE_CLICK"
            
            # RIGHT_CLICK: Aproximação da tela + indicador, médio e anelar
            elif depth < 0.7 and extended_count == 3 and fingers_extended[1] == 1 and fingers_extended[2] == 1 and fingers_extended[3] == 1:
                logger.info("🎯 Gesto detectado: RIGHT_CLICK (aproximação + indicador, médio e anelar)")
                return "RIGHT_CLICK"
            
            # SCROLL: 4 dedos estendidos (indicador, médio, anelar, mínimo) - qualquer profundidade
            elif extended_count == 4 and fingers_extended[1] == 1 and fingers_extended[2] == 1 and fingers_extended[3] == 1 and fingers_extended[4] == 1:
                logger.info("🎯 Gesto detectado: SCROLL (4 dedos - indicador, médio, anelar, mínimo)")
                return "SCROLL"
            
            # DRAG: Mão fechada (qualquer profundidade)
            elif extended_count == 0:
                logger.info("🎯 Gesto detectado: DRAG (mão fechada)")
                return "DRAG"
            
            else:
                return "NONE"
                
        except Exception as e:
            logger.error(f"Erro ao detectar gesto: {e}")
            return "NONE"

    def move_mouse(self, hand_position):
        """Move o mouse baseado na posição da mão com suavização"""
        if not hand_position:
            return
        
        try:
            x_norm, y_norm, gesture, landmarks, (x, y), depth = hand_position
            
            # Inverte o eixo X para corrigir a direção do movimento
            # Quando a mão vai para direita, o mouse deve ir para direita
            x_norm = 1.0 - x_norm
            
            # Converte coordenadas normalizadas para coordenadas da tela
            screen_x = int(x_norm * self.screen_width)
            screen_y = int(y_norm * self.screen_height)
            
            # Limita às dimensões da tela
            screen_x = max(0, min(self.screen_width - 1, screen_x))
            screen_y = max(0, min(self.screen_height - 1, screen_y))
            
            # Suaviza o movimento se há posição anterior
            if self.last_hand_position:
                last_x, last_y = self.last_hand_position
                screen_x = int(self.movement_smoothing * last_x + (1 - self.movement_smoothing) * screen_x)
                screen_y = int(self.movement_smoothing * last_y + (1 - self.movement_smoothing) * screen_y)
            
            # Move o mouse suavemente
            pyautogui.moveTo(screen_x, screen_y, duration=0.05)
            
            self.last_hand_position = (screen_x, screen_y)
            
            # Executa ações baseado no gesto
            self.execute_mouse_action(gesture, depth)
            
            logger.debug(f"Mouse movido para ({screen_x}, {screen_y}) - Gesto: {gesture} - Profundidade: {depth:.3f}")
            
        except Exception as e:
            logger.error(f"Erro ao mover mouse: {e}")

    def execute_mouse_action(self, gesture, depth):
        """Executa ações do mouse baseado no gesto e profundidade"""
        current_time = time.time()
        if current_time - self.last_gesture_time < self.gesture_cooldown:
            return
        
        try:
            if gesture == "CLICK" and self.last_gesture != "CLICK":
                pyautogui.click()
                logger.info("🖱️ Click executado")
                self.last_gesture_time = current_time
                self.is_dragging = False
                
            elif gesture == "RIGHT_CLICK" and self.last_gesture != "RIGHT_CLICK":
                pyautogui.rightClick()
                logger.info("🖱️ Right Click executado")
                self.last_gesture_time = current_time
                self.is_dragging = False
                
            elif gesture == "DOUBLE_CLICK" and self.last_gesture != "DOUBLE_CLICK":
                pyautogui.doubleClick()
                logger.info("🖱️ Double Click executado")
                self.last_gesture_time = current_time
                self.is_dragging = False
                
            elif gesture == "DRAG":
                if not self.is_dragging:
                    # Inicia drag
                    self.is_dragging = True
                    self.drag_start_position = pyautogui.position()
                    pyautogui.mouseDown()
                    logger.info("🖱️ Drag iniciado")
                # Continua drag (não precisa fazer nada)
                self.last_gesture_time = current_time
                
            elif gesture == "SCROLL":
                # Scroll baseado na posição Y da mão (como tablet/celular)
                # Quanto mais para cima da tela, mais scroll para cima
                # Quanto mais para baixo da tela, mais scroll para baixo
                scroll_amount = int((0.5 - y_norm) * 15)  # -15 a +15
                pyautogui.scroll(scroll_amount)
                logger.info(f"🖱️ Scroll executado: {scroll_amount}")
                self.last_gesture_time = current_time
                self.is_dragging = False
            
            # Para drag quando gesto muda
            if self.last_gesture == "DRAG" and gesture != "DRAG" and self.is_dragging:
                self.is_dragging = False
                pyautogui.mouseUp()
                logger.info("🖱️ Drag finalizado (gesto mudou)")
            
            self.last_gesture = gesture
            
        except Exception as e:
            logger.error(f"Erro ao executar ação do mouse: {e}")

    def draw_visualization(self, frame, hand_position):
        """Desenha visualização na câmera usando MediaPipe"""
        if not hand_position:
            return frame
        
        try:
            x_norm, y_norm, gesture, landmarks, (x, y), depth = hand_position
            
            # Desenha landmarks da mão usando MediaPipe
            self.mp_drawing.draw_landmarks(
                frame,
                landmarks,
                self.mp_hands.HAND_CONNECTIONS,
                self.mp_drawing_styles.get_default_hand_landmarks_style(),
                self.mp_drawing_styles.get_default_hand_connections_style()
            )
            
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
            
            # Desenha círculo na ponta do dedo indicador
            cv2.circle(frame, (x, y), 10, color, 2)
            
            # Desenha texto do gesto
            text = f"{gesture}"
            cv2.putText(frame, text, (x - 30, y - 20), 
                       cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)
            
            # Desenha informações de profundidade
            depth_text = f"Profundidade: {depth:.3f}"
            cv2.putText(frame, depth_text, (10, 60), 
                       cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
            
            # Desenha coordenadas da tela
            if self.last_hand_position:
                screen_x, screen_y = self.last_hand_position
                screen_text = f"Screen: ({screen_x}, {screen_y})"
                cv2.putText(frame, screen_text, (10, 30), 
                           cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
            
            return frame
            
        except Exception as e:
            logger.error(f"Erro ao desenhar visualização: {e}")
            return frame

    def process_frame(self, frame):
        """Processa um frame completo"""
        # Detecta landmarks da mão
        hand_position = self.detect_hand_landmarks(frame)
        
        if hand_position:
            # Move o mouse
            self.move_mouse(hand_position)
            
            # Desenha visualização
            frame = self.draw_visualization(frame, hand_position)
            
            # Mostra frame processado
            cv2.imshow('Mouse Control - MediaPipe', frame)
            cv2.waitKey(1)
        else:
            # Para drag se mão não detectada
            if self.is_dragging:
                self.is_dragging = False
                pyautogui.mouseUp()
                logger.info("🖱️ Drag finalizado (mão não detectada)")
            self.last_gesture = "NONE"
            cv2.imshow('Mouse Control - MediaPipe', frame)
            cv2.waitKey(1)
        
        return hand_position is not None

    def detection_loop(self):
        """Loop principal de detecção"""
        logger.info("Iniciando loop de controle do mouse com MediaPipe e profundidade...")
        
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
                
                # Processa frame
                hand_detected = self.process_frame(frame)
                
                # Controla FPS
                time.sleep(1.0 / self.fps)
                
            except Exception as e:
                logger.error(f"Erro no loop de deteccao: {e}")
                time.sleep(1)

    def start(self):
        """Inicia o serviço de controle do mouse"""
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
        
        logger.info("Servico de controle do mouse iniciado com MediaPipe e profundidade")

    def stop(self):
        """Para o serviço de controle do mouse"""
        logger.info("Parando servico de controle do mouse...")
        
        self.is_running = False
        
        if self.thread:
            self.thread.join(timeout=5)
            
        if self.camera:
            self.camera.release()
        
        if self.hands:
            self.hands.close()
        
        cv2.destroyAllWindows()
            
        logger.info("Servico de controle do mouse parado")

    def get_status(self):
        """Retorna status do serviço"""
        return {
            'running': self.is_running,
            'camera_opened': self.camera is not None and self.camera.isOpened(),
            'camera_index': self.camera_index,
            'fps': self.fps,
            'last_gesture': self.last_gesture,
            'mouse_position': self.last_hand_position,
            'is_dragging': self.is_dragging,
            'depth': self.last_depth
        }

# API REST para controle do serviço
app = Flask(__name__)
mouse_service = None

@app.route('/api/mouse-control/start', methods=['POST'])
def start_mouse_control():
    """Inicia o serviço de controle do mouse"""
    global mouse_service
    
    try:
        data = request.get_json() or {}
        camera_index = data.get('camera_index', 0)
        fps = data.get('fps', 30)
        
        if mouse_service is None:
            mouse_service = MouseControlService(camera_index, fps)
        
        mouse_service.start()
        return jsonify({'status': 'success', 'message': 'Controle do mouse iniciado com MediaPipe e profundidade'})
        
    except Exception as e:
        logger.error(f"Erro ao iniciar controle do mouse: {e}")
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/api/mouse-control/stop', methods=['POST'])
def stop_mouse_control():
    """Para o serviço de controle do mouse"""
    global mouse_service
    
    try:
        if mouse_service:
            mouse_service.stop()
        return jsonify({'status': 'success', 'message': 'Controle do mouse parado'})
        
    except Exception as e:
        logger.error(f"Erro ao parar controle do mouse: {e}")
        return jsonify({'status': 'error', 'message': str(e)}), 500

@app.route('/api/mouse-control/status', methods=['GET'])
def get_mouse_control_status():
    """Retorna status do serviço"""
    global mouse_service
    
    if mouse_service:
        return jsonify(mouse_service.get_status())
    else:
        return jsonify({'running': False, 'camera_opened': False})

@app.route('/api/mouse-control/health', methods=['GET'])
def mouse_control_health_check():
    """Health check"""
    return jsonify({'status': 'healthy', 'timestamp': int(time.time() * 1000)})

if __name__ == '__main__':
    logger.info("Iniciando servico de controle do mouse com MediaPipe e profundidade...")
    app.run(host='0.0.0.0', port=5001, debug=False) 