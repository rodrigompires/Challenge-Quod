package br.com.fiap.challenge_quod.challenge_quod.dto;

public class BiometricRequestDTO {
    private boolean authenticated;
    private int failedAttempts;
    private String deviceId;
    private Double latitude;
    private Double longitude;
    private String androidVersion;
    private Integer apiLevel;
    private String manufacturer;
    private String model;
    private String captureDate;

    public BiometricRequestDTO() {}

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getAndroidVersion() { return androidVersion; }
    public void setAndroidVersion(String androidVersion) { this.androidVersion = androidVersion; }
    public Integer getApiLevel() { return apiLevel; }
    public void setApiLevel(Integer apiLevel) { this.apiLevel = apiLevel; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getCaptureDate() { return captureDate; }
    public void setCaptureDate(String captureDate) { this.captureDate = captureDate; }
}
