package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.DatosVehiculosService;

@RestController
public class DatosVehiculosController {

    @Autowired
    private DatosVehiculosService datosVehiculosService;

    @GetMapping("/marcas")
    public List<String> getMarcas() {
        return datosVehiculosService.getMarcas();
    }
    @GetMapping("/anios")
    public List<Integer> getAñosPorMarca(@RequestParam String marca) {
        return datosVehiculosService.getAñosByMarca(marca);
    }
    
    @GetMapping("/modelos")
    public List<String> getModelosPorMarcaYAño(@RequestParam String marca, @RequestParam int anio) {
        return datosVehiculosService.getModelosByMarcaAndAnio(marca, anio);
    }
    
    @GetMapping("/versiones")
    public List<String> getVersionesPorMarcaAnioYModelo(@RequestParam String marca, @RequestParam int anio, @RequestParam String modelo) {
        return datosVehiculosService.getVersionByMarcaAndAnioAndModelo(marca, anio, modelo);
    }
    
    @GetMapping("/mixto")
    public double getMixtoPorMarcaAnioModeloVersion(@RequestParam String marca, @RequestParam int anio, @RequestParam String modelo, @RequestParam String version) {
        return datosVehiculosService.getMixtoByMarcaAnioModeloAndVersion(marca, anio, modelo, version);
    }
    
}