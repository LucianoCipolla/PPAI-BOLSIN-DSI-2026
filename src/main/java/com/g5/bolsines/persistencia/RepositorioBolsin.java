package com.g5.bolsines.persistencia;

import com.g5.bolsines.entity.Bolsin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioBolsin extends JpaRepository<Bolsin, Long> {
}
