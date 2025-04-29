package br.com.fiap.challenge_quod.challenge_quod.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.List;

@Document(collection = "Documentoscopia")
public class IdentityDocument {

    @Id
    private String transacaoId;
    private String tipoBiometria;
    private String tipoFraude;
    private int fraudeStatus;
    private String dataCaptura;
    private String dataCaptura24h;
    private Device dispositivo;
    private List<String> canalNotificacao;
    private String notificadoPor;
    private Metadata metadadosImagem1;
    private Metadata metadadosImagem2;

    public IdentityDocument(String transacaoId, String tipoBiometria, String tipoFraude, String dataCaptura,
                            String dataCaptura24h, Device dispositivo, List<String> canalNotificacao, String notificadoPor,
                            Metadata metadadosImagem1, Metadata metadadosImagem2) {
        this.transacaoId = transacaoId;
        this.tipoBiometria = "Documentoscopia";
        this.tipoFraude = tipoFraude;
        this.dataCaptura = dataCaptura;
        this.dataCaptura24h = dataCaptura24h;
        this.dispositivo = dispositivo;
        this.canalNotificacao = canalNotificacao != null ? canalNotificacao : Arrays.asList("sms", "email");
        this.notificadoPor = notificadoPor;
        this.metadadosImagem1 = metadadosImagem1;
        this.metadadosImagem2 = metadadosImagem2;
    }

    // Getters e Setters
    public String getTransacaoId() { return transacaoId; }
    public void setTransacaoId(String transacaoId) { this.transacaoId = transacaoId; }
    public String getTipoBiometria() { return tipoBiometria; }
    public void setTipoBiometria(String tipoBiometria) { this.tipoBiometria = tipoBiometria; }
    public String getTipoFraude() { return tipoFraude; }
    public void setTipoFraude(String tipoFraude) { this.tipoFraude = tipoFraude; }
    public int getFraudeStatus() { return fraudeStatus; }
    public void setFraudeStatus(int fraudStatus) { this.fraudeStatus = fraudStatus; }
    public String getDataCaptura() { return dataCaptura; }
    public void setDataCaptura(String dataCaptura) { this.dataCaptura = dataCaptura; }
    public String getDataCaptura24h() { return dataCaptura24h; }
    public void setDataCaptura24h(String dataCaptura24h) { this.dataCaptura24h = dataCaptura24h; }
    public Device getDispositivo() { return dispositivo; }
    public void setDispositivo(Device dispositivo) { this.dispositivo = dispositivo; }
    public List<String> getCanalNotificacao() { return canalNotificacao; }
    public void setCanalNotificacao(List<String> canalNotificacao) { this.canalNotificacao = canalNotificacao; }
    public String getNotificadoPor() { return notificadoPor; }
    public void setNotificadoPor(String notificadoPor) { this.notificadoPor = notificadoPor; }
    public Metadata getMetadadosImagem1() { return metadadosImagem1; }
    public void setMetadadosImagem1(Metadata metadadosImagem1) { this.metadadosImagem1 = metadadosImagem1; }
    public Metadata getMetadadosImagem2() { return metadadosImagem2; }
    public void setMetadadosImagem2(Metadata metadadosImagem2) { this.metadadosImagem2 = metadadosImagem2; }

    @Override
    public String toString() {
        return "IdentityDocument{" +
                "transacaoId='" + transacaoId + '\'' +
                ", tipoBiometria='" + tipoBiometria + '\'' +
                ", tipoFraude='" + tipoFraude + '\'' +
                ", dataCaptura='" + dataCaptura + '\'' +
                ", dataCaptura24h='" + dataCaptura24h + '\'' +
                ", dispositivo=" + dispositivo +
                ", canalNotificacao=" + canalNotificacao +
                ", notificadoPor='" + notificadoPor + '\'' +
                ", metadadosImagem1=" + metadadosImagem1 +
                ", metadadosImagem2=" + metadadosImagem2 +
                '}';
    }
}