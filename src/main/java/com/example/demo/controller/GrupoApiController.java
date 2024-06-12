package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.Invitacion;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.UsuarioGrupo;
import com.example.demo.service.GrupoService;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api")
public class GrupoApiController {

	@Autowired
	private GrupoService grupoService;

	@Autowired
	private UsuarioService usuarioService;

	@PostMapping("/crear-grupo")
	public String crearGrupo(@RequestBody Grupo grupo, Model model) {
	    // Obtén el usuario autenticado de Spring Security
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();

	    Usuario usuario = usuarioService.findByUsername(username);

	    if (usuario == null) {
	        return "error";
	    }

	    // Llama al servicio para crear el grupo y el usuario-grupo
	    Map<String, Object> resultado = grupoService.crearGrupoYUsuarioGrupo(grupo, usuario);

	    // Agrega atributos al modelo si vas a mostrar alguna vista
	    model.addAttribute("grupo", resultado.get("grupo"));
	    model.addAttribute("usuarioGrupo", resultado.get("usuarioGrupo"));

	    // Retorna a la vista de éxito o donde sea adecuado
	    return "grupo-creado"; // Asume que tienes una vista que se llama "grupo-creado"
	}


	@PostMapping("/crear-invitacion/{grupoId}")
	public String crearInvitacion(@PathVariable int grupoId, Model model) {
	    String enlaceInvitacion = grupoService.crearInvitacionParaGrupo(grupoId);
	    return enlaceInvitacion;
	}


	@GetMapping("/unirse-grupo")
	public ResponseEntity<?> unirseGrupo(@RequestParam("codigo") String codigoInvitacion, Model model) {
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
	    Usuario usuario = usuarioService.findByUsername(username);
	    
	    if (usuario == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
	    }

	    String respuesta = grupoService.unirseAGrupo(codigoInvitacion, usuario);
	    if (respuesta.equals("Te has unido al grupo exitosamente.")) {
	        return ResponseEntity.ok(respuesta);
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
	    }
	}


	@GetMapping("/grupos")
	public ResponseEntity<List<Map<String, Object>>> getGruposDelUsuario() {
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
	    Usuario usuario = usuarioService.findByUsername(username);

	    if (usuario == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }

	    List<Map<String, Object>> grupos = grupoService.obtenerGruposDelUsuario(usuario);
	    return ResponseEntity.ok(grupos);
	}


	@GetMapping("/usuario-grupo")
	public ResponseEntity<Map<String, Object>> getRolYNombrePorUsuarioYGrupo(@RequestParam int usuarioId, @RequestParam int grupoId) {
	    Map<String, Object> response = grupoService.getRolYNombrePorUsuarioYGrupoResponse(usuarioId, grupoId);

	    if (response.containsKey("error")) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	    }

	    return ResponseEntity.ok(response);
	}

	
	@PostMapping("/calcular-costo-viaje")
	public ResponseEntity<?> calcularCostoViaje(@RequestParam int grupoId) {
	    Grupo grupo = grupoService.obtenerGrupoPorId(grupoId);
	    if (grupo == null) {
	        return ResponseEntity.badRequest().body("Grupo no encontrado");
	    }

	    // Asumimos que solo los usuarios activos en el grupo cuentan para dividir el costo
	    long integrantes = grupo.getUsuarios().stream()
	            .filter(usuarioGrupo -> !usuarioGrupo.getUsuario().isBorrado())
	            .count();

	    if (integrantes == 0) {
	        return ResponseEntity.badRequest().body("No hay integrantes activos en el grupo");
	    }

	    // Realizar el cálculo del costo del viaje
	    double costoViaje = (grupo.getKilometrosRecorridos() * (grupo.getConsumoGasolina() / 100) * grupo.getDineroGasolina());

	    
	   

	    // Actualizar el campo 'costetotal' en cada UsuarioGrupo
	    for (UsuarioGrupo usuarioGrupo : grupo.getUsuarios()) {
	        if (!usuarioGrupo.getUsuario().isBorrado()) {
	            if ("conductor".equals(usuarioGrupo.getRol())) {
	                usuarioGrupo.setCostetotal((float) ((float) costoViaje - (costoViaje/integrantes))); // No dividir para el conductor
	            } else {
	                usuarioGrupo.setCostetotal((float) (costoViaje / integrantes)); // Dividir entre los pasajeros
	            }
	            grupoService.actualizarUsuarioGrupo(usuarioGrupo);
	        }
	    }

	    // Formatear la respuesta
	    Map<String, Object> response = new HashMap<>();
	    response.put("costoViaje", costoViaje);
	    response.put("grupoId", grupoId);
	    response.put("tituloGrupo", grupo.getTitulo());

	    return ResponseEntity.ok(response);
	}

	@PostMapping("/editar-grupo")
	public ResponseEntity<?> editarGrupo(@RequestParam int grupoId, @RequestBody Map<String, Object> updates) {
	    Grupo grupo = grupoService.obtenerGrupoPorId(grupoId);
	    if (grupo == null) {
	        return ResponseEntity.badRequest().body("Grupo no encontrado");
	    }

	    if (updates.containsKey("dineroGasolina")) {
	        grupo.setDineroGasolina(((Number) updates.get("dineroGasolina")).floatValue());
	    }
	    if (updates.containsKey("kilometrosRecorridos")) {
	        grupo.setKilometrosRecorridos(((Number) updates.get("kilometrosRecorridos")).intValue());
	    }
	    if (updates.containsKey("consumoGasolina")) {
	        grupo.setConsumoGasolina(((Number) updates.get("consumoGasolina")).floatValue());
	    }

	    grupoService.actualizarGrupo(grupo);

	    // Construir un mapa de la respuesta excluyendo los usuarios
	    Map<String, Object> response = Map.of(
	        "id", grupo.getId(),
	        "titulo", grupo.getTitulo(),
	        "descripcion", grupo.getDescripcion(),
	        "consumoGasolina", grupo.getConsumoGasolina(),
	        "kilometrosRecorridos", grupo.getKilometrosRecorridos(),
	        "dineroGasolina", grupo.getDineroGasolina(),
	        "activado", grupo.isActivado(),
	        "borrado", grupo.isBorrado()
	    );

	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/obtener-grupo")
	public ResponseEntity<?> obtenerGrupo(@RequestParam int grupoId) {
		Grupo grupo = grupoService.obtenerGrupoPorId(grupoId);
		if (grupo == null) {
			return ResponseEntity.badRequest().body("Grupo no encontrado");
		}
		// Construir un mapa de la respuesta excluyendo los usuarios
		Map<String, Object> response = Map.of("id", grupo.getId(), "titulo", grupo.getTitulo(),
				"descripcion", grupo.getDescripcion(), "consumoGasolina", grupo.getConsumoGasolina(),
				"kilometrosRecorridos", grupo.getKilometrosRecorridos(), "dineroGasolina", grupo.getDineroGasolina(),
				"activado", grupo.isActivado(), "borrado", grupo.isBorrado());
		return ResponseEntity.ok(response);
	}
	
    @GetMapping("/calcular-diferencia-coste")
    public ResponseEntity<?> calcularDiferenciaCoste(@RequestParam int grupoId) {
        Map<String, Float> diferencias = grupoService.calcularDiferenciaCoste(grupoId);
        if (diferencias == null) {
            return ResponseEntity.badRequest().body("Grupo no encontrado");
        }
        
        return ResponseEntity.ok(diferencias);
    }
    
    @PostMapping("/pagar")
    public ResponseEntity<?> pagar(@RequestParam int grupoId, @RequestParam int usuarioId) {
        // Obtén el grupo
        Grupo grupo = grupoService.obtenerGrupoPorId(grupoId);
        if (grupo == null) {
            return ResponseEntity.badRequest().body("Grupo no encontrado");
        }

        // Busca el UsuarioGrupo correspondiente
        Optional<UsuarioGrupo> usuarioGrupoOptional = grupoService.obtenerRolYNombrePorUsuarioYGrupo(usuarioId, grupoId);
        if (!usuarioGrupoOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado en el grupo");
        }

        // Actualiza el costepagado para igualarlo al costetotal
        UsuarioGrupo usuarioGrupo = usuarioGrupoOptional.get();
        usuarioGrupo.setCostepagado(usuarioGrupo.getCostetotal());
        grupoService.actualizarUsuarioGrupo(usuarioGrupo);

        // Construir la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", usuarioId);
        response.put("grupoId", grupoId);
        response.put("costepagado", usuarioGrupo.getCostepagado());
        response.put("costetotal", usuarioGrupo.getCostetotal());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/actualizar-costepagado")
    public ResponseEntity<?> actualizarCostepagado(@RequestParam int grupoId, @RequestParam int usuarioId) {
        Grupo grupo = grupoService.obtenerGrupoPorId(grupoId);
        if (grupo == null) {
            return ResponseEntity.badRequest().body("Grupo no encontrado");
        }

        Optional<UsuarioGrupo> usuarioGrupoOptional = grupoService.obtenerRolYNombrePorUsuarioYGrupo(usuarioId, grupoId);
        if (!usuarioGrupoOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado en el grupo");
        }

        UsuarioGrupo usuarioGrupo = usuarioGrupoOptional.get();
        float diferencia = usuarioGrupo.getCostetotal() - usuarioGrupo.getCostepagado();

        // Actualizar el costepagado del usuario
        if (diferencia > 0) {
            usuarioGrupo.setCostepagado(usuarioGrupo.getCostepagado() + diferencia);
        } else {
            usuarioGrupo.setCostepagado(usuarioGrupo.getCostetotal());
        }
        grupoService.actualizarUsuarioGrupo(usuarioGrupo);

        // Obtener el UsuarioGrupo del conductor
        Optional<UsuarioGrupo> conductorGrupoOptional = grupo.getUsuarios().stream()
                .filter(ug -> ug.getRol().equals("conductor"))
                .findFirst();
        
        if (!conductorGrupoOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Conductor no encontrado en el grupo");
        }

        UsuarioGrupo conductorGrupo = conductorGrupoOptional.get();

        // Actualizar el costepagado del conductor
        if (diferencia > 0) {
            conductorGrupo.setCostepagado(conductorGrupo.getCostepagado() + diferencia);
        } else {
            conductorGrupo.setCostepagado(conductorGrupo.getCostepagado() + usuarioGrupo.getCostetotal());
        }
        grupoService.actualizarUsuarioGrupo(conductorGrupo);

        // Construir la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", usuarioId);
        response.put("grupoId", grupoId);
        response.put("costepagadoUsuario", usuarioGrupo.getCostepagado());
        response.put("costetotalUsuario", usuarioGrupo.getCostetotal());
        response.put("costepagadoConductor", conductorGrupo.getCostepagado());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/obtener-invitacion")
    public ResponseEntity<?> obtenerInvitacion(@RequestParam int grupoId) {
        Invitacion invitacion = grupoService.obtenerInvitacionPorGrupoId(grupoId);
        if (invitacion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invitación no encontrada para el grupo especificado.");
        }

        String enlaceInvitacion = invitacion.getCodigoInvitacion();
        return ResponseEntity.ok(enlaceInvitacion);
    }

    @PostMapping("/grupo/borrar")
    public ResponseEntity<?> borrarGrupo(@RequestParam int id) {
        grupoService.borrarGrupo(id);
        return ResponseEntity.ok().build();
    }

   
    
    @PostMapping("/grupo/salir")
    public ResponseEntity<?> salirGrupo(@RequestParam int grupoId) {
        // Obtener el usuario autenticado
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioService.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autorizado");
        }

        try {
            grupoService.salirGrupo(usuario, grupoId);
            return ResponseEntity.ok().body("Te has salido del grupo exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al salir del grupo: " + e.getMessage());
        }
    }

}
