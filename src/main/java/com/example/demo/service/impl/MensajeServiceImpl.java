package com.example.demo.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Mensaje;
import com.example.demo.entity.Usuario;
import com.example.demo.entityDTO.UsuarioDTO;
import com.example.demo.entityDTO.UsuarioMensajeDTO;
import com.example.demo.repository.MensajeRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.MensajeService;

@Service("mensajeService")
public class MensajeServiceImpl implements MensajeService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private MensajeRepository mensajeRepository;
	
	@Override
	 public Mensaje enviarMensaje(int idEmisor, int idReceptor, String contenidoMensaje) {
	        Usuario emisor = usuarioRepository.findById(idEmisor);
	        Usuario receptor = usuarioRepository.findById(idReceptor);

	        Mensaje nuevoMensaje = new Mensaje();
	        nuevoMensaje.setEmisor(emisor);
	        nuevoMensaje.setReceptor(receptor);
	        nuevoMensaje.setMensaje(contenidoMensaje);
	        nuevoMensaje.setHora(LocalDateTime.now());

	        return mensajeRepository.save(nuevoMensaje);
	    }
	@Override
	  public List<Mensaje> obtenerMensajesEntreEmisorYReceptor(int idEmisor, int idReceptor) {
	        // Utiliza el repositorio para buscar los mensajes entre el emisor y el receptor, ordenados por ID
	        return mensajeRepository.findByEmisorIdAndReceptorId(idEmisor, idReceptor);
	    }
	@Override
	public List<Integer> obtenerIdsReceptoresPorIdEmisor(int idEmisor) {
        return mensajeRepository.findDistinctReceptorIdsByEmisorId(idEmisor);
    }
	
	@Override
	public List<String> obtenerNombresReceptoresPorIdEmisor(int idEmisor) {
	    return mensajeRepository.findDistinctReceptorNamesByEmisorId(idEmisor);
	}
	@Override
	public List<UsuarioDTO> obtenerUsuariosReceptoresPorIdEmisor(int idEmisor) {
	    List<Mensaje> mensajesEnviados = mensajeRepository.findByEmisorId(idEmisor);
	    List<Mensaje> mensajesRecibidos = mensajeRepository.findByReceptorId(idEmisor);
	    
	    List<UsuarioDTO> usuariosReceptores = mensajesEnviados.stream()
	        .map(mensaje -> new UsuarioDTO(
	            mensaje.getReceptor().getId(),
	            mensaje.getReceptor().getNombre(),
	            mensaje.getReceptor().getUsername(),
	            mensaje.getReceptor().getRol(),
	            mensaje.getReceptor().isActivado(),
	            null))
	        .distinct()
	        .collect(Collectors.toList());
	        
	    List<UsuarioDTO> usuariosEmisores = mensajesRecibidos.stream()
	        .map(mensaje -> new UsuarioDTO(
	            mensaje.getEmisor().getId(),
	            mensaje.getEmisor().getNombre(),
	            mensaje.getEmisor().getUsername(),
	            mensaje.getEmisor().getRol(),
	            mensaje.getEmisor().isActivado(),
	            null))
	        .distinct()
	        .collect(Collectors.toList());
	    
	    usuariosReceptores.addAll(usuariosEmisores);
	    
	    // Eliminar duplicados
	    List<UsuarioDTO> todosUsuarios = usuariosReceptores.stream()
	        .distinct()
	        .collect(Collectors.toList());
	    
	    return todosUsuarios;
	}

	
	   @Override
	    public List<UsuarioDTO> obtenerUsuariosEmisoresPorIdReceptor(int idReceptor) {
	        List<Usuario> emisores = mensajeRepository.findDistinctEmisoresByReceptorId(idReceptor);
	        return emisores.stream().map(this::convertToDTO).collect(Collectors.toList());
	    }
	    private UsuarioDTO convertToDTO(Usuario usuario) {
	        UsuarioDTO dto = new UsuarioDTO();
	        dto.setId(usuario.getId());
	        dto.setNombre(usuario.getNombre());
	        dto.setUsername(usuario.getUsername());
	        return dto;
	    }
	    
	    @Override
	    public int countUnreadMessages(int idReceptor) {
	        return mensajeRepository.countUnreadMessagesByReceptorId(idReceptor);
	    }
	    
	   
	    
	    @Override
	    public int contarMensajesNoLeidosDeUsuario(int idReceptor, int idEmisor) {
	        return mensajeRepository.countUnreadMessagesFromUser(idReceptor, idEmisor);
	    }
	    
	    @Override
	    public void marcarMensajesComoLeidos(int idEmisor, int idReceptor) {
	        List<Mensaje> mensajes = mensajeRepository.findByEmisorIdAndReceptorIdAndLeidoFalse(idEmisor, idReceptor);
	        for (Mensaje mensaje : mensajes) {
	            mensaje.setLeido(true);
	        }
	        mensajeRepository.saveAll(mensajes);
	    }

}
