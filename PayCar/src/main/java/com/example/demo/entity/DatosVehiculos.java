package com.example.demo.entity;

import java.sql.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class DatosVehiculos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String version;

    @Column
    private String mixto;

    @Column(nullable = false)
    private Boolean alquilado = false;

    @Column
    private Date fecha_inicio;

    @Column
    private Date fecha_fin;
}
