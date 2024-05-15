package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Mensaje;
import com.example.demo.entity.Usuario;
import com.example.demo.entityDTO.MensajeDTO;
import com.example.demo.service.MensajeService;
import com.example.demo.service.UsuarioService;

@RestController
public class ChatController {
	 @Autowired
	    private MensajeService mensajeService;
	 @Autowired
	    private UsuarioService usuarioService;
	
	 @PostMapping("/enviar-mensaje")
	    public ResponseEntity<String> enviarMensaje(
	            @RequestParam("idReceptor") int idReceptor,
	            @RequestParam("mensaje") String mensaje) {
		   String username = SecurityContextHolder.getContext().getAuthentication().getName();
		   
		   System.out.println(username);
		    Usuario usuario = usuarioService.findByUsername(username);
	
		    if(usuario!=null) {
	        mensajeService.enviarMensaje(usuario.getId(), idReceptor, mensaje);
	        return ResponseEntity.ok("Mensaje enviado correctamente");
		    }
		    else
		    	 return null;
	    }
	 
	 @GetMapping("/ver-mensajes")
	    public ResponseEntity<List<MensajeDTO>> verMensajes(
	            @RequestParam("idEmisor") int idEmisor,
	            @RequestParam("idReceptor") int idReceptor) {
	        // Obtener todos los mensajes del emisor
	        List<Mensaje> mensajesEmisor = mensajeService.obtenerMensajesEntreEmisorYReceptor(idEmisor, idReceptor);
	        
	        // Obtener todos los mensajes del receptor
	        List<Mensaje> mensajesReceptor = mensajeService.obtenerMensajesEntreEmisorYReceptor(idReceptor, idEmisor);
	        
	        // Combinar los mensajes del emisor y del receptor en una lista
	        List<Mensaje> todosMensajes = new ArrayList<>(mensajesEmisor);
	        todosMensajes.addAll(mensajesReceptor);
	        
	        // Ordenar los mensajes por hora
	        todosMensajes.sort(Comparator.comparing(Mensaje::getHora));
	        
	        // Convertir los mensajes a DTOs
	        List<MensajeDTO> mensajesDTO = new ArrayList<>();
	        for (Mensaje mensaje : todosMensajes) {
	            MensajeDTO mensajeDTO = new MensajeDTO();
	            mensajeDTO.setIdEmisor(mensaje.getEmisor().getId());
	            mensajeDTO.setIdReceptor(mensaje.getReceptor().getId() );
	            mensajeDTO.setMensaje(mensaje.getMensaje());
	            mensajeDTO.setHora(mensaje.getHora());
	            mensajesDTO.add(mensajeDTO);
	        }
	        
	        // Devolver los mensajes DTO en la respuesta
	        return ResponseEntity.ok(mensajesDTO);
	    }
	 
	 @GetMapping("/chats")
	 public List<String> misReceptores() {
	     String username = SecurityContextHolder.getContext().getAuthentication().getName();
	     Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	     List<String> nombres = mensajeService.obtenerNombresReceptoresPorIdEmisor(usuarioAutenticado.getId());
	     System.out.println("Nombres de Receptores para el usuario " + username + ": " + nombres);

	     return nombres;
	 }

}

