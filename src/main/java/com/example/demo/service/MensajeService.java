package com.example.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.example.demo.entity.Mensaje;
import com.example.demo.entityDTO.MensajeDTO;
import com.example.demo.entityDTO.UsuarioDTO;

public interface MensajeService {


	public List<Mensaje> obtenerMensajesEntreEmisorYReceptor(int idEmisor, int idReceptor);

	public List<Integer> obtenerIdsReceptoresPorIdEmisor(int idEmisor);

	public List<String> obtenerNombresReceptoresPorIdEmisor(int idEmisor);

	public List<UsuarioDTO> obtenerUsuariosReceptoresPorIdEmisor(int idEmisor);

	List<UsuarioDTO> obtenerUsuariosEmisoresPorIdReceptor(int idReceptor);

	public int countUnreadMessages(int idReceptor);

	int contarMensajesNoLeidosDeUsuario(int idReceptor, int idEmisor);

	public void marcarMensajesComoLeidos(int idEmisor, int idReceptor);
	
	  ResponseEntity<String> enviarElMensaje(String username, int idReceptor, String mensaje);
	  
	    ResponseEntity<List<MensajeDTO>> verMensajes(int idEmisor, int idReceptor);
}
