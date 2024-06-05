package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import org.springframework.http.ResponseEntity;

import com.example.demo.entity.Comentario;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.Valoracion;
import com.example.demo.entityDTO.ComentarioDTO;
import com.example.demo.model.UsuarioModel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UsuarioService {
	public Usuario registrar(UsuarioModel usuariomodel);

	public String findUsernameByEmail(String email);

	public Usuario findByUsername(String username);

	public String logout(HttpServletRequest request, HttpServletResponse response);

	public Usuario findUsuario(String username, String password);

	Usuario findById(int id);

	Usuario save(Usuario usuario);

	boolean isAmigo(Usuario usuario, int idAmigo);

	void actualizarListaAmigos(Usuario usuario, int idAmigoNuevo);

	ResponseEntity<String> agregarAmigo(int idAmigo);
	
	 public void confirmarAmigo( int amigoId);
	 public List<Valoracion> valorarUsuario(Usuario pasajero, Usuario conductor, int idGrupo, int valoracion);
	 
	 public List<Comentario> comentarUsuario(Usuario pasajero, Usuario conductor, int idGrupo, String comentario);

	 public Valoracion obtenerValoracion(Usuario pasajero, Usuario conductor, int idGrupo);
	 public Comentario obtenerComentario(Usuario pasajero, Usuario conductor, int idGrupo);
	 public void borrarAmigo(int idUsuario, int idAmigo);
	 public Map<String, Object> obtenerEstadisticasUsuario(int idUsuario);
	 public double obtenerMediaValoracionUsuario(int usuarioId);
	  public List<ComentarioDTO> obtenerComentariosParaConductor(int conductorId);
	  public double obtenerMediaValoracionParaConductor(int conductorId);
}
