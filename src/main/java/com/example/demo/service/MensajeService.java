package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Mensaje;
import com.example.demo.entityDTO.UsuarioDTO;
import com.example.demo.entityDTO.UsuarioMensajeDTO;

public interface MensajeService {
	
	public Mensaje enviarMensaje(int idEmisor, int idReceptor, String contenidoMensaje);
	  public List<Mensaje> obtenerMensajesEntreEmisorYReceptor(int idEmisor, int idReceptor);

	    public List<Integer> obtenerIdsReceptoresPorIdEmisor(int idEmisor);
	    public List<String> obtenerNombresReceptoresPorIdEmisor(int idEmisor);
	    public List<UsuarioDTO> obtenerUsuariosReceptoresPorIdEmisor(int idEmisor);
	    List<UsuarioDTO> obtenerUsuariosEmisoresPorIdReceptor(int idReceptor);
	    public int countUnreadMessages(int idReceptor);
	  

	    int contarMensajesNoLeidosDeUsuario(int idReceptor, int idEmisor);
	    public void marcarMensajesComoLeidos(int idEmisor, int idReceptor);
}
