package com.example.demo.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Mensaje;
import com.example.demo.entity.Usuario;

@Repository("mensajeRepository")
public interface MensajeRepository extends JpaRepository<Mensaje, Serializable> {
	 List<Mensaje> findByEmisorIdAndReceptorId(int idEmisor, int idReceptor);
	 
	   @Query("SELECT DISTINCT m.receptor.id FROM Mensaje m WHERE m.emisor.id = :idEmisor")
	    List<Integer> findDistinctReceptorIdsByEmisorId(int idEmisor);
	   
	   @Query("SELECT u.nombre FROM Usuario u WHERE u.id IN (SELECT DISTINCT m.receptor.id FROM Mensaje m WHERE m.emisor.id = :idEmisor)")
	   List<String> findDistinctReceptorNamesByEmisorId(int idEmisor);


	    List<Mensaje> findByEmisorId(int emisorId);
	
}
