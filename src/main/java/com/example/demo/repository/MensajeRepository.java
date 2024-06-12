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

	    List<Mensaje> findByReceptorId(int receptorId);
	    
	    @Query("SELECT DISTINCT m.emisor FROM Mensaje m WHERE m.receptor.id = :idReceptor")
	    List<Usuario> findDistinctEmisoresByReceptorId(int idReceptor);
	    
	    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.receptor.id = :receptorId AND m.leido = false")
	    int countUnreadMessagesByReceptorId(int receptorId);
	 
	    
	    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.receptor.id = :idReceptor AND m.emisor.id = :idEmisor AND m.leido = false")
	    int countUnreadMessagesFromUser(int idReceptor, int idEmisor);
	    
	    List<Mensaje> findByEmisorIdAndReceptorIdAndLeidoFalse(int emisorId, int receptorId);
	
	
	
}
