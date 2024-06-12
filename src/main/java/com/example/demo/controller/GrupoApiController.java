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
	    Map<String, Object> response = grupoService.calcularCostoViaje(grupoId);

	    if (response.containsKey("error")) {
	        return ResponseEntity.badRequest().body(response.get("error"));
	    }

	    return ResponseEntity.ok(response);
	}

	@PostMapping("/editar-grupo")
	public ResponseEntity<?> editarGrupo(@RequestParam int grupoId, @RequestBody Map<String, Object> updates) {
	    Map<String, Object> response = grupoService.actualizarGrupoConDatos(grupoId, updates);

	    if (response.containsKey("error")) {
	        return ResponseEntity.badRequest().body(response.get("error"));
	    }

	    return ResponseEntity.ok(response);
	}

	
	@GetMapping("/obtener-grupo")
	public ResponseEntity<?> obtenerGrupo(@RequestParam int grupoId) {
	    Map<String, Object> response = grupoService.obtenerDetallesGrupo(grupoId);

	    if (response.containsKey("error")) {
	        return ResponseEntity.badRequest().body(response.get("error"));
	    }

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
        Map<String, Object> response = grupoService.procesarPago(grupoId, usuarioId);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response.get("error"));
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/actualizar-costepagado")
    public ResponseEntity<?> actualizarCostepagado(@RequestParam int grupoId, @RequestParam int usuarioId) {
        Map<String, Object> response = grupoService.actualizarCostepagado(grupoId, usuarioId);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response.get("error"));
        }

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
