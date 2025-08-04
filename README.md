# 🎯 Touch Virtual - Sistema de Touchscreen Virtual

Transforme qualquer tela de notebook em touchscreen virtual usando reconhecimento de gestos das mãos via webcam.

## 🚀 Características Principais

- **Detecção de Mãos em Tempo Real**: Usando OpenCV e algoritmos de visão computacional
- **Reconhecimento de Gestos**: Clique, duplo clique, arrastar, scroll, zoom
- **Mapeamento Inteligente**: Conversão automática de coordenadas da câmera para a tela
- **Calibração Automática**: Sistema de calibração para precisão máxima
- **Interface Web Moderna**: Dashboard em tempo real com visualização dos gestos
- **WebSocket em Tempo Real**: Comunicação instantânea entre frontend e backend
- **Simulação de Mouse**: Usando Java Robot para eventos de mouse/toque
- **Configuração Flexível**: Sistema de configurações avançadas via properties

## 🛠️ Tecnologias Utilizadas

- **Spring Boot 3.2+**: Framework principal
- **OpenCV 4.8+**: Processamento de imagem e visão computacional
- **JavaCV**: Wrapper Java para OpenCV
- **WebSocket**: Comunicação em tempo real
- **Thymeleaf**: Template engine para interface web
- **Java Robot**: Simulação de eventos de mouse
- **Maven**: Gerenciamento de dependências

## 📋 Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Webcam funcional
- Windows/Mac/Linux

## 🚀 Instalação e Execução

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/touch-virtual.git
cd touch-virtual
```

### 2. Compile o projeto
```bash
mvn clean compile
```

### 3. Execute a aplicação
```bash
mvn spring-boot:run
```

### 4. Acesse a interface
Abra seu navegador e acesse: `http://localhost:8080`

## 🎮 Como Usar

### 1. Calibração Inicial
1. Acesse a interface web
2. Clique em "Iniciar Calibração"
3. Posicione sua mão nos pontos indicados na tela
4. Aguarde a confirmação de calibração completa

### 2. Gestos Suportados

| Gesto | Descrição | Ação |
|-------|-----------|------|
| **Mão Aberta** | Mão aberta movendo | Movimento do cursor |
| **Clique** | Dedo indicador estendido, outros dobrados | Clique do mouse |
| **Clique Direito** | Dois dedos estendidos | Clique direito |
| **Duplo Clique** | Dois cliques rápidos | Duplo clique |
| **Arrastar** | Gesto de pinça mantido | Arrastar e soltar |
| **Scroll** | Mão fechada movendo | Scroll vertical/horizontal |
| **Zoom** | Pinça abrindo/fechando | Zoom in/out |

### 3. Configurações

Acesse `http://localhost:8080/settings` para:
- Ajustar sensibilidade
- Configurar gestos ativos
- Modificar deadband
- Personalizar interface

## 🔧 Configuração Avançada

### Arquivo `application.properties`

```properties
# Configurações da câmera
camera.device-index=0
camera.frame-width=640
camera.frame-height=480
camera.fps=30

# Configurações de detecção
detection.confidence-threshold=0.7
detection.smoothing-factor=0.8
detection.deadband=0.05

# Configurações de gestos
gesture.timeout-ms=500
gesture.min-confidence=0.6

# Configurações do mouse
mouse.enabled=true
mouse.click-delay=10
```

### Variáveis de Ambiente

```bash
# Porta do servidor
SERVER_PORT=8080

# Dispositivo da câmera
CAMERA_DEVICE_INDEX=0

# Modo debug
DEBUG_ENABLED=true
```

## 📊 API REST

### Endpoints Principais

#### Status do Sistema
```http
GET /api/gestures/status
```

#### Reconhecimento de Gestos
```http
GET /api/gestures/recognize
```

#### Landmarks Detectados
```http
GET /api/gestures/landmarks
```

#### Configuração do Mouse
```http
POST /api/gestures/mouse/enable?enabled=true
```

#### Calibração
```http
POST /api/calibration/start?sessionId=user123
POST /api/calibration/add-point?sessionId=user123&cameraX=0.5&cameraY=0.5&screenX=500&screenY=300
```

### WebSocket

#### Conectar
```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);
```

#### Tópicos Disponíveis
- `/topic/gestures` - Atualizações de gestos
- `/topic/landmarks` - Landmarks detectados
- `/topic/stats` - Estatísticas de performance
- `/topic/calibration` - Status de calibração
- `/topic/mouse` - Informações do mouse

## 🏗️ Arquitetura

```
src/main/java/com/touchvirtual/
├── TouchVirtualApplication.java          # Classe principal
├── config/                              # Configurações
│   ├── OpenCVConfig.java               # Configuração OpenCV
│   ├── WebSocketConfig.java            # Configuração WebSocket
│   └── CameraConfig.java               # Configuração câmera
├── controller/                          # Controllers REST
│   ├── GestureController.java          # Endpoints de gestos
│   ├── CalibrationController.java      # Endpoints de calibração
│   └── WebSocketController.java        # WebSocket controller
├── service/                            # Lógica de negócio
│   ├── HandDetectionService.java       # Detecção de mãos
│   ├── GestureRecognitionService.java  # Reconhecimento de gestos
│   ├── CoordinateMappingService.java   # Mapeamento de coordenadas
│   ├── MouseSimulationService.java     # Simulação de mouse
│   └── CalibrationService.java         # Calibração
├── model/                              # Modelos de dados
│   ├── HandLandmark.java              # Landmark da mão
│   ├── GestureType.java               # Tipos de gestos
│   ├── TouchEvent.java                # Eventos de toque
│   ├── CalibrationData.java           # Dados de calibração
│   └── UserSettings.java              # Configurações do usuário
├── dto/                                # Data Transfer Objects
│   ├── GestureResponse.java           # Resposta de gestos
│   ├── CalibrationRequest.java        # Requisição de calibração
│   └── TouchEventDTO.java             # DTO de eventos
└── util/                               # Utilitários
    ├── OpenCVUtils.java               # Utilitários OpenCV
    ├── MathUtils.java                 # Utilitários matemáticos
    └── GestureUtils.java              # Utilitários de gestos
```

## 🔍 Monitoramento

### Health Check
```http
GET /actuator/health
```

### Métricas
```http
GET /actuator/metrics
```

### Logs
```bash
tail -f logs/touch-virtual.log
```

## 🐛 Troubleshooting

### Problema: Câmera não detectada
```bash
# Verifique se a webcam está funcionando
ls /dev/video*  # Linux
# ou
ffmpeg -f dshow -list_devices true -i dummy  # Windows
```

### Problema: OpenCV não carrega
```bash
# Verifique se as bibliotecas nativas estão instaladas
mvn dependency:resolve
```

### Problema: Performance baixa
```properties
# Ajuste as configurações de performance
performance.max-threads=8
performance.enable-gpu=true
camera.fps=15
```

## 🧪 Testes

### Executar todos os testes
```bash
mvn test
```

### Testes específicos
```bash
mvn test -Dtest=HandDetectionServiceTest
mvn test -Dtest=GestureRecognitionServiceTest
```

## 📈 Performance

### Otimizações Recomendadas

1. **GPU Acceleration**: Habilite se disponível
2. **Threading**: Ajuste o número de threads
3. **Frame Rate**: Reduza para melhor performance
4. **Resolution**: Use resolução menor se necessário

### Métricas de Performance

- **Latência**: < 50ms entre gesto e ação
- **Precisão**: > 95% na detecção de gestos
- **FPS**: 30fps para detecção suave
- **CPU**: < 30% de uso médio

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 🙏 Agradecimentos

- OpenCV Community
- Spring Boot Team
- JavaCV Contributors
- MediaPipe Team

## 📞 Suporte

- **Issues**: [GitHub Issues](https://github.com/seu-usuario/touch-virtual/issues)
- **Documentação**: [Wiki](https://github.com/seu-usuario/touch-virtual/wiki)
- **Email**: suporte@touchvirtual.com

---

**Touch Virtual** - Transformando gestos em interação digital! 🎯✨ 