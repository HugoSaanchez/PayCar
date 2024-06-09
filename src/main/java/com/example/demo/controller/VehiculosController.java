package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.DatosVehiculos;
import com.example.demo.service.DatosVehiculosService;
import com.example.demo.service.UsuarioService;

@Controller
@RequestMapping("/admin")
public class VehiculosController {

    @Autowired
    private DatosVehiculosService datosVehiculosService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/coches")
    public String verTodosLosDatos(Model model) {
        List<DatosVehiculos> datosVehiculos = datosVehiculosService.getAllDatosVehiculos();
        model.addAttribute("datosVehiculos", datosVehiculos);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String email = authentication.getName();
	    String username = usuarioService.findUsernameByEmail(email);
	    model.addAttribute("usuario", username);
        return "admin/coches"; // Nombre de la vista que mostrará los datos
    }

}
