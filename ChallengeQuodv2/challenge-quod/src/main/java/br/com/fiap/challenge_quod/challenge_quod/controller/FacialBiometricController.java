package br.com.fiap.challenge_quod.challenge_quod.controller;

import br.com.fiap.challenge_quod.challenge_quod.dto.FacialBiometricRequestDTO;
import br.com.fiap.challenge_quod.challenge_quod.dto.FacialBiometricResponseDTO;
import br.com.fiap.challenge_quod.challenge_quod.model.FacialBiometricDocument;
import br.com.fiap.challenge_quod.challenge_quod.model.Device;
import br.com.fiap.challenge_quod.challenge_quod.model.Metadata;
import br.com.fiap.challenge_quod.challenge_quod.repository.FacialBiometricRepository;
import br.com.fiap.challenge_quod.challenge_quod.util.FacialBiometricComparator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/images")
public class FacialBiometricController {

    private static final Logger logger = LoggerFactory.getLogger(FacialBiometricController.class);
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    private static final String FRAUD_API_URL = "https://challenge-quod-fraudes.free.beeceptor.com";

    @Autowired
    private FacialBiometricRepository facialBiometricRepository;

    @Autowired
    private HttpServletRequest request;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FacialBiometricResponseDTO> uploadImages(@Valid @ModelAttribute FacialBiometricRequestDTO requestDTO) {
        logger.info("Recebida requisição de upload de imagens. Test Mode: {}", requestDTO.isTestMode());
        logger.info("Latitude 1: {}, Longitude 1: {}", requestDTO.getLatitude1(), requestDTO.getLongitude1());
        logger.info("Latitude 2: {}, Longitude 2: {}", requestDTO.getLatitude2(), requestDTO.getLongitude2());
        logger.info("Android Version: {}, API Level: {}", requestDTO.getAndroidVersion(), requestDTO.getApiLevel());
        logger.info("Fabricante: {}, Modelo: {}, Data de Captura: {}", requestDTO.getManufacturer(), requestDTO.getModel(), requestDTO.getCaptureDate());
        logger.info("Diretório de upload: {}", UPLOAD_DIR);

        File file1 = null;
        File file2 = null;
        String fraudType;

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                logger.info("Diretório de upload criado: {}", created);
            }

            String fileName1 = "QuodAntifraude_" + System.currentTimeMillis() + ".jpg";
            String fileName2 = "QuodAntifraude_" + (System.currentTimeMillis() + 1000) + ".jpg";
            file1 = new File(UPLOAD_DIR + fileName1);
            file2 = new File(UPLOAD_DIR + fileName2);

            requestDTO.getImage1().transferTo(file1);
            requestDTO.getImage2().transferTo(file2);

            logger.info("Primeira imagem salva em: {}", file1.getAbsolutePath());
            logger.info("Segunda imagem salva em: {}", file2.getAbsolutePath());

            double lat1 = Double.parseDouble(requestDTO.getLatitude1());
            double lon1 = Double.parseDouble(requestDTO.getLongitude1());
            double lat2 = Double.parseDouble(requestDTO.getLatitude2());
            double lon2 = Double.parseDouble(requestDTO.getLongitude2());

            FacialBiometricComparator.ImageComparisonResult result = FacialBiometricComparator.compareImages(
                    file1.getAbsolutePath(), file2.getAbsolutePath(), lat1, lon1, lat2, lon2);

            fraudType = result.isFacesEqual() && result.isCoordinatesEqual() ? "Sem Fraude" :
                    (!result.isFacesEqual() ? "Faces Diferentes" : "Coordenadas Diferentes");

            logger.info("Resultado da comparação: areFacesEqual={}, areCoordinatesEqual={}, fraudType={}",
                    result.isFacesEqual(), result.isCoordinatesEqual(), fraudType);

            String transacaoId = UUID.randomUUID().toString();
            ZonedDateTime now = ZonedDateTime.now();
            String baseDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'_T_'HH:mm:ss"));
            String micros = String.format("%07d", now.getNano() / 100);
            String dataCaptura24h = baseDateTime + "._" + micros + now.format(DateTimeFormatter.ofPattern("XXX"));
            String ipOrigem = request.getRemoteAddr();

            String imagemId1 = UUID.randomUUID().toString();
            String imagemId2 = UUID.randomUUID().toString();

            FacialBiometricDocument imageData = new FacialBiometricDocument(
                    transacaoId,
                    "Facial",
                    fraudType,
                    requestDTO.getCaptureDate(),
                    dataCaptura24h,
                    new Device(requestDTO.getManufacturer(), requestDTO.getModel(), "Android " + requestDTO.getAndroidVersion()),
                    Arrays.asList("sms", "email"),
                    "Sistema Anti Fraude",
                    new Metadata(result.getLat1(), result.getLon1(), ipOrigem, fileName1, imagemId1),
                    new Metadata(result.getLat2(), result.getLon2(), ipOrigem, fileName2, imagemId2)
            );

            if ("Sem Fraude".equals(fraudType)) {
                imageData.setFraudeStatus(1);
            } else {
                imageData.setFraudeStatus(2);
            }

            logger.debug("Salvando FacialBiometricDocument no MongoDB: {}", imageData);
            FacialBiometricDocument savedData = facialBiometricRepository.save(imageData);
            logger.info("Dados salvos no MongoDB com ID: {}", savedData.getTransacaoId());

            if (!"Sem Fraude".equals(fraudType)) {
                sendToExternalApi(savedData);
            } else {
                logger.info("Nenhuma fraude detectada (fraudType=OK). Nenhuma notificação enviada à API externa.");
            }

            String message = "Imagens comparadas. Resultado: " + (result.isFacesEqual() && result.isCoordinatesEqual() ? "Similares" : "Diferentes");
            String deviceInfo = String.format("%s %s, Android %s",
                    requestDTO.getManufacturer(), requestDTO.getModel(), requestDTO.getAndroidVersion());
            FacialBiometricResponseDTO responseDTO = new FacialBiometricResponseDTO(
                    message,
                    fraudType,
                    result.getSimilarityScore(),
                    result.isCoordinatesEqual(),
                    result.getEuclideanDistance(),
                    deviceInfo
            );

            String responseJson = objectMapper.writeValueAsString(responseDTO);
            logger.info("JSON retornado ao frontend: {}", responseJson);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseDTO);

        } catch (IOException e) {
            logger.error("Erro ao salvar as imagens: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new FacialBiometricResponseDTO("Erro ao processar o upload: " + e.getMessage(), null, 0.0, null, 0.0, null));
        } catch (NumberFormatException e) {
            logger.error("Erro ao converter coordenadas: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new FacialBiometricResponseDTO("Erro nos dados fornecidos: " + e.getMessage(), null, 0.0, null, 0.0, null));
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação: {}", e.getMessage(), e);
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new FacialBiometricResponseDTO(e.getMessage(), null, 0.0, null, 0.0, null));
        }
    }

    private void sendToExternalApi(FacialBiometricDocument savedData) {
        try {
            String json = objectMapper.writeValueAsString(savedData);
            logger.info("Fraude detectada. Enviando JSON para a API externa: {}", json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FRAUD_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.warn(">>> RESPOSTA DA API EXTERNA <<< Status: {}, Body: {}", response.statusCode(), response.body());
        } catch (Exception e) {
            logger.error("Erro ao enviar para a API externa: {}", e.getMessage(), e);
        }
    }
}