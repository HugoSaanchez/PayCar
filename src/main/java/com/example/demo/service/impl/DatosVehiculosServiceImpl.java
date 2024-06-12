package com.example.demo.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.DatosVehiculos;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.DatosVehiculosRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.service.DatosVehiculosService;

import jakarta.transaction.Transactional;

@Service("datosVehiculosService")
public class DatosVehiculosServiceImpl implements DatosVehiculosService {

    @Autowired
    private DatosVehiculosRepository datosVehiculosRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<String> getMarcas() {
        return datosVehiculosRepository.findDistinctMarcas();
    }
    @Override
    public List<Integer> getAñosByMarca(String marca) {
        return datosVehiculosRepository.findDistinctAñosByMarca(marca);
    }
    @Override
    public List<String> getModelosByMarcaAndAnio(String marca, int anio) {
        return datosVehiculosRepository.findModelosByMarcaAndAnio(marca, anio);
    }
    @Override
    public List<String> getVersionByMarcaAndAnioAndModelo(String marca, int anio, String modelo) {
        return datosVehiculosRepository.findVersionByMarcaAndAnioAndModelo(marca, anio, modelo);
    }
    @Override
    public double getMixtoByMarcaAnioModeloAndVersion(String marca, int anio, String modelo, String version) {
        String datos = datosVehiculosRepository.findMixtoByMarcaAnioModeloAndVersion(marca, anio, modelo, version);

        // Extraer el valor numérico del String
        if (datos != null && datos.contains("l/100Km")) {
            try {
                // Quitar "l/100Km" y convertir el valor a double
                double mixtoDouble = Double.parseDouble(datos.replace("l/100Km", "").trim());
                return mixtoDouble;
            } catch (NumberFormatException e) {
                // Manejar error de formato si es necesario
                e.printStackTrace();
            }
        }
        return 0; // o algún valor por defecto o lanzar una excepción // o algún valor por defecto o lanzar una excepción
     }

    @Override
    @Transactional
    public Map<String, Object> getAlquiladoByMarcaAnioModeloAndVersion(String marca, int anio, String modelo, String version) {
        List<Object[]> results = datosVehiculosRepository.findAlquiladoAndFechasByMarcaAnioModeloAndVersion(marca, anio, modelo, version);

        Map<String, Object> response = new HashMap<>();
        if (results != null && !results.isEmpty()) {
            Object[] result = results.get(0);
            Boolean alquilado = (Boolean) result[0];
            Date fechaInicio = (Date) result[1];
            Date fechaFin = (Date) result[2];

            // Comprobar si la fecha actual es mayor que la fecha de fin
            if (fechaFin != null && fechaFin.toLocalDate().isBefore(LocalDate.now())) {
                alquilado = false;
                // Actualizar el estado de alquilado en la base de datos
                DatosVehiculos vehiculo = datosVehiculosRepository.findByMarcaAndAnioAndModeloAndVersion(marca, anio, modelo, version);
                vehiculo.setAlquilado(false);
                vehiculo.setFecha_inicio(null);  
                vehiculo.setFecha_fin(null);   
                vehiculo.setUsuario(null);
                datosVehiculosRepository.save(vehiculo);
            }

            response.put("alquilado", alquilado ? 1 : 0);
            response.put("fechaInicio", fechaInicio != null ? fechaInicio.toString() : null);
            response.put("fechaFin", fechaFin != null ? fechaFin.toString() : null);
        } else {
            response.put("alquilado", 0);
        }

        return response;
    }
    
    @Override	
    @Transactional
    public boolean alquilarVehiculo(String marca, int anio, String modelo, String version, String fechaInicio, String fechaFin, Usuario usuario) {
        try {
            DatosVehiculos vehiculo = datosVehiculosRepository.findByMarcaAndAnioAndModeloAndVersion(marca, anio, modelo, version);
            if (vehiculo != null) {
                vehiculo.setAlquilado(true);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                vehiculo.setFecha_inicio(java.sql.Date.valueOf(LocalDate.parse(fechaInicio, formatter)));
                vehiculo.setFecha_fin(java.sql.Date.valueOf(LocalDate.parse(fechaFin, formatter)));
                vehiculo.setUsuario(usuario);
                datosVehiculosRepository.save(vehiculo);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public List<DatosVehiculos> getVehiculosAlquiladosPorUsuario(Usuario usuario) {
        return datosVehiculosRepository.findByUsuario(usuario);
    }
    
    @Override
    public List<DatosVehiculos> getAllDatosVehiculos() {
        return datosVehiculosRepository.findAll();
    }
    
    @Override
    @Transactional
    public Map<String, Object> actualizarEstadoAlquilado() {
        List<DatosVehiculos> vehiculosAlquilados = datosVehiculosRepository.findAllAlquilados();
        Map<String, Object> response = new HashMap<>();

        for (DatosVehiculos vehiculo : vehiculosAlquilados) {
            if (vehiculo.getFecha_fin() != null && vehiculo.getFecha_fin().toLocalDate().isBefore(LocalDate.now())) {
                vehiculo.setAlquilado(false);
                vehiculo.setFecha_inicio(null);
                vehiculo.setFecha_fin(null);
                vehiculo.setUsuario(null);
                datosVehiculosRepository.save(vehiculo);
            }
        }

        response.put("actualizados", vehiculosAlquilados.size());
        return response;
    }
   

    @Override
    public Map<String, Object> obtenerDatosFiltrados(String email, List<String> marcas, String consumo, String ordenar, List<String> usuarios, String fechaInicio, String fechaFin) {
        Map<String, Object> resultado = new HashMap<>();
        String username = usuarioRepository.findByUsername(email).getNombre();
        resultado.put("usuario", username);

        List<DatosVehiculos> datosVehiculos = datosVehiculosRepository.findAll();
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

            filteredDatosVehiculos = datosVehiculos.stream()
                    .filter(dv -> dv.getFecha_fin() != null)
                    .filter(dv -> {
                        LocalDate fechaFinVehiculo = dv.getFecha_fin().toLocalDate();
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

        resultado.put("datosVehiculos", filteredDatosVehiculos);
        resultado.put("marcas", todasLasMarcas);
        resultado.put("selectedMarcas", finalMarcas);
        resultado.put("selectedConsumo", consumo);
        resultado.put("ordenar", ordenar);
        resultado.put("usuariosConAlquiler", usuariosConAlquiler);
        resultado.put("selectedUsuarios", usuarios);

        return resultado;
    }
    
    
    @Override
    public ResponseEntity<String> alquilarVehiculo(String marca, int anio, String modelo, String version, String fechaInicio, String fechaFin, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        try {
            DatosVehiculos vehiculo = datosVehiculosRepository.findByMarcaAndAnioAndModeloAndVersion(marca, anio, modelo, version);
            if (vehiculo != null) {
                vehiculo.setAlquilado(true);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                vehiculo.setFecha_inicio(java.sql.Date.valueOf(LocalDate.parse(fechaInicio, formatter)));
                vehiculo.setFecha_fin(java.sql.Date.valueOf(LocalDate.parse(fechaFin, formatter)));
                vehiculo.setUsuario(usuario);
                datosVehiculosRepository.save(vehiculo);
                return ResponseEntity.ok("Vehículo alquilado exitosamente");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al alquilar el vehículo");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al alquilar el vehículo");
        }
    }
    
    @Override
    public ResponseEntity<List<Map<String, Object>>> obtenerVehiculosAlquilados(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<DatosVehiculos> vehiculosAlquilados = datosVehiculosRepository.findByUsuario(usuario);

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (DatosVehiculos vehiculo : vehiculosAlquilados) {
            Map<String, Object> vehiculoMap = new HashMap<>();
            vehiculoMap.put("id", vehiculo.getId());
            vehiculoMap.put("marca", vehiculo.getMarca());
            vehiculoMap.put("anio", vehiculo.getAnio());
            vehiculoMap.put("modelo", vehiculo.getModelo());
            vehiculoMap.put("version", vehiculo.getVersion());
            vehiculoMap.put("mixto", vehiculo.getMixto());
            vehiculoMap.put("alquilado", vehiculo.getAlquilado());
            vehiculoMap.put("fecha_inicio", vehiculo.getFecha_inicio());
            vehiculoMap.put("fecha_fin", vehiculo.getFecha_fin());
            vehiculoMap.put("usuario_id", vehiculo.getUsuario().getId());

            resultado.add(vehiculoMap);
        }

        return ResponseEntity.ok(resultado);
    }
    
}