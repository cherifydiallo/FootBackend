package com.kratos.footbackend.dto;

import java.time.LocalDate;

public class CreatePlayerDto {

    private String fullName;
    private LocalDate birthDate;
    private String academy;
    private String category;
    private String registerNumber;
    private Integer heightCm;
    private Integer weightKg;
    private String fatherName;
    private String motherName;
    private String photo;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAcademy() { return academy; }
    public void setAcademy(String academy) { this.academy = academy; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}
