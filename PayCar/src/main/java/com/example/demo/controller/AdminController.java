package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Usuario;
import com.example.demo.service.AdminService;
import com.example.demo.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private AdminService adminService;

	@GetMapping("/usuario")
	public String mainLayout(@RequestParam(value = "estado", defaultValue = "todos") String estado,
			@RequestParam(value = "ordenado", defaultValue = "sinFiltro") String ordenado, Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		String username = usuarioService.findUsernameByEmail(email);

		List<Usuario> usuarios;
		if ("activados".equals(estado)) {
			usuarios = adminService.findByEstado("activados");
		} else if ("desactivados".equals(estado)) {
			usuarios = adminService.findByEstado("desactivados");
		} else {
			usuarios = adminService.findByRol("ROL_USER");
		}

		for (Usuario usuario : usuarios) {
			double mediaValoracionUsuario = adminService.obtenerMediaValoracionConductor(usuario.getId());
			String mediaTruncada = String.format("%.2f", mediaValoracionUsuario);
			model.addAttribute("mediaValoracionUsuario_" + usuario.getId(), mediaTruncada);
			List<String> comentarios = adminService.encontrarComentariosPorConductor(usuario.getId());
			model.addAttribute("comentarios_" + usuario.getId(), comentarios);
		}

		usuarios = adminService.ordenarUsuariosPorValoracion(usuarios, ordenado);

		model.addAttribute("usuario", username);
		model.addAttribute("usuarios", usuarios);
		return "admin/usuario";
	}

	@PostMapping("/borrar/{id}")
	public String borrarUsuario(@PathVariable("id") int id) {
		adminService.borrarUsuario(id);
		return "redirect:/admin/usuario";
	}

	@PostMapping("/activar/{id}")
	public String activarUsuario(@PathVariable("id") int id) {
		adminService.activarUsuario(id);
		return "redirect:/admin/usuario";
	}

}
