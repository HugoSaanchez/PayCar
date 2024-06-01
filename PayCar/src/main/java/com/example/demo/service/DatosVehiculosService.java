package com.example.demo.service;

import java.util.List;


public interface DatosVehiculosService {
    List<String> getMarcas();
    public List<Integer> getAñosByMarca(String marca);
    List<String> getModelosByMarcaAndAnio(String marca, int anio);
    public List<String> getVersionByMarcaAndAnioAndModelo(String marca, int anio, String modelo);
    public double getMixtoByMarcaAnioModeloAndVersion(String marca, int anio, String modelo, String version);
}