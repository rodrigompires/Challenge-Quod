# Quod Anti-Fraude

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green) ![Android](https://img.shields.io/badge/Android-Jetpack_Compose-blue) ![MongoDB](https://img.shields.io/badge/MongoDB-7-green).

## 📖 **Descrição**

Este projeto é uma solução que visa a simulação de validações como **validação facial**, **validação biométrica** e **validação documental**, desenvolvida como parte do **Challenge Quod**. 
A aplicação permite a validação de documentos brasileiros (**RG**, **CPF**, **CNH**) utilizando técnicas avançadas de **processamento de imagem** e **OCR**, além de autenticação biométrica (**impressão digital** e **reconhecimento facial**) com análise de **geolocalização** e **consistência de dispositivos**. 
O **backend** é construído em **Java** com **Spring Boot**, enquanto o **frontend** é uma aplicação **Android** desenvolvida com **Jetpack Compose**, utilizando bibliotecas como **ML Kit Document Scanner**, **CameraX**, e **Biometric API**.


### **Funcionalidades**


- **Autenticação Biométrica Facial**:
  - **Reconhecimento facial** com **ML Kit Face Detection** e análise de **vivacidade passivo** (liveness detection).
  - Detecção de possíveis **fraudes** nas imagens faciais **(posição neutra e sorrindo)**.
- **Validação de Documentos**:
  - Escaneamento de **RG**, **CPF** e **CNH** com **ML Kit Document Scanner**.
  - Validação de características visuais (**cores**, **dimensões**) e textuais (**OCR** com **Tesseract**).
  - Detecção de possíveis **fraudes** em documentos.
- **Autenticação Biométrica**:
  - Validação de **impressões digitais** com **Biometric API**.
  - Verificação de **geolocalização** para detectar movimentos suspeitos.
  - Validação de **consistência do dispositivo** (fabricante, modelo, SO).
- **Integração com Backend**:
  - Comunicação com **API REST** para validação de documentos e biometria.
  - Reporte de **fraudes** para uma API externa.
- **Frontend Android**:
  - Interface moderna e responsiva com **Jetpack Compose**.
  - Captura de imagens de **documentos** e **selfies** com **CameraX**.
  - Feedback em tempo real para o usuário durante validações.

## 🛠️ **Tecnologias Utilizadas**

### **Backend**

- **Java 17**: Linguagem principal para o backend.
- **Spring Boot 3.2**: Framework para construção da **API REST**.
- **OpenCV 4.8.0**: **Processamento de imagens** para validação de documentos.
- **Tesseract 5.3.0**: **OCR** para extração de texto de imagens.
- **MongoDB 7**: Banco de dados NoSQL para armazenamento de dados.
- **SLF4J**: Logging (logs comentados para produção).
- **Maven**: Gerenciamento de dependências.

### **Frontend**

- **Kotlin 1.9**: Linguagem principal para o frontend.
- **Jetpack Compose 1.6**: Biblioteca para construção de interfaces modernas.
- **ML Kit Document Scanner**: Escaneamento de documentos com suporte a frente e verso.
- **ML Kit Face Detection**: Detecção facial e análise de **vivacidade** para biometria.
- **CameraX**: Captura de imagens para documentos e selfies.
- **Biometric API**: Autenticação biométrica com **impressão digital**.
- **Retrofit**: Comunicação com a **API** do backend.
- **FusedLocationProviderClient**: Captura de **geolocalização**.
- **Gradle**: Gerenciamento de dependências.

## 📂 **Estrutura do Projeto**

### **Backend**

![image](https://github.com/user-attachments/assets/ab17e8d3-e7ca-4cc4-81e7-d325ac6d9f2c)

### **Frontend**

![image](https://github.com/user-attachments/assets/95780543-d9bf-4b01-9e01-02e304d0cd24)


## 🚀 **Como Executar**

### **Pré-requisitos**

- **Backend**:
  - **Java 17**
  - **Maven 3.8+**
  - **MongoDB 7**
  - **OpenCV 4.8.0**
  - **Tesseract 5.3.0**
- **Frontend**:
  - **Android Studio** (versão mais recente)
  - **Kotlin 1.9**
  - **SDK Android API 34**
  - Dispositivo/emulador com **Android 8.0+** (API 26)
  - Cabo USB para conectar o smartphone ao computador

 ### **Configuração do Backend**

1. **Clone o repositório**:
   ```bash
   git clone https://github.com/seu-usuario/challenge-quod.git
   cd challenge-quod/backend

### **Configuração e Instalação do FrontEnd**

1. **Conecte o Smartphone ao Computador:**
   - Use um cabo USB para conectar seu smartphone Android ao computador.
   - Certifique-se de que o smartphone está desbloqueado.
   - **Ative a opção Depuração USB no smartphone:**
      - Vá em Configurações > Sobre o telefone.
      - Toque várias vezes em Número da versão até ativar o modo desenvolvedor.
      - Volte para Configurações > Opções do desenvolvedor.
      - Ative a opção Depuração USB.
2. **Abra o Projeto no Android Studio:**
   - Inicie o Android Studio no computador.
3. **Configure a URL da API:**
   - Localize o arquivo do frontend responsável pela configuração da API (ex.: apiservice/RetrofitClient.kt).
   - Atualize a constante de URL:
     - **const val BASE_URL = "http://<seu-ip>:8080/api/"**
   - Substitua <seu-ip> pelo IP do computador onde o backend está rodando (ex.: 192.168.1.x).

