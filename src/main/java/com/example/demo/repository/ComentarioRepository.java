package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Comentario;
import com.example.demo.entity.Grupo;
import com.example.demo.entity.Usuario;

@Repository("comentarioRepository")
public interface ComentarioRepository extends CrudRepository<Comentario, Integer> {
	@Query("SELECT new com.example.demo.entity.Comentario(c.pasajero, c.comentario, c.conductor) FROM Comentario c WHERE c.conductor.id = :conductorId")
	List<Comentario> findComentariosAndPasajeroByConductorId(int conductorId);
	 List<Comentario> findByPasajeroAndConductorAndGrupo(Usuario pasajero, Usuario conductor, Grupo grupo);


}
