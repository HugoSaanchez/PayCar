package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Comentario;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.Valoracion;
import com.example.demo.entityDTO.ComentarioDTO;
import com.example.demo.entityDTO.UsuarioDTO;
import com.example.demo.entityDTO.ValoracionDTO;
import com.example.demo.model.UsuarioModel;
import com.example.demo.service.AdminService;
import com.example.demo.service.GrupoService;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api")
public class UserApiController {
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private GrupoService grupoService;


	@Autowired
	private AuthenticationManager authenticationManager;

	 @PostMapping("/login")
	    public ResponseEntity<?> login(@RequestParam("user") String username, @RequestParam("password") String pwd) {
	        UsuarioDTO usuarioDTO = usuarioService.login(username, pwd);

	        if (usuarioDTO == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
	        }

	        return ResponseEntity.ok(usuarioDTO);
	    }

	@PostMapping("/register")
	public com.example.demo.entity.Usuario saveUser(@RequestBody UsuarioModel user) {
		return usuarioService.registrar(user);
	}


	
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Invalidar el token de usuario actual
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout successful");
    }


    @PutMapping("/agregar-amigo")
    public ResponseEntity<String> agregarAmigo(@RequestParam("idAmigo") int idAmigo) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioService.agregarAmigo(idAmigo, username);
    }

 
	
    @GetMapping("/ver-amigos")
    public ResponseEntity<?> verPosiblesAmigos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return usuarioService.verPosiblesAmigos(username);
    }
	  
    @GetMapping("/buscar-por-email")
    public ResponseEntity<UsuarioDTO> getUsuarioByEmail(@RequestParam String email) {
        return usuarioService.getUsuarioByEmail(email);
    }

	  
    @GetMapping("/buscar-id")
    public ResponseEntity<String> getUsuarioById(@RequestParam int id) {
        return usuarioService.getUsuarioById(id);
    }
	  
    @GetMapping("/numero-amigos")
    public ResponseEntity<Integer> obtenerNumeroAmigos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return usuarioService.obtenerNumeroAmigos(username);
    }
	  
	   @PutMapping("/confirmar-amigo")
	    public ResponseEntity<String> confirmarAmigo(@RequestParam("idAmigo") int idAmigo) {
	        // Confirmar al amigo utilizando el ID del usuario autenticado
	        usuarioService.confirmarAmigo(idAmigo);

	        return ResponseEntity.ok("Amigo confirmado correctamente");
	    }
	   @PutMapping("/rechazar-amigo")
	    public ResponseEntity<String> rechazarAmigo(@RequestParam("idAmigo") int idAmigo) {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.rechazarAlAmigo(idAmigo, username);
	    }

	   
	   @GetMapping("/ver-amigosConfirmado")
	    public ResponseEntity<?> verAmigos() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.verAmigosConfirmados(username);
	    }
	   
	   @PostMapping("/valorar-usuario")
	    public ResponseEntity<String> valorarUsuario(
	            @RequestParam("idConductor") int idConductor,
	            @RequestParam("valoracion") int valoracion,
	            @RequestParam("idGrupo") int idGrupo) {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.valorarAlUsuario(username, idConductor, valoracion, idGrupo);
	    }

	   
	   @PostMapping("/comentar-usuario")
	    public ResponseEntity<String> comentarUsuario(
	            @RequestParam("idConductor") int idConductor,
	            @RequestParam("comentario") String comentario,
	            @RequestParam("idGrupo") int idGrupo) {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.comentarAlUsuario(username, idConductor, comentario, idGrupo);
	    }
	   
	   @GetMapping("/ver-valoracion")
	    public ResponseEntity<String> verValoracion(
	            @RequestParam("idConductor") int idConductor,
	            @RequestParam("idGrupo") int idGrupo) {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.verValoracion(username, idConductor, idGrupo);
	    }
	   
	   @GetMapping("/ver-comentario")
	    public ResponseEntity<String> verComentario(
	            @RequestParam("idConductor") int idConductor,
	            @RequestParam("idGrupo") int idGrupo) {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.verComentario(username, idConductor, idGrupo);
	    }


	    @DeleteMapping("/borrar-amigo")
	    public ResponseEntity<String> borrarAmigo(@RequestParam("idAmigo") int idAmigo) {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.borrarAlAmigo(username, idAmigo);
	    }
	    

	    @GetMapping("/ver-estadisticas")
	    public ResponseEntity<?> verEstadisticas() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.verEstadisticas(username);
	    }

	    @GetMapping("/comentarios")
	    public ResponseEntity<List<ComentarioDTO>> verTodosLosComentarios() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.verTodosLosComentarios(username);
	    }

	   @GetMapping("/valoraciones")
	    public ResponseEntity<ValoracionDTO> verMediaValoracionConductor() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();
	        return usuarioService.verMediaValoracionConductor(username);
	    }
	 

}
