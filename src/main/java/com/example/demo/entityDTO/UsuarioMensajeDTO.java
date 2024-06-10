package com.example.demo.entityDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioMensajeDTO {
	
	private String username;
    private int unreadMessages;
	

}
