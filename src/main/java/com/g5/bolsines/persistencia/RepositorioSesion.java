package com.g5.bolsines.persistencia;

import com.g5.bolsines.entity.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioSesion extends JpaRepository<Sesion, Long> {
}
