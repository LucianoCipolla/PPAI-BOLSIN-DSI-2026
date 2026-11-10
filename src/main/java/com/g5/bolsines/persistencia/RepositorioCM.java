package com.g5.bolsines.persistencia;

import com.g5.bolsines.entity.CM;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioCM extends JpaRepository<CM, Long> {
}
