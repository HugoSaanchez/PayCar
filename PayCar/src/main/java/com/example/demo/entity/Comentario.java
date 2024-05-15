package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Comentario {
	@Id
	@GeneratedValue
	private int id;

	@ManyToOne
	private Usuario pasajero;

	@ManyToOne
	private Usuario conductor;


	private String comentario;
	
	public Comentario(Usuario pasajero, String comentario, Usuario conductor) {
	    this.pasajero = pasajero;
	    this.comentario = comentario;
	    this.conductor = conductor;
	}


}
