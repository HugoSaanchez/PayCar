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
        Map<String, Object> datosUsuario = adminService.obtenerDatosUsuario(estado, ordenado);

        model.addAttribute("usuario", datosUsuario.get("usuario"));
        model.addAttribute("usuarios", datosUsuario.get("usuarios"));
        model.addAttribute("valoraciones", datosUsuario.get("valoraciones"));

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

    @GetMapping("/comentarios/conductor/{id}")
    public String encontrarComentariosPorIdConductor(@PathVariable("id") int idConductor, Model model) {
        Map<String, Object> datos = adminService.mostrarComentariosPorIdConductor(idConductor);

        if (datos.containsKey("nombreConductor")) {
            model.addAttribute("nombreConductor", datos.get("nombreConductor"));
        }

        model.addAttribute("comentarios", datos.get("comentarios"));

        return "admin/comentarios";
    }




}
