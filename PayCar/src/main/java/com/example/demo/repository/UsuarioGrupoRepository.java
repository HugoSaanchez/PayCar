package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UsuarioGrupo;

@Repository("usuarioGrupoRepository")
public interface UsuarioGrupoRepository  extends JpaRepository<UsuarioGrupo, Serializable> {
	
	 List<UsuarioGrupo> findByGrupoId(int grupoId);
}

