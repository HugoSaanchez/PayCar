package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.UsuarioGrupo;
import com.example.demo.service.GrupoService;
import com.example.demo.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class GrupoController {

    @Autowired
    private GrupoService grupoService;
    
	@Autowired
	private UsuarioService usuarioService;

	@GetMapping("/grupo")
	public String obtenerGrupos(
	    @RequestParam(value = "estado", defaultValue = "todos") String estado,
	    @RequestParam(required = false, name = "grupoId") Integer grupoId,
	    Model model) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String email = authentication.getName();
	    String username = usuarioService.findUsernameByEmail(email);

	    if (grupoId != null) {
	        List<UsuarioGrupo> usuariosGrupo = grupoService.obtenerUsuariosPorGrupoId(grupoId);
	        System.out.println(usuariosGrupo);
	        model.addAttribute("usuariosGrupo", usuariosGrupo);
	    }

	    List<Grupo> grupos;
	    if ("activados".equals(estado)) {
	        grupos = grupoService.findByEstado("activados");
	    } else if ("desactivados".equals(estado)) {
	        grupos = grupoService.findByEstado("desactivados");
	    } else {
	        grupos = grupoService.obtenerGrupos();
	    }

	    model.addAttribute("grupos", grupos);
	    model.addAttribute("usuario", username);
	    model.addAttribute("estado", estado);
	    return "admin/grupo"; // Nombre de la vista donde mostrar los grupos
	}

    
    @PostMapping("/grupo/activar/{id}")
    public String activarGrupo(@PathVariable("id") int grupoId) {
        grupoService.activarGrupo(grupoId);
        return "redirect:/admin/grupo";
    }

    @PostMapping("/grupo/desactivar/{id}")
    public String desactivarGrupo(@PathVariable("id") int grupoId) {
        grupoService.desactivarGrupo(grupoId);
        return "redirect:/admin/grupo";
    }
    
    @PostMapping("/grupo/borrar/{id}")
    public ResponseEntity<?> borrarGrupo(@PathVariable("id") int id) {
        try {
        	
           // Eliminar comentarios relacionados
            grupoService.borrarGrupo(id); // Luego eliminar el grupo
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al borrar el grupo: " + e.getMessage());
        }
    }

}
