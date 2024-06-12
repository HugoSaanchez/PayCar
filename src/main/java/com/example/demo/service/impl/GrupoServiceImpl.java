package com.example.demo.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.Invitacion;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.UsuarioGrupo;
import com.example.demo.repository.ComentarioRepository;
import com.example.demo.repository.GrupoRepository;
import com.example.demo.repository.InvitacionRepository;
import com.example.demo.repository.UsuarioGrupoRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.ValoracionRepository;
import com.example.demo.service.GrupoService;

import jakarta.transaction.Transactional;

@Service("grupoService")
public class GrupoServiceImpl implements GrupoService {

	@Autowired
	private GrupoRepository grupoRepository;

	@Autowired
	private InvitacionRepository invitacionRepository;

	@Autowired
	private ComentarioRepository comentarioRepository;

	@Autowired
	private ValoracionRepository valoracionRepository;

	@Autowired
	private UsuarioGrupoRepository usuarioGrupoRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;

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
		return usuarioGrupos.stream().map(UsuarioGrupo::getGrupo).collect(Collectors.toList());
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
		// Eliminar todos los comentarios asociados al grupo
		comentarioRepository.deleteByGrupoId(id);

		valoracionRepository.deleteByGrupoId(id);
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

	@Override
	public void eliminarUsuarioGrupo(UsuarioGrupo usuarioGrupo) {
		usuarioGrupoRepository.delete(usuarioGrupo);
	}
	
	@Override
    public void eliminarUsuarioDeGrupos(Usuario usuario) {
        List<UsuarioGrupo> usuarioGrupos = usuarioGrupoRepository.findByUsuario(usuario);
        for (UsuarioGrupo usuarioGrupo : usuarioGrupos) {
            usuarioGrupoRepository.delete(usuarioGrupo);
        }
    }
	
	@Override
    public void salirGrupo(Usuario usuario, int grupoId) {
        Optional<UsuarioGrupo> usuarioGrupo = usuarioGrupoRepository.findByUsuarioAndGrupoId(usuario, grupoId);
        usuarioGrupo.ifPresent(usuarioGrupoRepository::delete);
    }
	
	@Override
    public Map<String, Object> obtenerGruposYUsuarios(String estado, Integer grupoId, String email) {
        Map<String, Object> resultado = new HashMap<>();
        String username = usuarioRepository.findByUsername(email).getNombre();
        
        resultado.put("usuario", username);

        if (grupoId != null) {
            List<UsuarioGrupo> usuariosGrupo = obtenerUsuariosPorGrupoId(grupoId);
            resultado.put("usuariosGrupo", usuariosGrupo);
        }

        List<Grupo> grupos;
        if ("activados".equals(estado)) {
            grupos = findByEstado("activados");
        } else if ("desactivados".equals(estado)) {
            grupos = findByEstado("desactivados");
        } else {
            grupos = obtenerGrupos();
        }

        resultado.put("grupos", grupos);
        return resultado;
    }
	
	@Override
    public Map<String, Object> crearGrupoYUsuarioGrupo(Grupo grupo, Usuario usuario) {
        Map<String, Object> resultado = new HashMap<>();

        // Guarda el grupo para obtener un ID generado
        Grupo nuevoGrupo = crearGrupo(grupo);

        // Crea un UsuarioGrupo con el usuario como conductor y el grupo recién creado
        UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
        usuarioGrupo.setUsuario(usuario);
        usuarioGrupo.setGrupo(nuevoGrupo);
        usuarioGrupo.setRol("conductor");

        // Guarda el UsuarioGrupo
        crearUsuarioGrupo(usuarioGrupo);

        resultado.put("grupo", nuevoGrupo);
        resultado.put("usuarioGrupo", usuarioGrupo);

        return resultado;
    }
	
	@Override
	public String crearInvitacionParaGrupo(int grupoId) {
	    String codigoInvitacion = generarCodigoUnico();

	    Grupo grupo = obtenerGrupoPorId(grupoId);

	    Invitacion invitacion = new Invitacion();
	    invitacion.setCodigoInvitacion(codigoInvitacion);
	    invitacion.setGrupo(grupo);

	    guardarInvitacion(invitacion);

	    String enlaceInvitacion = "http://localhost:8080/unirse-grupo?codigo=" + codigoInvitacion;

	    return enlaceInvitacion;
	}

	@Override
    public String unirseAGrupo(String codigoInvitacion, Usuario usuario) {
        Invitacion invitacion = obtenerGrupoPorCodigo(codigoInvitacion);

        if (invitacion != null) {
            int grupoId = invitacion.getGrupo().getId();

            Optional<UsuarioGrupo> usuarioGrupoExistente = obtenerRolYNombrePorUsuarioYGrupo(usuario.getId(), grupoId);
            if (usuarioGrupoExistente.isPresent()) {
                return "Ya estás en este grupo.";
            }

            UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
            usuarioGrupo.setUsuario(usuario);
            usuarioGrupo.setGrupo(obtenerGrupoPorId(grupoId));
            usuarioGrupo.setRol("pasajero");

            crearUsuarioGrupo(usuarioGrupo);

            return "Te has unido al grupo exitosamente.";
        } else {
            return "Código de invitación no válido.";
        }
    }
	
	@Override
	public List<Map<String, Object>> obtenerGruposDelUsuario(Usuario usuario) {
	    List<Grupo> grupos = obtenerGruposPorUsuario(usuario);
	    return grupos.stream().map(grupo -> Map.of(
	            "id", grupo.getId(),
	            "titulo", grupo.getTitulo(),
	            "descripcion", grupo.getDescripcion(),
	            "consumoGasolina", grupo.getConsumoGasolina(),
	            "kilometrosRecorridos", grupo.getKilometrosRecorridos(),
	            "dineroGasolina", grupo.getDineroGasolina(),
	            "activado", grupo.isActivado(),
	            "borrado", grupo.isBorrado(),
	            "usuarios", grupo.getUsuarios().stream()
	                    .map(usuarioGrupo -> usuarioGrupo.getUsuario().getId())
	                    .collect(Collectors.toList())
	    )).collect(Collectors.toList());
	}
	
	@Override
	public Map<String, Object> getRolYNombrePorUsuarioYGrupoResponse(int usuarioId, int grupoId) {
	    Optional<UsuarioGrupo> usuarioGrupoOptional = obtenerRolYNombrePorUsuarioYGrupo(usuarioId, grupoId);

	    Map<String, Object> response = new HashMap<>();
	    if (usuarioGrupoOptional.isPresent()) {
	        UsuarioGrupo usuarioGrupo = usuarioGrupoOptional.get();
	        response.put("id", usuarioGrupo.getUsuario().getId());
	        response.put("nombre", usuarioGrupo.getUsuario().getNombre());
	        response.put("rol", usuarioGrupo.getRol());
	    } else {
	        response.put("error", "No se encontró el usuario en el grupo especificado");
	    }

	    return response;
	}


}
