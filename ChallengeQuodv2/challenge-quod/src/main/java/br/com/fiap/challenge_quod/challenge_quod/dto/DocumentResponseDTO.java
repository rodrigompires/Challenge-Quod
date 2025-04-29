package br.com.fiap.challenge_quod.challenge_quod.dto;

public class DocumentResponseDTO {
    private String transacaoId;
    private String documentType;
    private String message;
    private Double docScore;



    // Construtor padrão
    public DocumentResponseDTO() {}

    // Construtor com parâmetros
    public DocumentResponseDTO(String transacaoId, String documentType, String message, Double docScore) {
        this.transacaoId = transacaoId;
        this.documentType = documentType;
        this.message = message;
        this.docScore = docScore;

    }

    // Getters
    public String getTransacaoId() { return transacaoId; }
    public String getDocumentType() { return documentType; }
    public String getMessage() { return message; }


    // Setters
    public void setTransacaoId(String transacaoId) { this.transacaoId = transacaoId; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public Double getDocScore() { return docScore; }
    public void setDocScore(Double docScore) { this.docScore = docScore; }

}