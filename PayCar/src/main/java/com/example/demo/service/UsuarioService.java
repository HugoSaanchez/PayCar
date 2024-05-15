package com.example.demo.service;

import org.springframework.http.ResponseEntity;

import com.example.demo.entity.Usuario;
import com.example.demo.model.UsuarioModel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UsuarioService {
	public Usuario registrar(UsuarioModel usuariomodel);

	public String findUsernameByEmail(String email);

	public Usuario findByUsername(String username);

	public String logout(HttpServletRequest request, HttpServletResponse response);

	public Usuario findUsuario(String username, String password);

	Usuario findById(int id);

	Usuario save(Usuario usuario);

	boolean isAmigo(Usuario usuario, int idAmigo);

	void actualizarListaAmigos(Usuario usuario, int idAmigoNuevo);

	ResponseEntity<String> agregarAmigo(int idAmigo);
}
