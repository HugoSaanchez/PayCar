package com.example.demo.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

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
	
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Invalidar el token de usuario actual
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout successful");
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

        // Verificar si el usuario ya está en la lista de amigos o amigos confirmados
        if (isAmigo(amigo, usuarioAutenticado.getId()) || isAmigoConfirmado(amigo, usuarioAutenticado.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya es amigo o está en la lista de amigos confirmados");
        }

        // Actualizar lista de amigos solo para el amigo
        actualizarListaAmigos(amigo, usuarioAutenticado.getId());

        // Guardar el amigo
        usuarioService.save(amigo);

        return ResponseEntity.ok("Solicitud de amistad enviada correctamente");
    }

    private boolean isAmigo(Usuario usuario, int idAmigo) {
        String amigos = usuario.getAmigos() != null ? usuario.getAmigos() : "";
        return amigos.contains(String.valueOf(idAmigo));
    }

    private boolean isAmigoConfirmado(Usuario usuario, int idAmigo) {
        String amigosConfirmados = usuario.getAmigosConfirmados() != null ? usuario.getAmigosConfirmados() : "";
        return amigosConfirmados.contains(String.valueOf(idAmigo));
    }

    private void actualizarListaAmigos(Usuario usuario, int idAmigoNuevo) {
        String amigosActuales = usuario.getAmigos() != null ? usuario.getAmigos() : "";
        String nuevosAmigos = amigosActuales.isEmpty() ? String.valueOf(idAmigoNuevo) : amigosActuales + ";" + idAmigoNuevo;
        usuario.setAmigos(nuevosAmigos);
    }
	
	  @GetMapping("/ver-amigos")
	    public ResponseEntity<?> verPosiblesAmigos() {
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
	  
	  @GetMapping("/numero-amigos")
	  public ResponseEntity<Integer> obtenerNumeroAmigos() {
	      String username = SecurityContextHolder.getContext().getAuthentication().getName();
	      Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	      if (usuarioAutenticado == null) {
	          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0);
	      }

	      String amigos = usuarioAutenticado.getAmigos();
	      if (amigos == null || amigos.isEmpty()) {
	          return ResponseEntity.ok(0);
	      }

	      List<Integer> listaAmigosIds = List.of(amigos.split(";"))
	                                          .stream()
	                                          .map(Integer::parseInt)
	                                          .collect(Collectors.toList());

	      return ResponseEntity.ok(listaAmigosIds.size());
	  }
	  
	   @PutMapping("/confirmar-amigo")
	    public ResponseEntity<String> confirmarAmigo(@RequestParam("idAmigo") int idAmigo) {
	        // Confirmar al amigo utilizando el ID del usuario autenticado
	        usuarioService.confirmarAmigo(idAmigo);

	        return ResponseEntity.ok("Amigo confirmado correctamente");
	    }
	   @PutMapping("/rechazar-amigo")
	   public ResponseEntity<String> rechazarAmigo(@RequestParam("idAmigo") int idAmigo) {
	       // Obtener el usuario autenticado
	       String username = SecurityContextHolder.getContext().getAuthentication().getName();
	       Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	       // Verificar si el usuario autenticado es válido
	       if (usuarioAutenticado == null) {
	           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	       }

	       // Verificar si el ID del amigo existe
	       Usuario amigo = usuarioService.findById(idAmigo);
	       if (amigo == null) {
	           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario con el ID especificado no existe");
	       }

	       // Rechazar la solicitud de amistad
	       usuarioService.rechazarAmigo(usuarioAutenticado, amigo);

	       return ResponseEntity.ok("Solicitud de amistad rechazada correctamente");
	   }

	   
	   @GetMapping("/ver-amigosConfirmado")
	    public ResponseEntity<?> verAmigos() {
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();
	        Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	        if (usuarioAutenticado == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        String amigosConfirmados = usuarioAutenticado.getAmigosConfirmados();
	        if (amigosConfirmados == null || amigosConfirmados.isEmpty()) {
	            return ResponseEntity.ok("No tienes amigos");
	        }

	        List<Integer> listaAmigosIds = List.of(amigosConfirmados.split(";"))
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
	   
	   @PostMapping("/valorar-usuario")
	   public ResponseEntity<String> valorarUsuario(
	           @RequestParam("idConductor") int idConductor,
	           @RequestParam("valoracion") int valoracion,
	           @RequestParam("idGrupo") int idGrupo) {
	       // Obtener el usuario autenticado
	       String username = SecurityContextHolder.getContext().getAuthentication().getName();
	       Usuario pasajero = usuarioService.findByUsername(username);

	       if (pasajero == null) {
	           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	       }

	       Usuario conductor = usuarioService.findById(idConductor);
	       if (conductor == null) {
	           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	       }

	       // Llama al método valorarUsuario del servicio
	       List<Valoracion> valoracionesExistentes = usuarioService.valorarUsuario(pasajero, conductor, idGrupo, valoracion);

	       // Ya no es necesario comprobar si la lista está vacía
	       return ResponseEntity.ok("Usuario valorado correctamente");
	   }

	   
	   @PostMapping("/comentar-usuario")
	    public ResponseEntity<String> comentarUsuario(
	            @RequestParam("idConductor") int idConductor,
	            @RequestParam("comentario") String comentario,
	            @RequestParam("idGrupo") int idGrupo) {
	        // Obtener el usuario autenticado
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();
	        Usuario pasajero = usuarioService.findByUsername(username);

	        if (pasajero == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Usuario conductor = usuarioService.findById(idConductor);
	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	        }

	        // Llama al método comentarUsuario del servicio
	        List<Comentario> comentariosExistentes = usuarioService.comentarUsuario(pasajero, conductor, idGrupo, comentario);

	        if (!comentariosExistentes.isEmpty()) {
	            return ResponseEntity.ok("Comentario actualizado correctamente");
	        }

	        return ResponseEntity.ok("Comentario agregado correctamente");
	    }
	   
	   @GetMapping("/ver-valoracion")
	   public ResponseEntity<String> verValoracion(
	           @RequestParam("idConductor") int idConductor,
	           @RequestParam("idGrupo") int idGrupo) {
	       // Obtener el usuario autenticado
	       String username = SecurityContextHolder.getContext().getAuthentication().getName();
	       Usuario pasajero = usuarioService.findByUsername(username);

	       if (pasajero == null) {
	           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	       }

	       Usuario conductor = usuarioService.findById(idConductor);
	       if (conductor == null) {
	           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	       }

	       // Obtener la valoración del usuario
	       Valoracion valoracion = usuarioService.obtenerValoracion(pasajero, conductor, idGrupo);
	       if (valoracion != null) {
	           return ResponseEntity.ok("Valoración: " + valoracion.getValoracion());
	       } else {
	           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Valoración no encontrada");
	       }
	   }
	   
	   @GetMapping("/ver-comentario")
	   public ResponseEntity<String> verComentario(
	           @RequestParam("idConductor") int idConductor,
	           @RequestParam("idGrupo") int idGrupo) {
	       // Obtener el usuario autenticado
	       String username = SecurityContextHolder.getContext().getAuthentication().getName();
	       Usuario pasajero = usuarioService.findByUsername(username);

	       if (pasajero == null) {
	           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	       }

	       Usuario conductor = usuarioService.findById(idConductor);
	       if (conductor == null) {
	           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	       }

	       // Obtener el comentario del usuario
	       Comentario comentario = usuarioService.obtenerComentario(pasajero, conductor, idGrupo);
	       if (comentario != null) {
	           return ResponseEntity.ok("Comentario: " + comentario.getComentario());
	       } else {
	           return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comentario no encontrado");
	       }
	   }


	   @DeleteMapping("/borrar-amigo")
	    public ResponseEntity<String> borrarAmigo(@RequestParam("idAmigo") int idAmigo) {
	        // Obtener el usuario autenticado
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();
	        Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	        // Verificar si el usuario autenticado es válido
	        if (usuarioAutenticado == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        // Verificar si el ID del amigo existe
	        Usuario amigo = usuarioService.findById(idAmigo);
	        if (amigo == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario con el ID especificado no existe");
	        }

	        // Eliminar el amigo de la lista de amigos confirmados del usuario autenticado
	        usuarioService.borrarAmigo(usuarioAutenticado.getId(), idAmigo);

	        return ResponseEntity.ok("Amigo eliminado correctamente");
	    }
	    

	   @GetMapping("/ver-estadisticas")
	    public ResponseEntity<?> verEstadisticas() {
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();
	        Usuario usuarioAutenticado = usuarioService.findByUsername(username);

	        if (usuarioAutenticado == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Map<String, Object> estadisticas = usuarioService.obtenerEstadisticasUsuario(usuarioAutenticado.getId());
	        return ResponseEntity.ok(estadisticas);
	    }
	   

	   @GetMapping("/comentarios")
	    public ResponseEntity<List<ComentarioDTO>> verTodosLosComentarios() {
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();
	        Usuario conductor = usuarioService.findByUsername(username);

	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }

	        List<ComentarioDTO> comentarios = usuarioService.obtenerComentariosParaConductor(conductor.getId());
	        return ResponseEntity.ok(comentarios);
	    }

	    @GetMapping("/valoraciones")
	    public ResponseEntity<ValoracionDTO> verMediaValoracionConductor() {
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();
	        Usuario conductor = usuarioService.findByUsername(username);

	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }

	        double mediaValoracion = usuarioService.obtenerMediaValoracionParaConductor(conductor.getId());
	        ValoracionDTO valoracionDTO = new ValoracionDTO(mediaValoracion);
	        return ResponseEntity.ok(valoracionDTO);
	    }
	 

}
