package br.com.fiap.challenge_quod.challenge_quod.service;

import br.com.fiap.challenge_quod.challenge_quod.dto.BiometricRequestDTO;
import br.com.fiap.challenge_quod.challenge_quod.dto.BiometricResponseDTO;
import br.com.fiap.challenge_quod.challenge_quod.model.BiometricModel;
import br.com.fiap.challenge_quod.challenge_quod.model.Device;
import br.com.fiap.challenge_quod.challenge_quod.model.Metadata;
import br.com.fiap.challenge_quod.challenge_quod.repository.BiometricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;


@Service
public class BiometricService {

    private static final Logger logger = LoggerFactory.getLogger(BiometricService.class);
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final String FRAUD_API_URL = "https://challenge-quod-fraudes.free.beeceptor.com";
    private static final double MAX_SPEED_KMH = 1000;
    private static final double MIN_TIME_DIFF_HOURS = 0.0001; // 0.36 segundos

    // Variáveis de teste para forçar fraudes
    private static final boolean FORCE_SUSPICIOUS_MOVEMENT = false; // Forçar movimento suspeito
    private static final boolean FORCE_DEVICE_INCONSISTENCY = false; // Forçar dispositivo inconsistente

    private final BiometricRepository biometricRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BiometricService(BiometricRepository biometricRepository) {
        this.biometricRepository = biometricRepository;
    }

    public BiometricResponseDTO validateBiometric(BiometricRequestDTO request) {
        logger.info("Validando biometria - DeviceId: {}, Authenticated: {}, FailedAttempts: {}, Latitude: {}, Longitude: {}",
                request.getDeviceId(), request.isAuthenticated(), request.getFailedAttempts(), request.getLatitude(), request.getLongitude());

        String deviceId = request.getDeviceId();
        int currentFailedAttempts = request.getFailedAttempts();
        LocalDateTime timestamp = request.getCaptureDate() != null ? LocalDateTime.parse(request.getCaptureDate()) : LocalDateTime.now();
        String dataCaptura = timestamp.toString();
        String dataCaptura24h = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'_T_'HH:mm:ss'.'_nnnnnnnn"));

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String clientIp = "unknown";
        if (attributes != null) {
            HttpServletRequest httpRequest = attributes.getRequest();
            clientIp = httpRequest.getRemoteAddr();
            String forwardedFor = httpRequest.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isEmpty()) {
                clientIp = forwardedFor.split(",")[0].trim();
            }
        }

        Device dispositivo = new Device(
                request.getManufacturer() != null ? request.getManufacturer() : "unknown",
                request.getModel() != null ? request.getModel() : "unknown",
                request.getAndroidVersion() != null ? "Android " + request.getAndroidVersion() : "unknown"
        );

        Metadata metadadosDigital = new Metadata(
                request.getLatitude() != null ? request.getLatitude() : 0.0,
                request.getLongitude() != null ? request.getLongitude() : 0.0,
                clientIp,
                null,
                null
        );

        String geoAnalysis = validateGeolocation(deviceId, request.getLatitude(), request.getLongitude(), timestamp);
        boolean suspiciousMovement = geoAnalysis.contains("suspeito") || FORCE_SUSPICIOUS_MOVEMENT;

        String deviceAnalysis = validateDeviceConsistency(deviceId, dispositivo);
        boolean deviceInconsistent = deviceAnalysis.contains("inconsistente") || FORCE_DEVICE_INCONSISTENCY;

        String analysisReport = String.format("Geolocalização: %s | Dispositivo: %s | Tentativas falhas: %d",
                geoAnalysis, deviceAnalysis, currentFailedAttempts);

        BiometricModel biometricModel;

        if (request.isAuthenticated() && !suspiciousMovement && !deviceInconsistent) {
            biometricModel = new BiometricModel(
                    String.valueOf(System.currentTimeMillis()),
                    deviceId,
                    "Sem fraude",
                    dataCaptura,
                    dataCaptura24h,
                    dispositivo,
                    Arrays.asList("sms", "email"),
                    "Sistema Anti Fraude",
                    metadadosDigital
            );
            biometricModel.setFraudeStatus(1);
            biometricModel.setAnalysisReport(analysisReport);
            biometricRepository.save(biometricModel);
            logger.info("Autenticação biométrica bem-sucedida para DeviceId: {}", deviceId);
            return new BiometricResponseDTO("success", "Autenticação bem-sucedida", true, analysisReport);
        } else {
            String fraudReason = "";
            if (suspiciousMovement) fraudReason += "Movimento suspeito detectado; ";
            if (deviceInconsistent) fraudReason += "Dispositivo inconsistente; ";
            if (currentFailedAttempts >= MAX_FAILED_ATTEMPTS) fraudReason += "Muitas tentativas falhas";

            logger.warn("Falha ou suspeita na autenticação para DeviceId: {}. Detalhes: {}", deviceId, fraudReason);

            if (currentFailedAttempts >= MAX_FAILED_ATTEMPTS || suspiciousMovement || deviceInconsistent) {
                biometricModel = new BiometricModel(
                        String.valueOf(System.currentTimeMillis()),
                        deviceId,
                        fraudReason.trim(),
                        dataCaptura,
                        dataCaptura24h,
                        dispositivo,
                        Arrays.asList("sms", "email"),
                        "Sistema Anti Fraude",
                        metadadosDigital
                );
                biometricModel.setFraudeStatus(2);
                biometricModel.setAnalysisReport(analysisReport);
                BiometricModel savedModel = biometricRepository.save(biometricModel);
                logger.error("Fraude detectada para DeviceId: {}. Detalhes: {}", deviceId, fraudReason);
                sendToExternalApi(savedModel);
                return new BiometricResponseDTO("fraud_detected", fraudReason.trim(), false, analysisReport);
            } else {
                return new BiometricResponseDTO("failure", "Falha na autenticação", false, analysisReport);
            }
        }
    }

    private String validateGeolocation(String deviceId, Double latitude, Double longitude, LocalDateTime currentTime) {
        BiometricModel lastCapture = biometricRepository.findTopByDeviceIdOrderByDataCaptura24hDesc(deviceId);

        if (lastCapture == null || lastCapture.getMetadadosDigital() == null ||
                latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0) ||
                (lastCapture.getMetadadosDigital().getLatitude() == 0.0 && lastCapture.getMetadadosDigital().getLongitude() == 0.0)) {
            return "Sem histórico ou coordenadas indisponíveis";
        }

        double lat1 = lastCapture.getMetadadosDigital().getLatitude();
        double lon1 = lastCapture.getMetadadosDigital().getLongitude();
        double lat2 = latitude;
        double lon2 = longitude;
        LocalDateTime lastTime = LocalDateTime.parse(lastCapture.getDataCaptura());

        double distance = calculateHaversineDistance(lat1, lon1, lat2, lon2);
        double timeDiffHours = java.time.Duration.between(lastTime, currentTime).toSeconds() / 3600.0;
        double speedKmH = (timeDiffHours > MIN_TIME_DIFF_HOURS) ? distance / timeDiffHours : 0.0;

        logger.debug("Distância: {} km, Tempo: {} h, Velocidade: {} km/h", distance, timeDiffHours, speedKmH);

        if (FORCE_SUSPICIOUS_MOVEMENT) {
            return String.format("Movimento suspeito (forçado para teste, distância %.2f km, velocidade %.2f km/h)", distance, speedKmH);
        }

        if (timeDiffHours <= (1.0 / 3600.0) && distance > 1.0) {
            return String.format("Movimento suspeito (distância %.2f km em %.2f segundos)", distance, timeDiffHours * 3600);
        }

        if (speedKmH > MAX_SPEED_KMH) {
            return String.format("Movimento suspeito (velocidade %.2f km/h > %.2f km/h)", speedKmH, MAX_SPEED_KMH);
        }

        return String.format("Movimento normal (distância %.2f km, velocidade %.2f km/h)", distance, speedKmH);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String validateDeviceConsistency(String deviceId, Device currentDevice) {
        BiometricModel lastCapture = biometricRepository.findTopByDeviceIdOrderByDataCaptura24hDesc(deviceId);
        if (lastCapture == null || lastCapture.getDispositivo() == null) {
            return "Sem histórico de dispositivo";
        }

        Device lastDevice = lastCapture.getDispositivo();

        if (FORCE_DEVICE_INCONSISTENCY) {
            return String.format("Dispositivo inconsistente (forçado para teste, Anterior: %s %s %s, Atual: %s %s %s)",
                    lastDevice.getFabricante(), lastDevice.getModelo(), lastDevice.getSistemaOperacional(),
                    currentDevice.getFabricante(), currentDevice.getModelo(), currentDevice.getSistemaOperacional());
        }

        boolean isConsistent = lastDevice.getFabricante().equals(currentDevice.getFabricante()) &&
                lastDevice.getModelo().equals(currentDevice.getModelo()) &&
                lastDevice.getSistemaOperacional().equals(currentDevice.getSistemaOperacional());

        if (!isConsistent) {
            return String.format("Dispositivo inconsistente (Anterior: %s %s %s, Atual: %s %s %s)",
                    lastDevice.getFabricante(), lastDevice.getModelo(), lastDevice.getSistemaOperacional(),
                    currentDevice.getFabricante(), currentDevice.getModelo(), currentDevice.getSistemaOperacional());
        }
        return "Dispositivo consistente";
    }

    private void sendToExternalApi(BiometricModel savedModel) {
        try {
            String json = objectMapper.writeValueAsString(savedModel);
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
