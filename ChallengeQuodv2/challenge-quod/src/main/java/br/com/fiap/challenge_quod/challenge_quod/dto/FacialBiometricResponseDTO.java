package br.com.fiap.challenge_quod.challenge_quod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacialBiometricResponseDTO {

    @JsonProperty("message")
    private String message;

    @JsonProperty("fraudType")
    private String fraudType;

    @JsonProperty("similarityScore")
    private Double similarityScore;

    @JsonProperty("areCoordinatesEqual")
    private Boolean areCoordinatesEqual;

    @JsonProperty("euclideanDistance")
    private Double euclideanDistance;

    @JsonProperty("deviceInfo")
    private String deviceInfo;

    public FacialBiometricResponseDTO() {
    }

    public FacialBiometricResponseDTO(String message, String fraudType, Double similarityScore,
                                      Boolean areCoordinatesEqual, Double euclideanDistance, String deviceInfo) {
        this.message = message;
        this.fraudType = fraudType;
        this.similarityScore = similarityScore != null ? similarityScore : 0.0;
        this.areCoordinatesEqual = areCoordinatesEqual;
        this.euclideanDistance = euclideanDistance != null ? euclideanDistance : 0.0;
        this.deviceInfo = deviceInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFraudType() {
        return fraudType;
    }

    public void setFraudType(String fraudType) {
        this.fraudType = fraudType;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore != null ? similarityScore : 0.0;
    }

    public Boolean getAreCoordinatesEqual() {
        return areCoordinatesEqual;
    }

    public void setAreCoordinatesEqual(Boolean areCoordinatesEqual) {
        this.areCoordinatesEqual = areCoordinatesEqual;
    }

    public Double getEuclideanDistance() {
        return euclideanDistance;
    }

    public void setEuclideanDistance(Double euclideanDistance) {
        this.euclideanDistance = euclideanDistance != null ? euclideanDistance : 0.0;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}