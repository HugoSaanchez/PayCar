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
                                   @RequestParam(value = "usuario", required = false) String usuario,
                                   Model model) {
        List<DatosVehiculos> datosVehiculos = datosVehiculosService.getAllDatosVehiculos();
        List<String> todasLasMarcas = datosVehiculos.stream()
                .map(DatosVehiculos::getMarca)
                .distinct()
                .collect(Collectors.toList());

        if (marcas == null || marcas.isEmpty()) {
            marcas = new ArrayList<>();
            marcas.add(todasLasMarcas.get(0)); // Mostrar la primera marca por defecto
        }

        List<String> finalMarcas = marcas; // Asignar marcas a una variable final
        List<DatosVehiculos> filteredDatosVehiculos = datosVehiculos.stream()
                .filter(dv -> dv.getMarca() != null && finalMarcas.contains(dv.getMarca()))
                .collect(Collectors.toList());

        // Filtrar por consumo de gasolina solo entre los vehículos filtrados por marcas
        if (consumo != null && !consumo.isEmpty()) {
            filteredDatosVehiculos = filteredDatosVehiculos.stream()
                    .filter(dv -> dv.getMixto() != null && dv.getMixto().equals(consumo))
                    .collect(Collectors.toList());
        }

     

        // Filtrar por usuario si se selecciona la opción
        if (usuario != null && !usuario.isEmpty()) {
            filteredDatosVehiculos = filteredDatosVehiculos.stream()
                    .filter(dv -> dv.getUsuario() != null && dv.getUsuario().getNombre().equals(usuario))
                    .collect(Collectors.toList());
        }

        // Ordenar por consumo de gasolina si se selecciona la opción
        if ("mayor".equals(ordenar)) {
            filteredDatosVehiculos.sort((dv1, dv2) -> {
                Double consumo1 = Double.valueOf(dv1.getMixto().replace("l/100Km", "").trim());
                Double consumo2 = Double.valueOf(dv2.getMixto().replace("l/100Km", "").trim());
                return consumo2.compareTo(consumo1); // Ordenar de mayor a menor
            });
        } else if ("menor".equals(ordenar)) {
            filteredDatosVehiculos.sort((dv1, dv2) -> {
                Double consumo1 = Double.valueOf(dv1.getMixto().replace("l/100Km", "").trim());
                Double consumo2 = Double.valueOf(dv2.getMixto().replace("l/100Km", "").trim());
                return consumo1.compareTo(consumo2); // Ordenar de menor a mayor
            });
        }

        // Obtener la lista de usuarios con coches alquilados
        List<String> usuariosConAlquiler = datosVehiculos.stream()
                .filter(DatosVehiculos::getAlquilado)
                .map(dv -> dv.getUsuario() != null ? dv.getUsuario().getNombre() : "N/A")
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("datosVehiculos", filteredDatosVehiculos);
        model.addAttribute("marcas", todasLasMarcas);
        model.addAttribute("selectedMarcas", finalMarcas);
        model.addAttribute("selectedConsumo", consumo);
        model.addAttribute("ordenar", ordenar);
        model.addAttribute("usuariosConAlquiler", usuariosConAlquiler);
        model.addAttribute("selectedUsuario", usuario);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String username = usuarioService.findUsernameByEmail(email);
        model.addAttribute("usuario", username);

        return "admin/coches"; // Nombre de la vista que mostrará los datos
    }
}
