#!/usr/bin/env python3
"""
Script de teste para verificar dependências Python
"""
import sys

def test_dependencies():
    """Testa todas as dependências necessárias"""
    print(f"Python {sys.version}")
    
    # Testa NumPy
    try:
        import numpy as np
        print(f"NumPy: {np.__version__}")
    except ImportError as e:
        print(f"Erro NumPy: {e}")
        return False
    
    # Testa OpenCV
    try:
        import cv2
        print(f"OpenCV: {cv2.__version__}")
    except ImportError as e:
        print(f"Erro OpenCV: {e}")
        return False
    
    # Testa Flask
    try:
        import flask
        print(f"Flask: {flask.__version__}")
    except ImportError as e:
        print(f"Erro Flask: {e}")
        return False
    
    # Testa Requests
    try:
        import requests
        print(f"Requests: {requests.__version__}")
    except ImportError as e:
        print(f"Erro Requests: {e}")
        return False
    
    # Testa câmera
    try:
        cap = cv2.VideoCapture(0)
        if cap.isOpened():
            print("Camera: OK")
            cap.release()
        else:
            print("Camera: ERRO")
    except Exception as e:
        print(f"Erro Camera: {e}")
    
    return True

if __name__ == "__main__":
    print("Testando dependências Python...")
    success = test_dependencies()
    
    if success:
        print("\n✅ TODAS DEPENDÊNCIAS OK!")
        print("Você pode executar: hand_detection_service_opencv.py")
    else:
        print("\n❌ ALGUMAS DEPENDÊNCIAS COM PROBLEMA")
        print("Execute: install-python-deps-311.ps1") 