package com.example.demo.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Valoracion;

@Repository("valoracionRepository")
public interface ValoracionRepository extends CrudRepository<Valoracion, Integer> {
	 Iterable<Valoracion> findByConductorId(int conductorId);

	
}
