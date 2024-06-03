package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Usuario;

@Repository("usuarioRepository")
public interface UsuarioRepository extends JpaRepository<Usuario, Serializable> {
	public abstract Usuario findByUsername(String username);
	public abstract Usuario findById(int id);

	public abstract List<Usuario> findByRol(String rol);
	
	public abstract List<Usuario> findByRolAndActivado(String rol,boolean activado);
	

}
