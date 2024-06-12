package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Mensaje;
import com.example.demo.entity.Usuario;
import com.example.demo.entityDTO.MensajeDTO;
import com.example.demo.entityDTO.UsuarioDTO;
import com.example.demo.service.MensajeService;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api")
public class ChatController {
	@Autowired
	private MensajeService mensajeService;
	@Autowired
	private UsuarioService usuarioService;

	@PostMapping("/enviar-mensaje")
    public ResponseEntity<String> enviarMensaje(@RequestParam("idReceptor") int idReceptor,
                                                @RequestParam("mensaje") String mensaje) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return mensajeService.enviarElMensaje(username, idReceptor, mensaje);
    }

	@GetMapping("/ver-mensajes")
    public ResponseEntity<List<MensajeDTO>> verMensajes(@RequestParam("idEmisor") int idEmisor,
                                                        @RequestParam("idReceptor") int idReceptor) {
        return mensajeService.verMensajes(idEmisor, idReceptor);
    }

	@GetMapping("/chats")
	public ResponseEntity<List<UsuarioDTO>> misReceptores() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuarioAutenticado = usuarioService.findByUsername(username);

		List<UsuarioDTO> usuarios = mensajeService.obtenerUsuariosReceptoresPorIdEmisor(usuarioAutenticado.getId());

		return ResponseEntity.ok(usuarios);
	}

	@GetMapping("/noleido")
	public int countUnreadMessages() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuarioAutenticado = usuarioService.findByUsername(username);
		return mensajeService.countUnreadMessages(usuarioAutenticado.getId());
	}

	@GetMapping("/usuarios-noleidos")
	public ResponseEntity<Integer> obtenerConteoMensajesNoLeidosDeUsuario(@RequestParam("idEmisor") int idEmisor) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuarioAutenticado = usuarioService.findByUsername(username);

		int count = mensajeService.contarMensajesNoLeidosDeUsuario(usuarioAutenticado.getId(), idEmisor);

		return ResponseEntity.ok(count);
	}

	@PostMapping("/marcar-leidos")
	public ResponseEntity<String> marcarMensajesComoLeidos(@RequestParam("idEmisor") int idEmisor) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = usuarioService.findByUsername(username);

		if (usuario != null) {
			mensajeService.marcarMensajesComoLeidos(idEmisor, usuario.getId());
			return ResponseEntity.ok("Mensajes marcados como leídos correctamente");
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autorizado");
		}
	}

}
