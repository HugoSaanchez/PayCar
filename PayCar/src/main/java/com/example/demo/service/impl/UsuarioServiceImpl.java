package com.example.demo.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.example.demo.entity.Usuario;
import com.example.demo.model.UsuarioModel;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import userdetails.CustomUserDetails;

@Service("usuarioService")
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

	@Autowired
	private UsuarioRepository usuarioRepository;

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
		usuario.setRol("ROL_ADMIN");

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
	
        if(usuario != null && passwordEncoder().matches(password, usuario.getPassword())) {
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
	    String nuevosAmigos = amigosActuales.isEmpty() ? String.valueOf(idAmigoNuevo) : amigosActuales + ";" + idAmigoNuevo;
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


}
