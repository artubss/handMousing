# 🎯 Touch Virtual - Sistema de Touchscreen Virtual

Sistema avançado de touchscreen virtual usando detecção de mãos em tempo real com OpenCV e JavaCV.

## ✨ Funcionalidades

- **🎥 Detecção de Mãos Real**: Usando algoritmos avançados de visão computacional
- **🤖 Controle de Mouse**: Simulação de eventos de mouse com Java Robot
- **🎯 Calibração Automática**: Sistema de calibração integrado
- **📱 Interface Web**: Interface responsiva e moderna
- **⚡ Tempo Real**: Processamento em tempo real com baixa latência

## 🚀 Como Executar

### Pré-requisitos

- Java 21 ou superior
- Maven 3.6 ou superior
- Câmera webcam (opcional - funciona com câmera simulada)

### Execução Rápida

#### Windows

```bash
start.bat
```

#### Linux/Mac

```bash
chmod +x start.sh
./start.sh
```

#### Execução Manual

```bash
# Desabilita modo headless para Robot funcionar
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"
```

## 🎥 Detecção de Mãos Real

O sistema implementa detecção de mãos usando técnicas avançadas:

### Algoritmos Utilizados

1. **Detecção de Pele**: Usando espaço de cores HSV
2. **Segmentação Morfológica**: Limpeza de ruído
3. **Análise de Contornos**: Identificação de formas de mão
4. **Cálculo de Momentos**: Análise de forma e orientação
5. **Geração de Landmarks**: 21 pontos de referência da mão

### Características da Detecção

- ✅ **Detecção de Pele**: Range HSV otimizado para tons de pele
- ✅ **Filtros Morfológicos**: Remoção de ruído e preenchimento de gaps
- ✅ **Análise de Forma**: Razão largura/altura e convexidade
- ✅ **Landmarks Reais**: Baseados na forma detectada da mão
- ✅ **Orientação da Mão**: Cálculo de ângulo de rotação

## 🤖 Controle de Mouse

### Funcionalidades

- **Movimento do Mouse**: Mapeamento direto de coordenadas
- **Cliques**: Simulação de cliques esquerdo e direito
- **Arrastar**: Funcionalidade de drag and drop
- **Scroll**: Controle de scroll vertical e horizontal
- **Zoom**: Gestos de zoom in/out

### Configurações

- **Deadband**: Reduz tremores (configurável)
- **Sensibilidade**: Ajuste de velocidade (configurável)
- **Suavização**: Filtros para movimento mais suave

## 🎯 Calibração

### Tipos de Calibração

1. **Automática**: Calibração baseada em pontos padrão
2. **Manual**: Calibração por pontos específicos
3. **Reset**: Limpeza de dados de calibração

### Pontos de Calibração

- Canto superior esquerdo
- Canto superior direito
- Canto inferior direito
- Canto inferior esquerdo

## 📱 Interface Web

### Páginas Disponíveis

- **Dashboard**: Visão geral do sistema
- **Câmera**: Visualização e controle da câmera
- **Calibração**: Configuração de mapeamento
- **Gestos**: Reconhecimento de gestos
- **Configurações**: Ajustes do sistema

### Endpoints API

- `GET /api/system/status`: Status do sistema
- `POST /api/detection/start`: Iniciar detecção
- `POST /api/detection/stop`: Parar detecção
- `POST /api/calibration/auto`: Calibração automática
- `POST /api/calibration/reset`: Resetar calibração

## 🔧 Configuração

### application.yml

```yaml
camera:
  device-index: 0
  frame-width: 640
  frame-height: 480
  fps: 30
  auto-exposure: true

detection:
  confidence-threshold: 0.7
  min-area: 5000
  max-area: 100000

mouse:
  enabled: true
  deadband: 0.05
  sensitivity: 1.0
```

## 🛠️ Tecnologias

- **Spring Boot 3.5.4**: Framework principal
- **JavaCV 1.5.9**: Wrapper Java para OpenCV
- **OpenCV**: Visão computacional
- **Thymeleaf**: Template engine
- **WebSocket**: Comunicação em tempo real
- **Java Robot**: Controle de mouse

## 📊 Logs e Debug

### Logs Informativos

- ✅ Sucesso
- ⚠️ Avisos
- ❌ Erros
- 🔄 Processos
- 🎯 Detecção
- 📹 Câmera
- 🤖 Robot

### Debug

Para ativar logs detalhados:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false -Dlogging.level.com.touchvirtual=DEBUG"
```

## 🎯 Uso

1. **Execute a aplicação** usando os scripts fornecidos
2. **Acesse** http://localhost:8082
3. **Navegue** para a página da Câmera
4. **Inicie** a detecção de mãos
5. **Calibre** o sistema se necessário
6. **Use** gestos para controlar o mouse

## 🔍 Solução de Problemas

### Câmera não detectada

- Verifique se a câmera está conectada
- Teste diferentes índices de câmera
- O sistema usa câmera simulada como fallback

### Robot não funciona

- Execute com `-Djava.awt.headless=false`
- Verifique permissões de segurança
- Use os scripts fornecidos

### Detecção imprecisa

- Ajuste a calibração
- Melhore a iluminação
- Configure sensibilidade

## 📈 Melhorias Futuras

- [ ] Integração com MediaPipe
- [ ] Detecção de múltiplas mãos
- [ ] Reconhecimento de gestos avançados
- [ ] Machine Learning para melhor precisão
- [ ] Suporte a diferentes tipos de câmera

## 🤝 Contribuição

Contribuições são bem-vindas! Por favor, abra uma issue ou pull request.

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para detalhes.

---

**🎯 Touch Virtual** - Transformando gestos em interação digital
