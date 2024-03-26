package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Data;

@Data
@Entity
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
    
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "usuario_grupo",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> usuarios;
}
