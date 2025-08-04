# 🤖 Configuração do Robot para TouchVirtual

## ⚠️ Problema: Robot não disponível

Se você está vendo esta mensagem:

```
⚠️ Ambiente headless detectado - Robot não disponível
💡 Para usar o Robot, execute a aplicação com GUI ou use -Djava.awt.headless=false
```

## ✅ Soluções

### 1. Usar os Scripts Fornecidos (Recomendado)

#### Windows:

```bash
start.bat
```

#### Linux/Mac:

```bash
chmod +x start.sh
./start.sh
```

### 2. Execução Manual com Maven

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.awt.headless=false"
```

### 3. Configuração Permanente

O arquivo `.mvn/jvm.config` já está configurado com:

```
-Djava.awt.headless=false
```

### 4. Verificação no IDE

Se estiver usando IDE (IntelliJ, Eclipse, VS Code), adicione nas VM Options:

```
-Djava.awt.headless=false
```

## 🎯 Como Verificar se Funcionou

Quando a aplicação iniciar corretamente, você deve ver:

```
🤖 Robot inicializado para simulação de eventos
```

Em vez de:

```
⚠️ Ambiente headless detectado - Robot não disponível
```

## 🔧 Funcionalidades do Robot

Com o Robot habilitado, você terá:

- ✅ **Movimento do Mouse**: Controle direto do cursor
- ✅ **Cliques**: Simulação de cliques esquerdo e direito
- ✅ **Arrastar**: Funcionalidade de drag and drop
- ✅ **Scroll**: Controle de scroll vertical e horizontal
- ✅ **Zoom**: Gestos de zoom in/out

## 🐛 Solução de Problemas

### Erro de Segurança

Se aparecer erro de segurança, execute como administrador:

#### Windows:

```bash
# Execute o terminal como administrador
start.bat
```

#### Linux:

```bash
sudo ./start.sh
```

### Permissões no Linux

```bash
# Adicione permissões de acesso ao display
xhost +local:
export DISPLAY=:0
./start.sh
```

### macOS

```bash
# Permissões de acessibilidade
# Vá em: System Preferences > Security & Privacy > Privacy > Accessibility
# Adicione Java ou o terminal à lista
```

## 🎮 Testando o Robot

1. Execute a aplicação com Robot habilitado
2. Acesse: http://localhost:8082
3. Vá para a página da Câmera
4. Inicie a detecção de mãos
5. Mova sua mão na frente da câmera
6. O cursor deve se mover junto com sua mão

## 📞 Suporte

Se ainda não funcionar:

1. Verifique se está em ambiente com GUI (não servidor)
2. Confirme que não está em container Docker
3. Execute os scripts fornecidos em vez de comandos manuais
4. Verifique permissões de sistema

---

**🎯 TouchVirtual** - Controle por gestos funcionando! 🤖✨
