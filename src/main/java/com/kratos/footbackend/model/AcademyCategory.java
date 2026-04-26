package com.kratos.footbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "academy_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"academy_id", "name"}))
public class AcademyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id", nullable = false)
    private Academy academy;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Academy getAcademy() { return academy; }
    public void setAcademy(Academy academy) { this.academy = academy; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
