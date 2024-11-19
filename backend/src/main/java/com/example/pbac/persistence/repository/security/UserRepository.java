package com.example.pbac.persistence.repository.security;

import com.example.pbac.persistence.model.security.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Este archivo extiende a JpaRepository, lo cual permite realizar
 * operaciones CRUD (crear, leer, actualizar y eliminar) sobre la
 * entidad Test sin necesidad de implementar esos métodos manualmente.
 * Ademas extiende a JpaSpecification, el cual permite realizar Queries mas
 * complejos e
 * personalizados (ver el archivo: SearchUserPostulantSpecification)
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username El nombre de usuario del usuario a buscar.
     * @return Un `Optional` que contiene el usuario si se encuentra, o vacío si no
     *         se encuentra.
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email La dirección de correo electrónico del usuario a buscar.
     * @return Un `Optional` que contiene el usuario si se encuentra, o vacío si no
     *         se encuentra.
     */
    Optional<User> findByEmail(String email);

    /**
     * Actualiza la contraseña de un usuario mediante su dirección de correo
     * electrónico.
     *
     * @param email    La dirección de correo electrónico del usuario cuyo password
     *                 será actualizado.
     * @param password La nueva contraseña a asignar.
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("password") String password);
}
