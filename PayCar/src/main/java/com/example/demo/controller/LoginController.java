package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Usuario;
import com.example.demo.model.UsuarioModel;
import com.example.demo.service.UsuarioService;

@Controller
public class LoginController {

	  @Autowired
	   private UsuarioService usuarioService;

	   @GetMapping("/")
	    public String redirectToLogin() {
	        return "redirect:/auth/login";
	    }

	    @GetMapping("/auth/login")
	    public String login(Model model, 
	                        @RequestParam(name = "error", required = false) String error,
	                        @RequestParam(name = "logout", required = false) String logout) 
	    {
	        model.addAttribute("usuario", new Usuario());
	        model.addAttribute("error", error != null);  
	        model.addAttribute("logout", logout);
	        return "Autenticacion/login";
	    }

	   
	 
	 @GetMapping("/auth/register")
	    public String register(Model model) {
	        model.addAttribute("usuarioModel", new UsuarioModel());
	        return "Autenticacion/register";
	    }
	 
	 @PostMapping("/auth/register")
	    public String registerform(@ModelAttribute("usuarioModel") UsuarioModel usuarioModel, RedirectAttributes flash) {
	        usuarioService.registrar(usuarioModel);
	        flash.addFlashAttribute("success", "Usuario registrado con éxito!!");
	        return "redirect:/auth/login";
	    }
}
