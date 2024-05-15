package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.Invitacion;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.UsuarioGrupo;
import com.example.demo.service.GrupoService;
import com.example.demo.service.UsuarioService;

@RestController
public class GrupoApiController {

	@Autowired
	private GrupoService grupoService;

	@Autowired
	private UsuarioService usuarioService;

	@PostMapping("/crear-grupo")
	public String crearGrupo(@RequestBody Grupo grupo, Model model) {
		// Obtén el usuario autenticado de Spring Security
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		Usuario usuario = usuarioService.findByUsername(username);

		if (usuario == null) {

			return "error";
		}

		// Guarda el grupo para obtener un ID generado
		Grupo nuevoGrupo = grupoService.crearGrupo(grupo);

		// Crea un UsuarioGrupo con el usuario como conductor y el grupo recién creado
		UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
		usuarioGrupo.setUsuario(usuario);
		usuarioGrupo.setGrupo(nuevoGrupo);
		usuarioGrupo.setRol("conductor");

		// Guarda el UsuarioGrupo
		grupoService.crearUsuarioGrupo(usuarioGrupo);

		// Agrega atributos al modelo si vas a mostrar alguna vista
		model.addAttribute("grupo", nuevoGrupo);
		model.addAttribute("usuarioGrupo", usuarioGrupo);

		// Retorna a la vista de éxito o donde sea adecuado
		return "grupo-creado"; // Asume que tienes una vista que se llama "grupo-creado"
	}

	@PostMapping("/crear-invitacion/{grupoId}")
	public String crearInvitacion(@PathVariable int grupoId, Model model) {

		String codigoInvitacion = grupoService.generarCodigoUnico();

		Grupo grupo = grupoService.obtenerGrupoPorId(grupoId);

		Invitacion invitacion = new Invitacion();
		invitacion.setCodigoInvitacion(codigoInvitacion);
		invitacion.setGrupo(grupo);

		grupoService.guardarInvitacion(invitacion);

		String enlaceInvitacion = "http://localhost:8080/unirse-grupo?codigo=" + codigoInvitacion;

		return enlaceInvitacion;
	}

	@GetMapping("/unirse-grupo")
	public String unirseGrupo(@RequestParam("codigo") String codigoInvitacion, Model model) {
		// Obtener la invitación correspondiente al código
		Invitacion invitacion = grupoService.obtenerGrupoPorCodigo(codigoInvitacion);

		// Verificar si la invitación existe
		if (invitacion != null) {
			// Obtener el ID del grupo asociado al código de invitación
			int grupoId = invitacion.getGrupo().getId();

			// Obtener el usuario autenticado
			String username = SecurityContextHolder.getContext().getAuthentication().getName();
			Usuario usuario = usuarioService.findByUsername(username);

			// Crear un UsuarioGrupo con el usuario y el grupo correspondiente
			UsuarioGrupo usuarioGrupo = new UsuarioGrupo();
			usuarioGrupo.setUsuario(usuario);
			usuarioGrupo.setGrupo(grupoService.obtenerGrupoPorId(grupoId));
			usuarioGrupo.setRol("pasajero"); // O el rol que desees asignar al usuario

			// Guardar el UsuarioGrupo
			grupoService.crearUsuarioGrupo(usuarioGrupo);

			// Redirigir a una vista de éxito o donde sea adecuado
			return "redirect:/exito";
		} else {
			// Si la invitación no existe, redirigir a una vista de error o donde sea
			// adecuado
			return "redirect:/error";
		}
	}

	@GetMapping("/grupos")
	public List<Map<String, Object>> getGruposDelUsuario() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = usuarioService.findByUsername(username);
		if (usuario != null) {
			List<Grupo> grupos = grupoService.obtenerGruposPorUsuario(usuario);
			return grupos.stream().map(grupo -> Map.of("id", grupo.getId(), "titulo", grupo.getTitulo(), "descripcion",
					grupo.getDescripcion(), "consumoGasolina", grupo.getConsumoGasolina(), "kilometrosRecorridos",
					grupo.getKilometrosRecorridos(), "dineroGasolina", grupo.getDineroGasolina(), "activado",
					grupo.isActivado(), "borrado", grupo.isBorrado(), "usuarios", grupo.getUsuarios().stream()
							.map(usuarioGrupo -> usuarioGrupo.getUsuario().getId()).collect(Collectors.toList())))
					.collect(Collectors.toList());
		} else {
			return List.of();
		}
	}

	@GetMapping("/usuario-grupo")
    public Map<String, Object> getRolYNombrePorUsuarioYGrupo(@RequestParam int usuarioId, @RequestParam int grupoId) {
        Optional<UsuarioGrupo> usuarioGrupoOptional = grupoService.obtenerRolYNombrePorUsuarioYGrupo(usuarioId, grupoId);

        Map<String, Object> response = new HashMap<>();
        if (usuarioGrupoOptional.isPresent()) {
            UsuarioGrupo usuarioGrupo = usuarioGrupoOptional.get();
            response.put("nombre", usuarioGrupo.getUsuario().getNombre());
            response.put("rol", usuarioGrupo.getRol());
        } else {
            response.put("error", "No se encontró el usuario en el grupo especificado");
        }

        return response;
    }

}
