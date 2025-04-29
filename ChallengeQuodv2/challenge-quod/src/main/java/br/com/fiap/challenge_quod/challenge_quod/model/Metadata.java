package br.com.fiap.challenge_quod.challenge_quod.model;

public class Metadata {
    private double latitude;
    private double longitude;
    private String ipOrigem;
    private String fileName;
    private String imagemId;

    public Metadata() {}

    public Metadata(double latitude, double longitude, String ipOrigem, String fileName, String imagemId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.ipOrigem = ipOrigem;
        this.fileName = fileName;
        this.imagemId = imagemId;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getIpOrigem() { return ipOrigem; }
    public void setIpOrigem(String ipOrigem) { this.ipOrigem = ipOrigem; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getImagemId() { return imagemId; }
    public void setImagemId(String imagemId) { this.imagemId = imagemId; }

    @Override
    public String toString() {
        return "Metadados{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", ipOrigem='" + ipOrigem + '\'' +
                ", fileName='" + fileName + '\'' +
                ", imagemId='" + imagemId + '\'' +
                '}';
    }
}