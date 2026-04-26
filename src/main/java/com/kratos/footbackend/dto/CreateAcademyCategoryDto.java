package com.kratos.footbackend.dto;

public class CreateAcademyCategoryDto {

    private Long academyId;
    private String name;

    public Long getAcademyId() { return academyId; }
    public void setAcademyId(Long academyId) { this.academyId = academyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
