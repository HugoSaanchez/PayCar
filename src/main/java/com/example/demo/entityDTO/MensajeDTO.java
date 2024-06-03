package com.example.demo.entityDTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MensajeDTO {
	private int idEmisor;
	private int idReceptor;
    private String mensaje;
    private LocalDateTime hora;


}