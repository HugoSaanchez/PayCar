package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioModel {
	@Id
	private int id;
	private String nombre;
	private String username;
	private String password;
	private String rol;
	private boolean activado = false; 
	private boolean borrado = false;

}
