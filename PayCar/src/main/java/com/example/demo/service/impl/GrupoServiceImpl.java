package com.example.demo.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.Invitacion;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.UsuarioGrupo;
import com.example.demo.repository.GrupoRepository;
import com.example.demo.repository.InvitacionRepository;
import com.example.demo.repository.UsuarioGrupoRepository;
import com.example.demo.service.GrupoService;

@Service("grupoService")
public class GrupoServiceImpl implements GrupoService {
	
	@Autowired
	private GrupoRepository grupoRepository;
	
	@Autowired
	private InvitacionRepository invitacionRepository;
	
	@Autowired
	private UsuarioGrupoRepository usuarioGrupoRepository;

	@Override
	public List<Grupo> obtenerGrupos() {
		 return grupoRepository.findAll();
	}

	@Override
	public List<UsuarioGrupo> obtenerUsuariosPorGrupoId(int grupoId) {
		 return usuarioGrupoRepository.findByGrupoId(grupoId);
	}
	
	@Override
	public Grupo crearGrupo(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

	@Override
	public UsuarioGrupo crearUsuarioGrupo(UsuarioGrupo usuarioGrupo) {
		
        return usuarioGrupoRepository.save(usuarioGrupo);
	}
	@Override
	 public String generarCodigoUnico() {
	        return UUID.randomUUID().toString();
	    }
	
	public Grupo obtenerGrupoPorId(int id) {
        return grupoRepository.findById(id).orElse(null);
    }
	
	 public void guardarInvitacion(Invitacion invitacion) {
	        invitacionRepository.save(invitacion);
	    }
	 
	 @Override
	   public Invitacion obtenerGrupoPorCodigo(String codigoInvitacion) {
	        Invitacion invitacion = invitacionRepository.findByCodigoInvitacion(codigoInvitacion);
	        
	        return invitacion;
	    }
	 
	 @Override
	  public List<Grupo> obtenerGruposPorUsuario(Usuario usuario) {
	        List<UsuarioGrupo> usuarioGrupos = usuarioGrupoRepository.findByUsuario(usuario);
	        return usuarioGrupos.stream()
	                            .map(UsuarioGrupo::getGrupo)
	                            .collect(Collectors.toList());
	    }
	 
	 @Override
	  public Optional<UsuarioGrupo> obtenerRolYNombrePorUsuarioYGrupo(int usuarioId, int grupoId) {
	        return usuarioGrupoRepository.findByUsuarioIdAndGrupoId(usuarioId, grupoId);
	    }

}
