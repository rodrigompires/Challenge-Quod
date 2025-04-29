package br.com.fiap.challenge_quod.challenge_quod.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class DocumentRequestDTO {
    @NotBlank(message = "O tipo de documento é obrigatório")
    private String documentType;

    @NotNull(message = "A imagem da frente é obrigatória")
    private MultipartFile frontImage;

    @NotNull(message = "A imagem do verso é obrigatória")
    private MultipartFile backImage;

    private Double latitudeFront;
    private Double longitudeFront;
    private Double latitudeBack;
    private Double longitudeBack;
    private String androidVersion;
    private Integer apiLevel;
    private String manufacturer;
    private String model;
    private String captureDate;

    // Construtor padrão
    public DocumentRequestDTO() {}

    // Getters
    public String getDocumentType() { return documentType; }
    public MultipartFile getFrontImage() { return frontImage; }
    public MultipartFile getBackImage() { return backImage; }
    public Double getLatitudeFront() { return latitudeFront; }
    public Double getLongitudeFront() { return longitudeFront; }
    public Double getLatitudeBack() { return latitudeBack; }
    public Double getLongitudeBack() { return longitudeBack; }
    public String getAndroidVersion() { return androidVersion; }
    public Integer getApiLevel() { return apiLevel; }
    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public String getCaptureDate() { return captureDate; }

    // Setters
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public void setFrontImage(MultipartFile frontImage) { this.frontImage = frontImage; }
    public void setBackImage(MultipartFile backImage) { this.backImage = backImage; }
    public void setLatitudeFront(Double latitudeFront) { this.latitudeFront = latitudeFront; }
    public void setLongitudeFront(Double longitudeFront) { this.longitudeFront = longitudeFront; }
    public void setLatitudeBack(Double latitudeBack) { this.latitudeBack = latitudeBack; }
    public void setLongitudeBack(Double longitudeBack) { this.longitudeBack = longitudeBack; }
    public void setAndroidVersion(String androidVersion) { this.androidVersion = androidVersion; }
    public void setApiLevel(Integer apiLevel) { this.apiLevel = apiLevel; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public void setModel(String model) { this.model = model; }
    public void setCaptureDate(String captureDate) { this.captureDate = captureDate; }
}