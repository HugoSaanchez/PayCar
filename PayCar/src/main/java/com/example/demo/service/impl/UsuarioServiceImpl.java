package com.example.demo.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Usuario;
import com.example.demo.model.UsuarioModel;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.UsuarioService;

import userdetails.CustomUserDetails;
@Service("usuarioService")
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService{
	
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

	
}
