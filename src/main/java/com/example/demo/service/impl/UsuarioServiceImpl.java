package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Comentario;
import com.example.demo.entity.Grupo;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.UsuarioGrupo;
import com.example.demo.entity.Valoracion;
import com.example.demo.entityDTO.ComentarioDTO;
import com.example.demo.entityDTO.UsuarioDTO;
import com.example.demo.entityDTO.ValoracionDTO;
import com.example.demo.model.UsuarioModel;
import com.example.demo.repository.ComentarioRepository;
import com.example.demo.repository.UsuarioGrupoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.ValoracionRepository;
import com.example.demo.service.GrupoService;
import com.example.demo.service.UsuarioService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import userdetails.CustomUserDetails;

@Service("usuarioService")
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	@Autowired
	private ValoracionRepository valoracionRepository;
	@Autowired
	private ComentarioRepository comentarioRepository;
	@Autowired
	private UsuarioGrupoRepository usuarioGrupoRepository;
	@Autowired
	private GrupoService grupoService;

	private Usuario convertToUsuario(UsuarioModel usuarioModel) {
		ModelMapper mapper = new ModelMapper();
		return mapper.map(usuarioModel, Usuario.class);
	}

	private UsuarioModel convertToUsuarioModel(Usuario usuario) {
		ModelMapper mapper = new ModelMapper();
		return mapper.map(usuario, UsuarioModel.class);
	}

	@Override
	public Usuario registrar(UsuarioModel usuariomodel) {
		Usuario usuario = convertToUsuario(usuariomodel);
		usuario.setPassword(passwordEncoder().encode(usuario.getPassword()));

		usuario.setActivado(true);
		usuario.setBorrado(false);
		usuario.setRol("ROL_USER");

		return usuarioRepository.save(usuario);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.example.demo.entity.Usuario usuario = usuarioRepository.findByUsername(username);

		if (usuario == null) {
			throw new UsernameNotFoundException("Usuario no encontrado");
		}

		if (!usuario.isEnabled() || usuario.isBorrado() || !usuario.getRol().equals("ROL_ADMIN")) {
			throw new UsernameNotFoundException("Acceso denegado");
		}
		if (usuario != null && usuario.isEnabled() && !usuario.isBorrado()) {

			CustomUserDetails customUserDetails = new CustomUserDetails(usuario);

			UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(username);
			builder.password(usuario.getPassword());
			builder.authorities(new SimpleGrantedAuthority(usuario.getRol()));
			builder.accountExpired(!customUserDetails.isAccountNonExpired());
			builder.accountLocked(!customUserDetails.isAccountNonLocked());
			builder.credentialsExpired(!customUserDetails.isCredentialsNonExpired());
			builder.disabled(!customUserDetails.isEnabled());

			return builder.build();
		} else {
			throw new UsernameNotFoundException("Alumno no encontrado");
		}
	}

	@Override
	public UsuarioDTO login(String username, String password) {
		Usuario usuario = usuarioRepository.findByUsername(username);

		if (usuario == null || !passwordEncoder().matches(password, usuario.getPassword())) {
			return null;
		}

		String token = getJWTToken(username);
		usuario.setToken(token);

		UsuarioDTO usuarioDTO = new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getUsername(),
				usuario.getRol(), usuario.isActivado(), usuario.getToken());

		return usuarioDTO;
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

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public String findUsernameByEmail(String email) {
		Usuario usuario = usuarioRepository.findByUsername(email);
		return usuario != null ? usuario.getNombre() : null;
	}

	@Override
	public Usuario findByUsername(String username) {

		return usuarioRepository.findByUsername(username);
	}

	@Override
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		try {
			SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
			logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());
			return "redirect:/auth/login?logout";
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/";
		}
	}

	@Override
	public Usuario findUsuario(String username, String password) {

		Usuario usuario = usuarioRepository.findByUsername(username);

		if (usuario != null && passwordEncoder().matches(password, usuario.getPassword())) {
			return usuario;
		} else {

			return null;
		}
	}

	@Override
	public Usuario findById(int id) {
		return usuarioRepository.findById(id);
	}

	@Override
	public Usuario save(Usuario usuario) {
		return usuarioRepository.save(usuario);
	}

	@Override
	public boolean isAmigo(Usuario usuario, int idAmigo) {
		String amigos = usuario.getAmigos() != null ? usuario.getAmigos() : "";
		return amigos.contains(String.valueOf(idAmigo));
	}

	@Override
	public void actualizarListaAmigos(Usuario usuario, int idAmigoNuevo) {
		String amigosActuales = usuario.getAmigos() != null ? usuario.getAmigos() : "";
		String nuevosAmigos = amigosActuales.isEmpty() ? String.valueOf(idAmigoNuevo)
				: amigosActuales + ";" + idAmigoNuevo;
		usuario.setAmigos(nuevosAmigos);
	}

	@Override
	public ResponseEntity<String> agregarAmigo(int idAmigo) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuarioAutenticado = findByUsername(username);

		if (usuarioAutenticado == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
		}

		Usuario amigo = findById(idAmigo);
		if (amigo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario con el ID especificado no existe");
		}

		if (isAmigo(usuarioAutenticado, idAmigo)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya es amigo");
		}

		actualizarListaAmigos(usuarioAutenticado, idAmigo);
		actualizarListaAmigos(amigo, usuarioAutenticado.getId());

		save(usuarioAutenticado);
		save(amigo);

		return ResponseEntity.ok("Usuario agregado como amigo correctamente");
	}

	@Override

	public void confirmarAmigo(int amigoId) {
		// Obtener el nombre de usuario del contexto de seguridad
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = findByUsername(username);

		if (usuario == null) {
			return;
		}

		Usuario amigo = findById(amigoId);

		if (amigo == null) {
			return;
		}

		// Actualizar listas del usuario autenticado
		List<Integer> amigosList = Arrays.stream(usuario.getAmigos().split(";")).map(Integer::parseInt)
				.collect(Collectors.toList());

		if (!amigosList.contains(amigoId)) {
			return;
		}

		amigosList.remove(Integer.valueOf(amigoId));
		usuario.setAmigos(String.join(";", amigosList.stream().map(String::valueOf).collect(Collectors.toList())));

		List<Integer> amigosConfirmadosList = usuario.getAmigosConfirmados() == null
				|| usuario.getAmigosConfirmados().isEmpty() ? new ArrayList<>()
						: Arrays.stream(usuario.getAmigosConfirmados().split(";")).map(Integer::parseInt)
								.collect(Collectors.toList());

		amigosConfirmadosList.add(amigoId);
		usuario.setAmigosConfirmados(
				String.join(";", amigosConfirmadosList.stream().map(String::valueOf).collect(Collectors.toList())));

		save(usuario);

		// Actualizar listas del amigo
		List<Integer> amigosDelAmigoList = amigo.getAmigos() == null || amigo.getAmigos().isEmpty() ? new ArrayList<>()
				: Arrays.stream(amigo.getAmigos().split(";")).map(Integer::parseInt).collect(Collectors.toList());

		if (amigosDelAmigoList.contains(usuario.getId())) {
			amigosDelAmigoList.remove(Integer.valueOf(usuario.getId()));
		}

		amigo.setAmigos(
				String.join(";", amigosDelAmigoList.stream().map(String::valueOf).collect(Collectors.toList())));

		List<Integer> amigosConfirmadosDelAmigoList = amigo.getAmigosConfirmados() == null
				|| amigo.getAmigosConfirmados().isEmpty() ? new ArrayList<>()
						: Arrays.stream(amigo.getAmigosConfirmados().split(";")).map(Integer::parseInt)
								.collect(Collectors.toList());

		amigosConfirmadosDelAmigoList.add(usuario.getId());
		amigo.setAmigosConfirmados(String.join(";",
				amigosConfirmadosDelAmigoList.stream().map(String::valueOf).collect(Collectors.toList())));

		save(amigo);
	}

	@Override
	public void rechazarAmigo(Usuario usuarioAutenticado, Usuario amigo) {
		// Remover al amigo de la lista de amigos pendientes del usuario autenticado
		String amigosActuales = usuarioAutenticado.getAmigos() != null ? usuarioAutenticado.getAmigos() : "";
		List<String> listaAmigos = new ArrayList<>(List.of(amigosActuales.split(";")));
		listaAmigos.remove(String.valueOf(amigo.getId()));
		usuarioAutenticado.setAmigos(String.join(";", listaAmigos));

		// Guardar los cambios en el usuario autenticado
		save(usuarioAutenticado);
	}

	@Override
	public List<Valoracion> valorarUsuario(Usuario pasajero, Usuario conductor, int idGrupo, int valoracion) {
		Grupo grupo = grupoService.obtenerGrupoPorId(idGrupo);

		// Verificar si ya existe una valoración para este grupo
		List<Valoracion> valoracionesExistentes = valoracionRepository.findByPasajeroAndConductorAndGrupo(pasajero,
				conductor, grupo);

		if (valoracionesExistentes.isEmpty()) {
			Valoracion nuevaValoracion = new Valoracion();
			nuevaValoracion.setPasajero(pasajero);
			nuevaValoracion.setConductor(conductor);
			nuevaValoracion.setGrupo(grupo);
			nuevaValoracion.setValoracion(valoracion);
			valoracionRepository.save(nuevaValoracion);
		} else {
			// Actualizar la valoración existente
			Valoracion valoracionExistente = valoracionesExistentes.get(0);
			valoracionExistente.setValoracion(valoracion);
			valoracionRepository.save(valoracionExistente);
		}

		return valoracionesExistentes;
	}

	@Override
	public List<Comentario> comentarUsuario(Usuario pasajero, Usuario conductor, int idGrupo, String comentario) {
		Grupo grupo = grupoService.obtenerGrupoPorId(idGrupo);

		// Verificar si ya existe un comentario para este grupo
		List<Comentario> comentariosExistentes = comentarioRepository.findByPasajeroAndConductorAndGrupo(pasajero,
				conductor, grupo);

		if (comentariosExistentes.isEmpty()) {
			Comentario nuevoComentario = new Comentario(pasajero, comentario, conductor);
			nuevoComentario.setGrupo(grupo);
			comentarioRepository.save(nuevoComentario);
		} else {
			// Actualizar el comentario existente
			Comentario comentarioExistente = comentariosExistentes.get(0);
			comentarioExistente.setComentario(comentario);
			comentarioRepository.save(comentarioExistente);
		}

		return comentariosExistentes;
	}

	@Override
	public Valoracion obtenerValoracion(Usuario pasajero, Usuario conductor, int idGrupo) {
		Grupo grupo = grupoService.obtenerGrupoPorId(idGrupo);
		return valoracionRepository.findByPasajeroAndConductorAndGrupo(pasajero, conductor, grupo).stream().findFirst()
				.orElse(null);
	}

	@Override
	public Comentario obtenerComentario(Usuario pasajero, Usuario conductor, int idGrupo) {
		Grupo grupo = grupoService.obtenerGrupoPorId(idGrupo);
		return comentarioRepository.findByPasajeroAndConductorAndGrupo(pasajero, conductor, grupo).stream().findFirst()
				.orElse(null);
	}

	@Override

	public void borrarAmigo(int idUsuario, int idAmigo) {
		Usuario usuario = usuarioRepository.findById(idUsuario);
		Usuario amigo = usuarioRepository.findById(idAmigo);

		if (usuario != null && amigo != null) {
			// Eliminar el amigo de la lista de amigos confirmados del usuario
			List<String> amigosConfirmados = new ArrayList<>(Arrays.asList(usuario.getAmigosConfirmados().split(";")));
			amigosConfirmados.remove(String.valueOf(idAmigo));
			usuario.setAmigosConfirmados(String.join(";", amigosConfirmados));

			// Eliminar el usuario de la lista de amigos confirmados del amigo
			List<String> amigosDelAmigo = new ArrayList<>(Arrays.asList(amigo.getAmigosConfirmados().split(";")));
			amigosDelAmigo.remove(String.valueOf(idUsuario));
			amigo.setAmigosConfirmados(String.join(";", amigosDelAmigo));

			// Guardar ambos usuarios
			usuarioRepository.save(usuario);
			usuarioRepository.save(amigo);
		}
	}

	@Override
	public Map<String, Object> obtenerEstadisticasUsuario(int idUsuario) {
		List<UsuarioGrupo> usuarioGrupos = usuarioGrupoRepository.findByUsuarioId(idUsuario);

		int numeroGrupos = usuarioGrupos.size();
		float totalCostePagado = 0;
		float totalCosteTotal = 0;
		int vecesConductor = 0;
		int vecesPasajero = 0;

		for (UsuarioGrupo ug : usuarioGrupos) {
			totalCostePagado += ug.getCostepagado();
			totalCosteTotal += ug.getCostetotal();
			if (ug.getRol().equalsIgnoreCase("conductor")) {
				vecesConductor++;
			} else if (ug.getRol().equalsIgnoreCase("pasajero")) {
				vecesPasajero++;
			}
		}

		Map<String, Object> estadisticas = new HashMap<>();
		estadisticas.put("numeroGrupos", numeroGrupos);
		estadisticas.put("totalCostePagado", totalCostePagado);
		estadisticas.put("totalCosteTotal", totalCosteTotal);
		estadisticas.put("vecesConductor", vecesConductor);
		estadisticas.put("vecesPasajero", vecesPasajero);

		return estadisticas;
	}

	@Override
	public double obtenerMediaValoracionUsuario(int usuarioId) {
		Usuario usuario = usuarioRepository.findById(usuarioId);
		if (usuario == null) {
			return 0.0;
		}

		List<Valoracion> valoraciones = valoracionRepository.findByConductor(usuario);

		return valoraciones.stream().mapToInt(Valoracion::getValoracion).average().orElse(0.0);
	}

	@Override
	public List<ComentarioDTO> obtenerComentariosParaConductor(int conductorId) {
		List<Comentario> comentarios = comentarioRepository.findByConductorId(conductorId);
		return comentarios.stream()
				.map(comentario -> new ComentarioDTO(comentario.getComentario(), comentario.getPasajero().getNombre()))
				.collect(Collectors.toList());
	}

	@Override
	public double obtenerMediaValoracionParaConductor(int conductorId) {
		List<Valoracion> valoraciones = valoracionRepository.findByConductorIdList(conductorId);
		return valoraciones.stream().mapToInt(Valoracion::getValoracion).average().orElse(0.0);
	}

	@Override
	public ResponseEntity<String> agregarAmigo(int idAmigo, String username) {
		Usuario usuarioAutenticado = usuarioRepository.findByUsername(username);

		if (usuarioAutenticado == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
		}
		if (usuarioAutenticado.getId() == idAmigo) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No te puedes agregar a ti mismo como amigo");
		}

		Usuario amigo = usuarioRepository.findById(idAmigo);
		if (amigo == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario con el ID especificado no existe");
		}

		if (isAmigo(amigo, usuarioAutenticado.getId()) || isAmigoConfirmado(amigo, usuarioAutenticado.getId())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("El usuario ya es amigo o está en la lista de amigos confirmados");
		}

		actualizarListaAmigos(amigo, usuarioAutenticado.getId());
		usuarioRepository.save(amigo);

		return ResponseEntity.ok("Solicitud de amistad enviada correctamente");
	}

	@Override
	public boolean isAmigoConfirmado(Usuario usuario, int idAmigo) {
		String amigosConfirmados = usuario.getAmigosConfirmados() != null ? usuario.getAmigosConfirmados() : "";
		return amigosConfirmados.contains(String.valueOf(idAmigo));
	}

	@Override
	public ResponseEntity<?> verPosiblesAmigos(String username) {
		Usuario usuarioAutenticado = usuarioRepository.findByUsername(username);

		if (usuarioAutenticado == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
		}

		String amigos = usuarioAutenticado.getAmigos();
		if (amigos == null || amigos.isEmpty()) {
			return ResponseEntity.ok("No tienes amigos");
		}

		List<Integer> listaAmigosIds = List.of(amigos.split(";")).stream().map(Integer::parseInt)
				.collect(Collectors.toList());

		List<UsuarioDTO> listaAmigos = listaAmigosIds.stream().map(id -> {
			Usuario amigo = usuarioRepository.findById(id).orElse(null);
			if (amigo != null) {
				return new UsuarioDTO(amigo.getId(), amigo.getNombre(), amigo.getUsername(), amigo.getRol(),
						amigo.isActivado(), amigo.getToken());
			}
			return null;
		}).filter(amigo -> amigo != null).collect(Collectors.toList());

		return ResponseEntity.ok(listaAmigos);
	}

	@Override
	public ResponseEntity<UsuarioDTO> getUsuarioByEmail(String email) {
		Usuario usuario = usuarioRepository.findByUsername(email);
		if (usuario != null) {
			UsuarioDTO usuarioDTO = new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getUsername(),
					usuario.getRol(), usuario.isActivado(), usuario.getToken());
			return ResponseEntity.ok(usuarioDTO);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Override
	public ResponseEntity<String> getUsuarioById(int id) {
		Usuario usuario = usuarioRepository.findById(id);
		if (usuario != null) {
			UsuarioDTO usuarioDTO = new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getUsername(),
					usuario.getRol(), usuario.isActivado(), usuario.getToken());
			return ResponseEntity.ok(usuarioDTO.getNombre());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Override
	public ResponseEntity<Integer> obtenerNumeroAmigos(String username) {
		Usuario usuarioAutenticado = usuarioRepository.findByUsername(username);

		if (usuarioAutenticado == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(0);
		}

		String amigos = usuarioAutenticado.getAmigos();
		if (amigos == null || amigos.isEmpty()) {
			return ResponseEntity.ok(0);
		}

		List<Integer> listaAmigosIds = List.of(amigos.split(";")).stream().map(Integer::parseInt)
				.collect(Collectors.toList());

		return ResponseEntity.ok(listaAmigosIds.size());
	}
	
	 @Override
	    public ResponseEntity<String> rechazarAlAmigo(int idAmigo, String username) {
	        Usuario usuarioAutenticado = usuarioRepository.findByUsername(username);

	        if (usuarioAutenticado == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Usuario amigo = usuarioRepository.findById(idAmigo);
	        if (amigo == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario con el ID especificado no existe");
	        }

	        rechazarAmigo(usuarioAutenticado, amigo);

	        return ResponseEntity.ok("Solicitud de amistad rechazada correctamente");
	    }
	 @Override
	    public ResponseEntity<?> verAmigosConfirmados(String username) {
	        Usuario usuarioAutenticado = usuarioRepository.findByUsername(username);

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
	                Usuario amigo = usuarioRepository.findById(id).orElse(null);
	                if (amigo != null) {
	                    return new UsuarioDTO(
	                        amigo.getId(),
	                        amigo.getNombre(),
	                        amigo.getUsername(),
	                        amigo.getRol(),
	                        amigo.isActivado(),
	                        amigo.getToken()
	                    );
	                }
	                return null;
	            })
	            .filter(amigo -> amigo != null)
	            .collect(Collectors.toList());

	        return ResponseEntity.ok(listaAmigos);
	    }
	 @Override
	    public ResponseEntity<String> valorarAlUsuario(String username, int idConductor, int valoracion, int idGrupo) {
	        Usuario pasajero = usuarioRepository.findByUsername(username);

	        if (pasajero == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Usuario conductor = usuarioRepository.findById(idConductor);
	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	        }

	        List<Valoracion> valoracionesExistentes = valorarUsuario(pasajero, conductor, idGrupo, valoracion);

	        return ResponseEntity.ok("Usuario valorado correctamente");
	    }
	 
	  @Override
	    public ResponseEntity<String> comentarAlUsuario(String username, int idConductor, String comentario, int idGrupo) {
	        Usuario pasajero = usuarioRepository.findByUsername(username);

	        if (pasajero == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Usuario conductor = usuarioRepository.findById(idConductor);
	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	        }

	        List<Comentario> comentariosExistentes = comentarUsuario(pasajero, conductor, idGrupo, comentario);

	        if (!comentariosExistentes.isEmpty()) {
	            return ResponseEntity.ok("Comentario actualizado correctamente");
	        }

	        return ResponseEntity.ok("Comentario agregado correctamente");
	    }
	  
	  @Override
	    public ResponseEntity<String> verValoracion(String username, int idConductor, int idGrupo) {
	        Usuario pasajero = usuarioRepository.findByUsername(username);

	        if (pasajero == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Usuario conductor = usuarioRepository.findById(idConductor);
	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	        }

	        Valoracion valoracion = obtenerValoracion(pasajero, conductor, idGrupo);
	        if (valoracion != null) {
	            return ResponseEntity.ok("Valoración: " + valoracion.getValoracion());
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Valoración no encontrada");
	        }
	        
	        
	    }
	  
	  @Override
	    public ResponseEntity<String> verComentario(String username, int idConductor, int idGrupo) {
	        Usuario pasajero = usuarioRepository.findByUsername(username);

	        if (pasajero == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Usuario conductor = usuarioRepository.findById(idConductor);
	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conductor no encontrado");
	        }

	        Comentario comentario = obtenerComentario(pasajero, conductor, idGrupo);
	        if (comentario != null) {
	            return ResponseEntity.ok("Comentario: " + comentario.getComentario());
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comentario no encontrado");
	        }
	    }
	  
	  @Override
	    public ResponseEntity<String> borrarAlAmigo(String username, int idAmigo) {
	        Usuario usuarioAutenticado = usuarioRepository.findByUsername(username);

	        if (usuarioAutenticado == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Usuario amigo = usuarioRepository.findById(idAmigo);
	        if (amigo == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario con el ID especificado no existe");
	        }

	        borrarAmigo(usuarioAutenticado.getId(), idAmigo);

	        return ResponseEntity.ok("Amigo eliminado correctamente");
	    }
	  
	  
	  @Override
	    public ResponseEntity<?> verEstadisticas(String username) {
	        Usuario usuarioAutenticado = usuarioRepository.findByUsername(username);

	        if (usuarioAutenticado == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
	        }

	        Map<String, Object> estadisticas = obtenerEstadisticasUsuario(usuarioAutenticado.getId());
	        return ResponseEntity.ok(estadisticas);
	    }
	  
	  @Override
	    public ResponseEntity<ValoracionDTO> verMediaValoracionConductor(String username) {
	        Usuario conductor = usuarioRepository.findByUsername(username);

	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }

	        double mediaValoracion = obtenerMediaValoracionParaConductor(conductor.getId());
	        ValoracionDTO valoracionDTO = new ValoracionDTO(mediaValoracion);
	        return ResponseEntity.ok(valoracionDTO);
	    }
	  
	  @Override
	    public ResponseEntity<List<ComentarioDTO>> verTodosLosComentarios(String username) {
	        Usuario conductor = usuarioRepository.findByUsername(username);

	        if (conductor == null) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }

	        List<ComentarioDTO> comentarios = obtenerComentariosParaConductor(conductor.getId());
	        return ResponseEntity.ok(comentarios);
	    }
	  


}
