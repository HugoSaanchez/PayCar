package com.example.demo.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Usuario;
import com.example.demo.entityDTO.UsuarioDTO;
import com.example.demo.model.UsuarioModel;
import com.example.demo.service.UsuarioService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
public class UserApiController {
	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestParam("user") String username, @RequestParam("password") String pwd) {

	    Usuario usuario = usuarioService.findUsuario(username, pwd);

	    if (usuario == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
	    }

	    String token = getJWTToken(username);
	    usuario.setToken(token);
	    
	    UsuarioDTO usuarioDTO = new UsuarioDTO(
	        usuario.getId(),
	        usuario.getNombre(),
	        usuario.getUsername(),
	        usuario.getRol(),
	        usuario.isActivado(),
	        usuario.getToken()
	    );

	    return ResponseEntity.ok(usuarioDTO);
	}

	@PostMapping("/register")
	public com.example.demo.entity.Usuario saveUser(@RequestBody UsuarioModel user) {
		return usuarioService.registrar(user);
	}

	private String getJWTToken(String username) {
		String secretKey = "mySecretKey";
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ALUMNO");

		String token = Jwts.builder().setId("softtekJWT").setSubject(username)
				.claim("authorities",
						grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 600000))
				.signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

		return "Bearer " + token;
	}

	@PutMapping("/agregar-amigo")
	public ResponseEntity<String> agregarAmigo(@RequestParam("idAmigo") int idAmigo) {
	    // Obtener el usuario autenticado
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
	    Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	    // Verificar si el usuario autenticado es válido
	    if (usuarioAutenticado == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	    }
	    if (usuarioAutenticado.getId() == idAmigo) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No te puedes agregar a ti mismo como amigo");
	    }
	    // Verificar si el ID del amigo existe
	    Usuario amigo = usuarioService.findById(idAmigo);
	    if (amigo == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario con el ID especificado no existe");
	    }

	    // Verificar si el usuario ya es amigo
	    if (isAmigo(usuarioAutenticado, idAmigo)) {
	        System.out.println(idAmigo);
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya es amigo");
	    }

	    // Actualizar lista de amigos para ambos usuarios
	    actualizarListaAmigos(usuarioAutenticado, idAmigo);
	    actualizarListaAmigos(amigo, usuarioAutenticado.getId());

	    // Guardar ambos usuarios
	    usuarioService.save(usuarioAutenticado);
	    usuarioService.save(amigo);

	    return ResponseEntity.ok("Usuario agregado como amigo correctamente");
	}

	private boolean isAmigo(Usuario usuario, int idAmigo) {
	    String amigos = usuario.getAmigos() != null ? usuario.getAmigos() : "";
	    return amigos.contains(String.valueOf(idAmigo));
	}

	private void actualizarListaAmigos(Usuario usuario, int idAmigoNuevo) {
	    String amigosActuales = usuario.getAmigos() != null ? usuario.getAmigos() : "";
	    String nuevosAmigos = amigosActuales.isEmpty() ? String.valueOf(idAmigoNuevo) : amigosActuales + ";" + idAmigoNuevo;
	    usuario.setAmigos(nuevosAmigos);
	}
	
	  @GetMapping("/ver-amigos")
	    public ResponseEntity<?> verAmigos() {
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();
	        Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	        if (usuarioAutenticado == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        String amigos = usuarioAutenticado.getAmigos();
	        if (amigos == null || amigos.isEmpty()) {
	            return ResponseEntity.ok("No tienes amigos");
	        }

	        List<Integer> listaAmigosIds = List.of(amigos.split(";"))
	                                            .stream()
	                                            .map(Integer::parseInt)
	                                            .collect(Collectors.toList());

	        List<UsuarioDTO> listaAmigos = listaAmigosIds.stream()
	            .map(id -> {
	                Usuario amigo = usuarioService.findById(id);
	                return new UsuarioDTO(
	                    amigo.getId(),
	                    amigo.getNombre(),
	                    amigo.getUsername(),
	                    amigo.getRol(),
	                    amigo.isActivado(),
	                    amigo.getToken()
	                );
	            })
	            .collect(Collectors.toList());

	        return ResponseEntity.ok(listaAmigos);
	    }
	  
	  
	  @GetMapping("/buscar-por-email")
	    public ResponseEntity<UsuarioDTO> getUsuarioByEmail(@RequestParam String email) {
	        Usuario usuario = usuarioService.findByUsername(email);
	        if (usuario != null) {
	            UsuarioDTO usuarioDTO = new UsuarioDTO(
	                usuario.getId(),
	                usuario.getNombre(),
	                usuario.getUsername(),
	                usuario.getRol(),
	                usuario.isActivado(),
	                usuario.getToken()
	            );
	            return ResponseEntity.ok(usuarioDTO);
	        } else {
	            return ResponseEntity.notFound().build();
	        }
	    }

	  
	  @GetMapping("buscar-id")
	  public ResponseEntity<String> getUsuarioById(@RequestParam int id){
		  Usuario usuario = usuarioService.findById(id);
		  if (usuario != null) {
	            UsuarioDTO usuarioDTO = new UsuarioDTO(
	                usuario.getId(),
	                usuario.getNombre(),
	                usuario.getUsername(),
	                usuario.getRol(),
	                usuario.isActivado(),
	                usuario.getToken()
	            );
	            return ResponseEntity.ok(usuarioDTO.getNombre());
		  }else {
		      return ResponseEntity.notFound().build();
		  }
	  }

}
