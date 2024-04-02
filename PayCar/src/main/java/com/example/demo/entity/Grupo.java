package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue
    private int id;
    @Column(nullable = false)
    private String titulo;

    private String descripcion;
    @Column(nullable = true)
    private float consumoGasolina;
    @Column(nullable = true)
    private int kilometrosRecorridos;
    @Column(nullable = true)
    private float dineroGasolina;
    private boolean activado;
    private boolean borrado;
    
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsuarioGrupo> usuarios;
}
