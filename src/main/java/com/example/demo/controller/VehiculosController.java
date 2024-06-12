package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.DatosVehiculos;
import com.example.demo.service.DatosVehiculosService;
import com.example.demo.service.UsuarioService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String verTodosLosDatos(@RequestParam(required = false) List<String> marcas,
                                   @RequestParam(value = "consumo", required = false) String consumo,
                                   @RequestParam(value = "ordenar", required = false) String ordenar,
                                   @RequestParam(value = "usuarios", required = false) List<String> usuarios,
                                   @RequestParam(value = "fechaInicio", required = false) String fechaInicio,
                                   @RequestParam(value = "fechaFin", required = false) String fechaFin,
                                   Model model) {
        List<DatosVehiculos> datosVehiculos = datosVehiculosService.getAllDatosVehiculos();
        List<String> todasLasMarcas = datosVehiculos.stream()
                .map(DatosVehiculos::getMarca)
                .distinct()
                .collect(Collectors.toList());

        if (marcas == null || marcas.isEmpty()) {
            if (!todasLasMarcas.isEmpty()) {
                marcas = new ArrayList<>();
                marcas.add(todasLasMarcas.get(0));
            } else {
                marcas = new ArrayList<>();
            }
        }

        List<String> finalMarcas = marcas;
        List<DatosVehiculos> filteredDatosVehiculos = datosVehiculos.stream()
                .filter(dv -> dv.getMarca() != null && (finalMarcas.isEmpty() || finalMarcas.contains(dv.getMarca())))
                .collect(Collectors.toList());

        if (consumo != null && !consumo.isEmpty()) {
            filteredDatosVehiculos = filteredDatosVehiculos.stream()
                    .filter(dv -> dv.getMixto() != null && dv.getMixto().equals(consumo))
                    .collect(Collectors.toList());
        }

        if (usuarios != null && !usuarios.isEmpty()) {
            List<String> usuariosEspecificos = usuarios.stream().filter(u -> !u.equals("No alquilado")).collect(Collectors.toList());
            if (!usuariosEspecificos.isEmpty()) {
                filteredDatosVehiculos = datosVehiculos.stream()
                        .filter(dv -> dv.getUsuario() != null && usuariosEspecificos.contains(dv.getUsuario().getNombre()))
                        .collect(Collectors.toList());
            } else {
                filteredDatosVehiculos = filteredDatosVehiculos.stream()
                        .filter(dv -> dv.getUsuario() == null && usuarios.contains("No alquilado") && finalMarcas.contains(dv.getMarca()) ||
                                dv.getUsuario() != null && (usuarios.contains(dv.getUsuario().getNombre()) || usuarios.contains("No alquilado")))
                        .collect(Collectors.toList());
            }
        }

        if (fechaInicio != null && !fechaInicio.isEmpty() && fechaFin != null && !fechaFin.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(fechaInicio, formatter).plusDays(1);
            LocalDate endDate = LocalDate.parse(fechaFin, formatter).plusDays(1);
            System.out.println(startDate + " + " + endDate );

            filteredDatosVehiculos = datosVehiculos.stream()
                    .filter(dv -> dv.getFecha_fin() != null)
                    .filter(dv -> {
                        LocalDate fechaFinVehiculo = dv.getFecha_fin().toLocalDate();
                        System.out.println((fechaFinVehiculo.isAfter(startDate) || fechaFinVehiculo.isEqual(startDate)) &&
                               (fechaFinVehiculo.isBefore(endDate) || fechaFinVehiculo.isEqual(endDate)));
                        return (fechaFinVehiculo.isAfter(startDate) || fechaFinVehiculo.isEqual(startDate)) &&
                               (fechaFinVehiculo.isBefore(endDate) || fechaFinVehiculo.isEqual(endDate));
                    })
                    .collect(Collectors.toList());
        }

        if (ordenar == null || ordenar.equals("id")) {
            filteredDatosVehiculos.sort(Comparator.comparing(DatosVehiculos::getId));
        } else if ("mayor".equals(ordenar)) {
            filteredDatosVehiculos.sort((dv1, dv2) -> {
                Double consumo1 = Double.valueOf(dv1.getMixto().replace("l/100Km", "").trim());
                Double consumo2 = Double.valueOf(dv2.getMixto().replace("l/100Km", "").trim());
                return consumo2.compareTo(consumo1);
            });
        } else if ("menor".equals(ordenar)) {
            filteredDatosVehiculos.sort((dv1, dv2) -> {
                Double consumo1 = Double.valueOf(dv1.getMixto().replace("l/100Km", "").trim());
                Double consumo2 = Double.valueOf(dv2.getMixto().replace("l/100Km", "").trim());
                return consumo1.compareTo(consumo2);
            });
        }

        List<String> usuariosConAlquiler = datosVehiculos.stream()
                .filter(dv -> dv.getUsuario() != null)
                .map(dv -> dv.getUsuario().getNombre())
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("datosVehiculos", filteredDatosVehiculos);
        model.addAttribute("marcas", todasLasMarcas);
        model.addAttribute("selectedMarcas", finalMarcas);
        model.addAttribute("selectedConsumo", consumo);
        model.addAttribute("ordenar", ordenar);
        model.addAttribute("usuariosConAlquiler", usuariosConAlquiler);
        model.addAttribute("selectedUsuarios", usuarios);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String username = usuarioService.findUsernameByEmail(email);
        model.addAttribute("usuario", username);

        return "admin/coches";
    }
}




