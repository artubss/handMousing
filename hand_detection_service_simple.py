#!/usr/bin/env python3
"""
Serviço de Detecção de Mãos Simulado (versão de teste)
Comunica com o sistema Java via HTTP REST API
"""

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
        self.thread = None
        
        # Configuração da API REST
        self.java_api_url = "http://localhost:8082/api/hand-detection"
        
        logger.info("Servico de deteccao de maos Python (simulado) inicializado")

    def detect_hands_simulated(self):
        """Simula detecção de mãos"""
        try:
            # Simula detecção de mão no centro da tela
            landmarks = []
            hand_data = []
            
            # Gera 21 landmarks simulados no centro
            for i in range(21):
                hand_data.append({
                    'x': 0.5 + (i % 5) * 0.01,  # Centro + variação
                    'y': 0.5 + (i // 5) * 0.01,  # Centro + variação
                    'z': 0.0,
                    'confidence': 0.8
                })
            
            landmarks.append(hand_data)
            return landmarks
            
        except Exception as e:
            logger.error(f"Erro ao simular deteccao: {e}")
            return []

    def send_to_java(self, landmarks):
        """Envia landmarks para o sistema Java"""
        try:
            data = {
                'timestamp': int(time.time() * 1000),
                'landmarks': landmarks,
                'hand_count': len(landmarks),
                'confidence': 0.8 if landmarks else 0.0
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
        """Loop principal de detecção simulada"""
        logger.info("Iniciando loop de deteccao simulada...")
        
        while self.is_running:
            try:
                # Simula detecção de mãos
                landmarks = self.detect_hands_simulated()
                
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
            
        logger.info("Servico de deteccao parado")

    def get_status(self):
        """Retorna status do serviço"""
        return {
            'running': self.is_running,
            'camera_opened': True,  # Simulado
            'camera_index': self.camera_index,
            'fps': self.fps
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
    logger.info("Iniciando servico de deteccao de maos Python (simulado)...")
    app.run(host='0.0.0.0', port=5000, debug=False) 