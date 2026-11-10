package com.g5.bolsines.persistencia;

import com.g5.bolsines.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);
}
