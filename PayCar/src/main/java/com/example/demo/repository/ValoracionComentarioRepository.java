package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.ValoracionComentario;

@Repository("valoracionComentarioRepository")
public interface ValoracionComentarioRepository extends CrudRepository<ValoracionComentario, Integer> {
	 Iterable<ValoracionComentario> findByConductorId(int conductorId);
	 @Query("SELECT vc.comentario FROM ValoracionComentario vc WHERE vc.conductor.id = :conductorId")
	 List<String> findComentarioByConductorId(@Param("conductorId") int conductorId);
	
}
