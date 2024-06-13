package com.example.demo.entityDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
	 private int id;
	    private String nombre;
	    private String username;
	    private String rol;
	    private boolean activado;
	    private boolean borrado;
	    private String token;
}

