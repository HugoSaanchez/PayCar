package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
    public String obtenerGrupos(@RequestParam(required = false, name = "grupoId") Integer grupoId, Model model) {
    	 Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
   	    String email = authentication.getName();
   	    String username = usuarioService.findUsernameByEmail(email);
    	if (grupoId != null) {
            List<UsuarioGrupo> usuariosGrupo = grupoService.obtenerUsuariosPorGrupoId(grupoId);
            System.out.println(usuariosGrupo);
            model.addAttribute("usuariosGrupo", usuariosGrupo);
        }

        List<Grupo> grupos = grupoService.obtenerGrupos();
        model.addAttribute("grupos", grupos);
        model.addAttribute("usuario", username);
        return "admin/grupo"; // Nombre de la vista donde mostrar los grupos
    }
}
