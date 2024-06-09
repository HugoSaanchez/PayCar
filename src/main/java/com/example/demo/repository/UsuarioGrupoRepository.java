package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Usuario;
import com.example.demo.entity.UsuarioGrupo;

@Repository("usuarioGrupoRepository")
public interface UsuarioGrupoRepository  extends JpaRepository<UsuarioGrupo, Serializable> {
    List<UsuarioGrupo> findByUsuario(Usuario usuario);
	 List<UsuarioGrupo> findByGrupoId(int grupoId);
	  Optional<UsuarioGrupo> findByUsuarioIdAndGrupoId(int usuarioId, int grupoId);
	  List<UsuarioGrupo> findByUsuarioId(int usuarioId);
	  Optional<UsuarioGrupo> findByUsuarioAndGrupoId(Usuario usuario, int grupoId);
}

