package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Grupo;


@Repository("grupoRepository")
public interface GrupoRepository  extends JpaRepository<Grupo, Serializable> {

	
	public abstract List<Grupo> findByActivado(boolean activado);
	
}
