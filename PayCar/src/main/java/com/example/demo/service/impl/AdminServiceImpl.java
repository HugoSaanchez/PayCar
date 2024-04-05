package com.example.demo.service.impl;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Usuario;
import com.example.demo.entity.ValoracionComentario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.ValoracionComentarioRepository;
import com.example.demo.service.AdminService;

import jakarta.persistence.EntityNotFoundException;

@Service("adminService")
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ValoracionComentarioRepository valoracionComentarioRepository;

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
			usuario.setBorrado(true);
			;
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
	 public List<String> encontrarComentariosPorConductor(int idConductor) {
        System.out.println(valoracionComentarioRepository.findComentarioByConductorId(idConductor));
        return valoracionComentarioRepository.findComentarioByConductorId(idConductor);
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
        Iterable<ValoracionComentario> valoraciones = valoracionComentarioRepository.findByConductorId(conductorId);
        int totalValoraciones = 0;
        int totalPuntuacion = 0;
        for (ValoracionComentario valoracion : valoraciones) {
            totalValoraciones++;
            totalPuntuacion += valoracion.getValoracion();
        }
        return totalValoraciones > 0 ? (double) totalPuntuacion / totalValoraciones : 0;
    }
}
	

  




