#!/usr/bin/env python3
"""
Script de teste para verificar se a câmera funciona corretamente
"""
import cv2
import time
import logging

# Configuração de logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def test_camera():
    """Testa diferentes configurações de câmera"""
    
    # Lista de backends para tentar (Windows)
    backends = [
        (cv2.CAP_DSHOW, "DirectShow"),
        (cv2.CAP_MSMF, "Media Foundation"),
        (cv2.CAP_ANY, "Qualquer")
    ]
    
    # Tenta diferentes índices de câmera
    camera_indices = [0, 1, 2]
    
    for camera_index in camera_indices:
        logger.info(f"\n=== Testando câmera no índice {camera_index} ===")
        
        for backend_code, backend_name in backends:
            logger.info(f"Tentando backend: {backend_name}")
            
            camera = None
            try:
                # Cria captura com backend específico
                camera = cv2.VideoCapture(camera_index, backend_code)
                
                if not camera.isOpened():
                    logger.warning(f"❌ Backend {backend_name} falhou para índice {camera_index}")
                    continue
                
                # Configura propriedades
                camera.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
                camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
                camera.set(cv2.CAP_PROP_FPS, 30)
                camera.set(cv2.CAP_PROP_BUFFERSIZE, 1)
                
                # Testa se consegue capturar frames
                success_count = 0
                total_attempts = 10
                
                for i in range(total_attempts):
                    ret, frame = camera.read()
                    if ret and frame is not None:
                        success_count += 1
                        logger.info(f"✅ Frame {i+1} capturado com sucesso")
                    else:
                        logger.warning(f"❌ Frame {i+1} falhou")
                    time.sleep(0.1)
                
                if success_count > 0:
                    width = int(camera.get(cv2.CAP_PROP_FRAME_WIDTH))
                    height = int(camera.get(cv2.CAP_PROP_FRAME_HEIGHT))
                    fps = camera.get(cv2.CAP_PROP_FPS)
                    
                    logger.info(f"🎉 SUCESSO! Backend: {backend_name}, Índice: {camera_index}")
                    logger.info(f"Resolução: {width}x{height}, FPS: {fps}")
                    logger.info(f"Taxa de sucesso: {success_count}/{total_attempts}")
                    
                    # Mostra o último frame capturado
                    if success_count > 0:
                        cv2.imshow(f'Camera Test - {backend_name}', frame)
                        cv2.waitKey(2000)  # Mostra por 2 segundos
                        cv2.destroyAllWindows()
                    
                    camera.release()
                    return True
                else:
                    logger.error(f"❌ Backend {backend_name} não conseguiu capturar nenhum frame")
                    
            except Exception as e:
                logger.error(f"❌ Erro com backend {backend_name}: {e}")
            finally:
                if camera is not None:
                    camera.release()
    
    logger.error("❌ NENHUMA CAMERA FUNCIONOU!")
    return False

if __name__ == '__main__':
    logger.info("🔍 Iniciando teste de câmera...")
    success = test_camera()
    
    if success:
        logger.info("✅ Teste concluído com sucesso!")
    else:
        logger.error("❌ Teste falhou!")
        logger.error("💡 Verifique:")
        logger.error("   1. Se a webcam está conectada")
        logger.error("   2. Se não está sendo usada por outro aplicativo")
        logger.error("   3. Execute o app Câmera do Windows")
        logger.error("   4. Reinicie o computador se necessário") 