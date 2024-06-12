package com.example.demo.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Comentario;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.Valoracion;
import com.example.demo.repository.ComentarioRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.ValoracionRepository;
import com.example.demo.service.AdminService;
import com.example.demo.service.GrupoService;
import com.example.demo.service.UsuarioService;

import jakarta.persistence.EntityNotFoundException;

@Service("adminService")
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ValoracionRepository valoracionRepository;
	
	@Autowired
	private ComentarioRepository comentarioRepository;
	
	@Autowired
	private GrupoService grupoService;
	@Autowired
	private UsuarioService usuarioService;

	@Override
	public List<Usuario> findByRol(String rol) {
		return usuarioRepository.findByRol(rol);

	}

	 @Override
	    public void borrarUsuario(int id) {
	        Usuario usuario = usuarioRepository.findById(id);

	        // Verificar si se encontró el usuario
	        if (usuario != null) {
	            // Eliminar relaciones del usuario con los grupos
	            grupoService.eliminarUsuarioDeGrupos(usuario);

	            // Actualizar el campo "borrado" a true
	            usuario.setBorrado(true);

	            // Guardar los cambios en la base de datos
	            usuarioRepository.save(usuario);
	        } else {
	            // Manejar el caso en que no se encuentre el usuario
	            throw new EntityNotFoundException("Usuario no encontrado con ID: " + id);
	        }
	    }

	@Override
	public void activarUsuario(int id) {
		Usuario usuario = usuarioRepository.findById(id);

		if (usuario != null) {
			if (usuario.isActivado())
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

	
	

	
	@Override
    public List<Usuario> ordenarUsuariosPorValoracion(List<Usuario> usuarios, String ordenado) {
        if ("mayorMenor".equals(ordenado)) {
            usuarios.sort((u1, u2) -> Double.compare(
                obtenerMediaValoracionConductor(u2.getId()),
                obtenerMediaValoracionConductor(u1.getId())
            ));
        } else if ("menorMayor".equals(ordenado)) {
            usuarios.sort((u1, u2) -> Double.compare(
                obtenerMediaValoracionConductor(u1.getId()),
                obtenerMediaValoracionConductor(u2.getId())
            ));
        }
        return usuarios;
    }
    
    @Override
    public double obtenerMediaValoracionConductor(int conductorId) {
        Iterable<Valoracion> valoraciones = valoracionRepository.findByConductorId(conductorId);
        int totalValoraciones = 0;
        int totalPuntuacion = 0;
        for (Valoracion valoracion : valoraciones) {
            totalValoraciones++;
            totalPuntuacion += valoracion.getValoracion();
        }
        return totalValoraciones > 0 ? (double) totalPuntuacion / totalValoraciones : 0;
    }   
    
    @Override
    public List<Comentario> encontrarComentariosPorIdConductor(int idConductor) {
        List<Comentario> comentarios = comentarioRepository.findComentariosAndPasajeroByConductorId(idConductor);
        for (Comentario comentario : comentarios) {
        	Usuario conductor = comentario.getConductor();
            Usuario pasajero = comentario.getPasajero();
            String textoComentario = comentario.getComentario();
           
            
        }
        return comentarios;
    }

    @Override
    public Map<String, Object> obtenerDatosUsuario(String estado, String ordenado) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String username = usuarioService.findUsernameByEmail(email);

        List<Usuario> usuarios;
        if ("activados".equals(estado)) {
            usuarios = findByEstado("activados");
        } else if ("desactivados".equals(estado)) {
            usuarios = findByEstado("desactivados");
        } else {
            usuarios = findByRol("ROL_USER");
        }

        Map<String, String> valoraciones = new HashMap<>();
        for (Usuario usuario : usuarios) {
            double mediaValoracion = usuarioService.obtenerMediaValoracionUsuario(usuario.getId());
            String mediaTruncada = String.format("%.2f", mediaValoracion).replace(",", "."); // Reemplazar coma por punto
            String userUsername = usuario.getUsername(); // Usar username como clave
            valoraciones.put(userUsername, mediaTruncada);
        }

        usuarios = ordenarUsuariosPorValoracion(usuarios, ordenado);

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("usuario", username);
        resultado.put("usuarios", usuarios);
        resultado.put("valoraciones", valoraciones);

        return resultado;
    }

    @Override
    public Map<String, Object> mostrarComentariosPorIdConductor(int idConductor) {
        List<Comentario> comentarios = comentarioRepository.findComentariosAndPasajeroByConductorId(idConductor);
        Map<String, Object> result = new HashMap<>();
        if (!comentarios.isEmpty()) {
            Usuario conductor = comentarios.get(0).getConductor();
            String nombreConductor = conductor.getNombre();
            result.put("nombreConductor", nombreConductor);
        }
        result.put("comentarios", comentarios);
        return result;
    }
    
    
}
	

  




