package br.com.fiap.challenge_quod.challenge_quod.dto;

public class BiometricResponseDTO {
    private String status; // "success", "failure", "fraud_detected"
    private String message;
    private boolean registered;
    private String analysisReport;

    public BiometricResponseDTO() {}

    public BiometricResponseDTO(String status, String message, boolean registered, String analysisReport) {
        this.status = status;
        this.message = message;
        this.registered = registered;
        this.analysisReport = analysisReport;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRegistered() { return registered; }
    public void setRegistered(boolean registered) { this.registered = registered; }
    public String getAnalysisReport() { return analysisReport; }
    public void setAnalysisReport(String analysisReport) { this.analysisReport = analysisReport; }
}
