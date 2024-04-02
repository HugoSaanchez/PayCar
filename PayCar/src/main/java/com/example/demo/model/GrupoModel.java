package com.example.demo.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoModel {

	@Id
	private int id;
	private String titulo;
	private String descripcion;
	private float consumoGasolina;
	private int kilometrosRecorridos;
	private float dineroGasolina;
	private boolean activado;
	private boolean borrado;
}
