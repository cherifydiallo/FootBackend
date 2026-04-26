package com.kratos.footbackend.dto;

public class CreateAcademyDto {

    private String academyName;
    private String localite;
    private String numeroTelephone;
    private String description;

    public String getAcademyName() { return academyName; }
    public void setAcademyName(String academyName) { this.academyName = academyName; }

    public String getLocalite() { return localite; }
    public void setLocalite(String localite) { this.localite = localite; }

    public String getNumeroTelephone() { return numeroTelephone; }
    public void setNumeroTelephone(String numeroTelephone) { this.numeroTelephone = numeroTelephone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
