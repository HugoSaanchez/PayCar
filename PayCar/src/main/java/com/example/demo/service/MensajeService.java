package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Mensaje;
import com.example.demo.entity.Usuario;

public interface MensajeService {
	
	public Mensaje enviarMensaje(int idEmisor, int idReceptor, String contenidoMensaje);
	  public List<Mensaje> obtenerMensajesEntreEmisorYReceptor(int idEmisor, int idReceptor);

	    public List<Integer> obtenerIdsReceptoresPorIdEmisor(int idEmisor);
	    public List<String> obtenerNombresReceptoresPorIdEmisor(int idEmisor);
}
