package com.example.demo.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.DatosVehiculos;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.DatosVehiculosRepository;
import com.example.demo.service.DatosVehiculosService;

import jakarta.transaction.Transactional;

@Service("datosVehiculosService")
public class DatosVehiculosServiceImpl implements DatosVehiculosService {

    @Autowired
    private DatosVehiculosRepository datosVehiculosRepository;

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
                vehiculo.setFecha_inicio(null);  // Borrar fecha de inicio
                vehiculo.setFecha_fin(null);     // Borrar fecha de fin
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
    
   

    
    
    
}