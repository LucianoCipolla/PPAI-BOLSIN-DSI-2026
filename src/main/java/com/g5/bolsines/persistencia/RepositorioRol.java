package com.g5.bolsines.persistencia;

import com.g5.bolsines.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioRol extends JpaRepository<Rol, Long> {
}
