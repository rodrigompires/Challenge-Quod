package br.com.fiap.challenge_quod.challenge_quod.controller;

import br.com.fiap.challenge_quod.challenge_quod.dto.BiometricRequestDTO;
import br.com.fiap.challenge_quod.challenge_quod.dto.BiometricResponseDTO;
import br.com.fiap.challenge_quod.challenge_quod.service.BiometricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/biometric")
public class BiometricController {

    private final BiometricService biometricService;

    public BiometricController(BiometricService biometricService) {
        this.biometricService = biometricService;
    }

    @PostMapping(value = "/validate", consumes = "multipart/form-data")
    public ResponseEntity<BiometricResponseDTO> validateBiometric(
            @RequestPart    ("authenticated") String authenticated,
            @RequestPart("failedAttempts") String failedAttempts,
            @RequestPart("deviceId") String deviceId,
            @RequestPart(value = "latitude", required = false) String latitude,
            @RequestPart(value = "longitude", required = false) String longitude,
            @RequestPart(value = "androidVersion", required = false) String androidVersion,
            @RequestPart(value = "apiLevel", required = false) String apiLevel,
            @RequestPart(value = "manufacturer", required = false) String manufacturer,
            @RequestPart(value = "model", required = false) String model,
            @RequestPart(value = "captureDate", required = false) String captureDate
    ) {
        BiometricRequestDTO request = new BiometricRequestDTO();
        request.setAuthenticated(Boolean.parseBoolean(authenticated));
        request.setFailedAttempts(Integer.parseInt(failedAttempts));
        request.setDeviceId(deviceId);
        request.setLatitude(latitude != null ? Double.parseDouble(latitude) : null);
        request.setLongitude(longitude != null ? Double.parseDouble(longitude) : null);
        request.setAndroidVersion(androidVersion);
        request.setApiLevel(apiLevel != null ? Integer.parseInt(apiLevel) : null);
        request.setManufacturer(manufacturer);
        request.setModel(model);
        request.setCaptureDate(captureDate);

        BiometricResponseDTO response = biometricService.validateBiometric(request);
        return ResponseEntity.ok(response);
    }
}
