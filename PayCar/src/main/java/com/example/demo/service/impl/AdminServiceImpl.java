package com.example.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.AdminService;

import jakarta.persistence.EntityNotFoundException;

@Service("adminService")
public class AdminServiceImpl implements AdminService {
	
	   @Autowired
	    private UsuarioRepository usuarioRepository;

	@Override
	public List<Usuario> findByRol(String rol) {
		return usuarioRepository.findByRol(rol);

	}

	@Override
	public void borrarUsuario(int id) {
		 Usuario usuario = usuarioRepository.findById(id).orElse(null);

	        // Verificar si se encontró el usuario
	        if (usuario != null) {
	            // Actualizar el campo "borrado" a 1
	        	usuario.setBorrado(true);;
	            // Guardar los cambios en la base de datos
	        	usuarioRepository.save(usuario);
	        } else {
	            // Manejar el caso en que no se encuentre el usuario
	            throw new EntityNotFoundException("Usuario no encontrado con ID: " + id);
	        }
	}

	@Override
	public void activarUsuario(int id) {
		 Usuario usuario = usuarioRepository.findById(id).orElse(null);

	       
	        if (usuario != null) {
	            if(usuario.isActivado())
	            	usuario.setActivado(false);
	            else
	            	usuario.setActivado(true);
	           
	        	usuarioRepository.save(usuario);
	        } else {
	            // Manejar el caso en que no se encuentre el usuario
	            throw new EntityNotFoundException("Usuario no encontrado con ID: " + id);
	        }
		
	}

	@Override
	public List<Usuario> findByEstado(String estado) {
        if ("activados".equals(estado)) {
        	  return usuarioRepository.findByRolAndActivado("ROL_USER", true);
        } else if ("desactivados".equals(estado)) {
        	  return usuarioRepository.findByRolAndActivado("ROL_USER", false);
        } else {
            return usuarioRepository.findByRol("ROL_USER");
        }

	}

	  
}
