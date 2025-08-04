#!/usr/bin/env python3
"""
Script para testar o sistema híbrido Python + Java
"""
import requests
import time
import json

def test_python_service():
    """Testa o serviço Python"""
    print("🔍 Testando serviço Python...")
    
    try:
        # Testa health check
        response = requests.get('http://localhost:5000/api/hand-detection/health')
        if response.status_code == 200:
            print("✅ Serviço Python: OK")
            return True
        else:
            print("❌ Serviço Python: ERRO")
            return False
    except Exception as e:
        print(f"❌ Serviço Python: {e}")
        return False

def test_java_service():
    """Testa o serviço Java"""
    print("🔍 Testando serviço Java...")
    
    try:
        # Testa status do Java
        response = requests.get('http://localhost:8080/api/gestures/status')
        if response.status_code == 200:
            data = response.json()
            print("✅ Serviço Java: OK")
            print(f"   - Mão detectada: {data.get('handDetected', False)}")
            print(f"   - Confiança: {data.get('detectionConfidence', 0)}")
            return True
        else:
            print("❌ Serviço Java: ERRO")
            return False
    except Exception as e:
        print(f"❌ Serviço Java: {e}")
        return False

def test_communication():
    """Testa comunicação entre Python e Java"""
    print("🔍 Testando comunicação Python -> Java...")
    
    try:
        # Simula dados de mão do Python
        test_data = {
            'timestamp': int(time.time() * 1000),
            'landmarks': [[
                {'x': 0.5, 'y': 0.5, 'z': 0.0, 'confidence': 0.8}
            ]],
            'hand_count': 1,
            'confidence': 0.8
        }
        
        # Envia para o Java
        response = requests.post(
            'http://localhost:8082/api/hand-detection',
            json=test_data,
            headers={'Content-Type': 'application/json'}
        )
        
        if response.status_code == 200:
            print("✅ Comunicação Python -> Java: OK")
            return True
        else:
            print(f"❌ Comunicação Python -> Java: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Comunicação Python -> Java: {e}")
        return False

def main():
    """Função principal"""
    print("🚀 Testando Sistema Híbrido Python + Java")
    print("=" * 50)
    
    # Testa serviços
    python_ok = test_python_service()
    java_ok = test_java_service()
    comm_ok = test_communication()
    
    print("\n" + "=" * 50)
    print("📊 RESULTADOS:")
    print(f"   Python: {'✅ OK' if python_ok else '❌ ERRO'}")
    print(f"   Java: {'✅ OK' if java_ok else '❌ ERRO'}")
    print(f"   Comunicação: {'✅ OK' if comm_ok else '❌ ERRO'}")
    
    if python_ok and java_ok and comm_ok:
        print("\n🎉 SISTEMA HÍBRIDO FUNCIONANDO!")
        print("   Acesse: http://localhost:8082")
    else:
        print("\n⚠️ ALGUNS PROBLEMAS DETECTADOS")
        print("   Verifique os logs acima")

if __name__ == "__main__":
    main() 