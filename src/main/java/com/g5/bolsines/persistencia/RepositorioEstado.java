package com.g5.bolsines.persistencia;

import com.g5.bolsines.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioEstado extends JpaRepository<Estado, Long> {
}
