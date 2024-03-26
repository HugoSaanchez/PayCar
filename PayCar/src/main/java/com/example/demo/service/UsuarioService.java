package com.example.demo.service;


import com.example.demo.entity.Usuario;
import com.example.demo.model.UsuarioModel;

public interface UsuarioService {
	public Usuario registrar(UsuarioModel usuariomodel);
	public String findUsernameByEmail(String email);
	

}
