package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.Grupo;
import com.example.demo.entity.Invitacion;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.UsuarioGrupo;

public interface GrupoService {

	public abstract List<Grupo> obtenerGrupos();

	public abstract List<UsuarioGrupo> obtenerUsuariosPorGrupoId(int grupoId);

	public abstract Grupo crearGrupo(Grupo grupo);

	public abstract UsuarioGrupo crearUsuarioGrupo(UsuarioGrupo usuarioGrupo);

	public abstract String generarCodigoUnico();

	public abstract Grupo obtenerGrupoPorId(int id);

	public abstract void guardarInvitacion(Invitacion invitacion);

	public abstract Invitacion obtenerGrupoPorCodigo(String codigoInvitacion);

	public List<Grupo> obtenerGruposPorUsuario(Usuario usuario);

	public Optional<UsuarioGrupo> obtenerRolYNombrePorUsuarioYGrupo(int usuarioId, int grupoId);

	public Grupo actualizarGrupo(Grupo grupo);

	public void actualizarUsuarioGrupo(UsuarioGrupo usuarioGrupo);

	public Map<String, Float> calcularDiferenciaCoste(int grupoId);

	public Invitacion obtenerInvitacionPorGrupoId(int grupoId);

	public void activarGrupo(int grupoId);

	public void desactivarGrupo(int grupoId);

	public void borrarGrupo(int id);

	void eliminarUsuarioGrupo(UsuarioGrupo usuarioGrupo);

	public abstract List<Grupo> findByEstado(String rol);

	public void eliminarUsuarioDeGrupos(Usuario usuario);

	public void salirGrupo(Usuario usuario, int grupoId);

	Map<String, Object> obtenerGruposYUsuarios(String estado, Integer grupoId, String email);
}
