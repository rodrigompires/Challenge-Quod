package br.com.fiap.challenge_quod.challenge_quod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class FacialBiometricRequestDTO {
    @NotNull(message = "A primeira imagem é obrigatória")
    private MultipartFile image1;

    @NotNull(message = "A segunda imagem é obrigatória")
    private MultipartFile image2;

    @NotBlank(message = "A latitude da primeira imagem é obrigatória")
    private String latitude1;

    @NotBlank(message = "A longitude da primeira imagem é obrigatória")
    private String longitude1;

    @NotBlank(message = "A latitude da segunda imagem é obrigatória")
    private String latitude2;

    @NotBlank(message = "A longitude da segunda imagem é obrigatória")
    private String longitude2;

    @NotBlank(message = "A versão do Android é obrigatória")
    private String androidVersion;

    @NotBlank(message = "O nível da API é obrigatório")
    private String apiLevel;

    @NotBlank(message = "O fabricante é obrigatório")
    private String manufacturer;

    @NotBlank(message = "O modelo é obrigatório")
    private String model;

    @NotBlank(message = "A data de captura é obrigatória")
    private String captureDate;

    private boolean testMode; // Opcional, default false

    // Getters e Setters manuais
    public MultipartFile getImage1() { return image1; }
    public void setImage1(MultipartFile image1) { this.image1 = image1; }

    public MultipartFile getImage2() { return image2; }
    public void setImage2(MultipartFile image2) { this.image2 = image2; }

    public String getLatitude1() { return latitude1; }
    public void setLatitude1(String latitude1) { this.latitude1 = latitude1; }

    public String getLongitude1() { return longitude1; }
    public void setLongitude1(String longitude1) { this.longitude1 = longitude1; }

    public String getLatitude2() { return latitude2; }
    public void setLatitude2(String latitude2) { this.latitude2 = latitude2; }

    public String getLongitude2() { return longitude2; }
    public void setLongitude2(String longitude2) { this.longitude2 = longitude2; }

    public String getAndroidVersion() { return androidVersion; }
    public void setAndroidVersion(String androidVersion) { this.androidVersion = androidVersion; }

    public String getApiLevel() { return apiLevel; }
    public void setApiLevel(String apiLevel) { this.apiLevel = apiLevel; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getCaptureDate() { return captureDate; }
    public void setCaptureDate(String captureDate) { this.captureDate = captureDate; }

    public boolean isTestMode() { return testMode; }
    public void setTestMode(boolean testMode) { this.testMode = testMode; }
}