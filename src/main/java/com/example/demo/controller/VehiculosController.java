package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String verTodosLosDatos(@RequestParam(required = false) List<String> marcas,
                                   @RequestParam(value = "consumo", required = false) String consumo,
                                   @RequestParam(value = "ordenar", required = false) String ordenar,
                                   @RequestParam(value = "usuarios", required = false) List<String> usuarios,
                                   @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                   @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                   Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Map<String, Object> datos = datosVehiculosService.obtenerDatosFiltrados(email, marcas, consumo, ordenar, usuarios, fechaInicio, fechaFin);

        model.addAttribute("datosVehiculos", datos.get("datosVehiculos"));
        model.addAttribute("marcas", datos.get("marcas"));
        model.addAttribute("selectedMarcas", datos.get("selectedMarcas"));
        model.addAttribute("selectedConsumo", datos.get("selectedConsumo"));
        model.addAttribute("ordenar", datos.get("ordenar"));
        model.addAttribute("usuariosConAlquiler", datos.get("usuariosConAlquiler"));
        model.addAttribute("selectedUsuarios", datos.get("selectedUsuarios"));
        model.addAttribute("usuario", datos.get("usuario"));

        return "admin/coches";
    }
}




