package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Comentario;
import com.example.demo.entity.Usuario;

public interface AdminService {

	public abstract List<Usuario> findByRol(String rol);

	public abstract void borrarUsuario(int id);

	public abstract void activarUsuario(int id);

	public abstract List<Usuario> findByEstado(String rol);

	public double obtenerMediaValoracionConductor(int idConductor);


	public List<Usuario> ordenarUsuariosPorValoracion(List<Usuario> usuarios, String orden);
	
	public List<Comentario> encontrarComentariosPorIdConductor(int idConductor);
	 
	
	

}
