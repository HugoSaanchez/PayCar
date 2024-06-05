package com.example.demo.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import jakarta.transaction.Transactional;

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
	 
	 @Override
	  public Grupo actualizarGrupo(Grupo grupo) {
	        return grupoRepository.save(grupo);
	    }
	 @Override
	    public void actualizarUsuarioGrupo(UsuarioGrupo usuarioGrupo) {
	        usuarioGrupoRepository.save(usuarioGrupo);
	    }
	 @Override
	 public Map<String, Float> calcularDiferenciaCoste(int grupoId) {
	     Grupo grupo = obtenerGrupoPorId(grupoId);
	     if (grupo == null) {
	         return null;
	     }
	     
	     Map<String, Float> diferencias = new HashMap<>();
	     for (UsuarioGrupo usuarioGrupo : grupo.getUsuarios()) {
	         float diferencia;
	         if ("conductor".equals(usuarioGrupo.getRol())) {
	             diferencia = usuarioGrupo.getCostetotal() - usuarioGrupo.getCostepagado();
	         } else {
	             diferencia = usuarioGrupo.getCostepagado() - usuarioGrupo.getCostetotal();
	         }
	         diferencias.put(usuarioGrupo.getUsuario().getNombre(), diferencia);
	     }
	     
	     return diferencias;
	 }
	 
	 @Override
	 public Invitacion obtenerInvitacionPorGrupoId(int grupoId) {
		    return invitacionRepository.findByGrupoId(grupoId);
		}
	 
	 @Override
	 public void activarGrupo(int grupoId) {
	        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
	        grupo.setActivado(true);
	        grupoRepository.save(grupo);
	    }
	 @Override
	    public void desactivarGrupo(int grupoId) {
	        Grupo grupo = grupoRepository.findById(grupoId).orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
	        grupo.setActivado(false);
	        grupoRepository.save(grupo);
	    }
	 	@Override
	 	@Transactional
	    public void borrarGrupo(int id) {
	        // Eliminar todas las invitaciones asociadas al grupo
	        invitacionRepository.deleteByGrupoId(id);
	        
	        // Eliminar el grupo
	        grupoRepository.deleteById(id);
	    }
		@Override
		public List<Grupo> findByEstado(String estado) {
			if ("activados".equals(estado)) {
				return grupoRepository.findByActivado(true);
			} else if ("desactivados".equals(estado)) {
				return grupoRepository.findByActivado(false);
			}
			return null; 

		}


}
