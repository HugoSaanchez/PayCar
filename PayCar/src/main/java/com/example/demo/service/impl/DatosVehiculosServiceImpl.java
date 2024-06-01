package com.example.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repository.DatosVehiculosRepository;
import com.example.demo.service.DatosVehiculosService;

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


}