package br.com.fiap.challenge_quod.challenge_quod.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "Digital")
public class BiometricModel {

    @Id
    private String id;
    private String deviceId;
    private String tipoBiometria = "Digital";
    private String tipoFraude;
    private int fraudeStatus;
    private String dataCaptura; // String em vez de LocalDateTime
    private String dataCaptura24h;
    private Device dispositivo;
    private List<String> canalNotificacao;
    private String notificadoPor;
    private Metadata metadadosDigital;
    private String analysisReport;
//    private Metadata metadadosImagem2;
    private String _class = "br.com.fiap.challenge_quod.challenge_quod.model.BiometricModel";

    public BiometricModel() {}

    public BiometricModel(String id, String deviceId, String tipoFraude, String dataCaptura, String dataCaptura24h,
                          Device dispositivo, List<String> canalNotificacao, String notificadoPor,
                          Metadata metadadosDigital) {
        this.id = id;
        this.deviceId = deviceId;
        this.tipoFraude = tipoFraude;
        this.dataCaptura = dataCaptura;
        this.dataCaptura24h = dataCaptura24h;
        this.dispositivo = dispositivo;
        this.canalNotificacao = canalNotificacao;
        this.notificadoPor = notificadoPor;
        this.metadadosDigital = metadadosDigital;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDeviceId() { return deviceId; } // Novo getter
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
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
    public Metadata getMetadadosDigital() { return metadadosDigital; }
    public void setMetadadosDigital(Metadata metadadosDigital) { this.metadadosDigital = metadadosDigital; }
    public String getAnalysisReport() { return analysisReport; }
    public void setAnalysisReport(String analysisReport) { this.analysisReport = analysisReport; }
//    public Metadata getMetadadosImagem2() { return metadadosImagem2; }
//    public void setMetadadosImagem2(Metadata metadadosImagem2) { this.metadadosImagem2 = metadadosImagem2; }
    public String get_class() { return _class; }
    public void set_class(String _class) { this._class = _class; }
}
