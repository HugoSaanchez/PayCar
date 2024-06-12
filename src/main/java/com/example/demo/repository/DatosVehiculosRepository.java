package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.DatosVehiculos;
import com.example.demo.entity.Usuario;

@Repository("datosVehiculosRepository")
public interface DatosVehiculosRepository extends JpaRepository<DatosVehiculos, Long> {
	@Query("SELECT DISTINCT dv.marca FROM DatosVehiculos dv")
	List<String> findDistinctMarcas();

	@Query("SELECT DISTINCT dv.anio FROM DatosVehiculos dv WHERE dv.marca = :marca")
	List<Integer> findDistinctAñosByMarca(String marca);

	@Query("SELECT DISTINCT dv.modelo FROM DatosVehiculos dv WHERE dv.marca = :marca AND dv.anio = :anio")
	List<String> findModelosByMarcaAndAnio(String marca, int anio);

	@Query("SELECT DISTINCT d.version FROM DatosVehiculos d WHERE d.marca = :marca AND d.anio = :anio AND d.modelo = :modelo")
	List<String> findVersionByMarcaAndAnioAndModelo(String marca, int anio, String modelo);

	@Query("SELECT d.mixto FROM DatosVehiculos d WHERE d.marca = :marca AND d.anio = :anio AND d.modelo = :modelo AND d.version = :version")
	String findMixtoByMarcaAnioModeloAndVersion(String marca, int anio, String modelo, String version);

	@Query("SELECT v.alquilado, v.fecha_inicio, v.fecha_fin FROM DatosVehiculos v WHERE v.marca = :marca AND v.anio = :anio AND v.modelo = :modelo AND v.version = :version")
	List<Object[]> findAlquiladoAndFechasByMarcaAnioModeloAndVersion(@Param("marca") String marca,
			@Param("anio") int anio, @Param("modelo") String modelo, @Param("version") String version);

	DatosVehiculos findByMarcaAndAnioAndModeloAndVersion(String marca, int anio, String modelo, String version);

	List<DatosVehiculos> findByUsuario(Usuario usuario);
	
	@Query("SELECT dv FROM DatosVehiculos dv WHERE dv.alquilado = true")
    List<DatosVehiculos> findAllAlquilados();
}
