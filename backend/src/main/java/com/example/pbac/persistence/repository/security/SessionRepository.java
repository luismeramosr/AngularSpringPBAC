package com.example.pbac.persistence.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.pbac.persistence.model.security.Session;

/**
 * Este archivo extiende a JpaRepository, lo cual permite realizar
 * operaciones CRUD (crear, leer, actualizar y eliminar) sobre la
 * entidad Session sin necesidad de implementar esos métodos manualmente.
 */

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    /**
     * - `update(String accessToken, String refreshToken, String oldRefreshToken)`:
     * Actualiza los valores de los tokens de acceso y de refresco en la tabla
     * `session` donde el `refresh_token` coincide con el valor proporcionado en el
     * parámetro `oldRefreshToken`.
     * Este método es útil para renovar los tokens de acceso y de refresco asociados
     * con una sesión, utilizando un token de refresco existente.
     *
     * @param accessToken     El nuevo token de acceso que se actualizará en la base
     *                        de datos.
     * @param refreshToken    El nuevo token de refresco que se actualizará en la
     *                        base de datos.
     * @param oldRefreshToken El token de refresco actual que se utiliza como
     *                        referencia para la actualización.
     */
    @Modifying
    @Query(value = "UPDATE session SET access_token = ?1, refresh_token = ?2 WHERE refresh_token = ?3", nativeQuery = true)
    public void update(String accessToken, String refreshToken, String oldRefreshToken);
}
