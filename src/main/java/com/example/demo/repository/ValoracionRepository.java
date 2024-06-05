package com.example.demo.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.Valoracion;

@Repository("valoracionRepository")
public interface ValoracionRepository extends CrudRepository<Valoracion, Integer> {
	 Iterable<Valoracion> findByConductorId(int conductorId);
	  List<Valoracion> findByPasajeroAndConductorAndGrupo(Usuario pasajero, Usuario conductor, Grupo grupo);
	 
	  List<Valoracion> findByConductor(Usuario conductor);
}
