package com.example.demo.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Invitacion;

@Repository("invitacionRepository")

public interface InvitacionRepository  extends JpaRepository<Invitacion, Serializable>{
	Invitacion findByCodigoInvitacion(String codigoInvitacion);
	Invitacion findByGrupoId(int grupoId);
    void deleteByGrupoId(int grupoId);
}
