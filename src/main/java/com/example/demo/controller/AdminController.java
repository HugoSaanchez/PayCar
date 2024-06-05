package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.example.demo.entity.Comentario;
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
	                         @RequestParam(value = "ordenado", defaultValue = "sinFiltro") String ordenado,
	                         Model model) {
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

	    Map<String, String> valoraciones = new HashMap<>();
	    for (Usuario usuario : usuarios) {
	        double mediaValoracion = usuarioService.obtenerMediaValoracionUsuario(usuario.getId());
	        String mediaTruncada = String.format("%.2f", mediaValoracion).replace(",", "."); // Reemplazar coma por punto
	        String userUsername = usuario.getUsername(); // Usar username como clave
	        valoraciones.put(userUsername, mediaTruncada);
	    }

	    usuarios = adminService.ordenarUsuariosPorValoracion(usuarios, ordenado);

	    model.addAttribute("usuario", username);
	    model.addAttribute("usuarios", usuarios);
	    model.addAttribute("valoraciones", valoraciones);
	    System.out.println(valoraciones);
	    return "/admin/usuario";
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

	@GetMapping("/comentarios/conductor/{id}")
	public String encontrarComentariosPorIdConductor(@PathVariable("id") int idConductor, Model model) {

	    List<Comentario> comentarios = adminService.encontrarComentariosPorIdConductor(idConductor);
	    if (!comentarios.isEmpty()) {
	        // Si hay comentarios, procesa la lista
	        Usuario conductor = comentarios.get(0).getConductor();
	        String nombreConductor = conductor.getNombre();
	        model.addAttribute("nombreConductor", nombreConductor);
	    }

	    model.addAttribute("comentarios", comentarios);

	    return "admin/comentarios"; // Cambia la vista a la que se redirige
	}





}
