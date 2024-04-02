package com.example.demo.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.UsuarioGrupo;
import com.example.demo.repository.GrupoRepository;
import com.example.demo.repository.UsuarioGrupoRepository;
import com.example.demo.service.GrupoService;

@Service("grupoService")
public class GrupoServiceImpl implements GrupoService {
	
	@Autowired
	private GrupoRepository grupoRepository;
	@Autowired
	private UsuarioGrupoRepository usuarioGrupoRepository;

	@Override
	public List<Grupo> obtenerGrupos() {
		 return grupoRepository.findAll();
	}

	@Override
	public List<UsuarioGrupo> obtenerUsuariosPorGrupoId(int grupoId) {
		 return usuarioGrupoRepository.findByGrupoId(grupoId);
	}

}
