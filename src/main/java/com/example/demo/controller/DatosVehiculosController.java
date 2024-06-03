package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.DatosVehiculos;
import com.example.demo.entity.Usuario;
import com.example.demo.service.DatosVehiculosService;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api")
public class DatosVehiculosController {

	@Autowired
	private DatosVehiculosService datosVehiculosService;
	
	@Autowired
	private UsuarioService usuarioService;

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
	public List<String> getVersionesPorMarcaAnioYModelo(@RequestParam String marca, @RequestParam int anio,
			@RequestParam String modelo) {
		return datosVehiculosService.getVersionByMarcaAndAnioAndModelo(marca, anio, modelo);
	}

	@GetMapping("/mixto")
	public double getMixtoPorMarcaAnioModeloVersion(@RequestParam String marca, @RequestParam int anio,
			@RequestParam String modelo, @RequestParam String version) {
		return datosVehiculosService.getMixtoByMarcaAnioModeloAndVersion(marca, anio, modelo, version);
	}

	@GetMapping("/alquilado")
	public ResponseEntity<Map<String, Object>> getAlquilado(@RequestParam String marca, @RequestParam int anio,
	                                                        @RequestParam String modelo, @RequestParam String version) {
	    Map<String, Object> alquiladoInfo = datosVehiculosService.getAlquiladoByMarcaAnioModeloAndVersion(marca, anio, modelo, version);
	    return ResponseEntity.ok(alquiladoInfo);
	}
	 
	 @PostMapping("/alquilar")
	    public ResponseEntity<String> alquilar(@RequestParam String marca, @RequestParam int anio,
	                                           @RequestParam String modelo, @RequestParam String version,
	                                           @RequestParam String fechaInicio, @RequestParam String fechaFin) {
	        // Obtener el nombre de usuario autenticado
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        String username = authentication.getName();

	        // Buscar la entidad Usuario correspondiente
	        Usuario usuario = usuarioService.findByUsername(username);
	        if (usuario == null) {
	            return ResponseEntity.status(404).body("Usuario no encontrado");
	        }
	        
	        boolean success = datosVehiculosService.alquilarVehiculo(marca, anio, modelo, version, fechaInicio, fechaFin, usuario);
	        if (success) {
	            return ResponseEntity.ok("Vehículo alquilado exitosamente");
	        } else {
	            return ResponseEntity.status(500).body("Error al alquilar el vehículo");
	        }
	    }
	 
	 @GetMapping("/vehiculos-alquilados")
	 public ResponseEntity<List<Map<String, Object>>> getVehiculosAlquilados() {
	     // Obtener el nombre de usuario autenticado
	     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	     String username = authentication.getName();

	     // Buscar la entidad Usuario correspondiente
	     Usuario usuario = usuarioService.findByUsername(username);
	     if (usuario == null) {
	         return ResponseEntity.status(404).body(null);
	     }

	     List<DatosVehiculos> vehiculosAlquilados = datosVehiculosService.getVehiculosAlquiladosPorUsuario(usuario);
	     
	     // Crear una lista para almacenar los resultados modificados
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
	         vehiculoMap.put("usuario_id", vehiculo.getUsuario().getId()); // Solo agregar el ID del usuario
	         
	         resultado.add(vehiculoMap);
	     }
	     
	     return ResponseEntity.ok(resultado);
	 }


}