package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.UsuarioGrupo;

public interface GrupoService {
	
	public abstract List<Grupo> obtenerGrupos();
	public abstract List<UsuarioGrupo> obtenerUsuariosPorGrupoId(int grupoId);
}
