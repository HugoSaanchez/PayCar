package com.example.demo.service;

import java.util.List;
import java.util.Map;

import com.example.demo.entity.DatosVehiculos;
import com.example.demo.entity.Usuario;


public interface DatosVehiculosService {
    List<String> getMarcas();
    public List<Integer> getAñosByMarca(String marca);
    List<String> getModelosByMarcaAndAnio(String marca, int anio);
    public List<String> getVersionByMarcaAndAnioAndModelo(String marca, int anio, String modelo);
    public double getMixtoByMarcaAnioModeloAndVersion(String marca, int anio, String modelo, String version);
    public Map<String, Object> getAlquiladoByMarcaAnioModeloAndVersion(String marca, int anio, String modelo, String version);
    public boolean alquilarVehiculo(String marca, int anio, String modelo, String version, String fechaInicio, String fechaFin, Usuario usuario);
    public List<DatosVehiculos> getVehiculosAlquiladosPorUsuario(Usuario usuario);
    public List<DatosVehiculos> getAllDatosVehiculos();
}