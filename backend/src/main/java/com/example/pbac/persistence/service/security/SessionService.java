package com.example.pbac.persistence.service.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.pbac.persistence.model.security.Session;
import com.example.pbac.persistence.repository.security.SessionRepository;
import com.example.pbac.web.security.service.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Este archivo es el encargado de la gestión de los registros de la entidad
 * Session.
 * Para la creacion de estos metodos ocupamos los metodos creados en la capa
 * repository
 * y specification. El servicio maneja las respuestas con el tipo `Result` que
 * encapsula
 * tanto el valor correcto como el error (si ocurre).
 */

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository repository;
    private final JwtService jwtService;

    /**
     * Busca una sesión en la base de datos utilizando el ID proporcionado.
     *
     * Este método intenta encontrar una sesión existente a través de su
     * identificador único (`id`), que generalmente es el
     * token de refresco o algún identificador asociado a la sesión.
     *
     * @param id El identificador único de la sesión que se desea buscar.
     * @return Un objeto `Optional` que contiene la sesión encontrada si existe, o
     *         está vacío si no se encuentra.
     */
    public Optional<Session> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Inserta o actualiza una sesión en la base de datos.
     *
     * Este método intenta encontrar una sesión existente utilizando el antiguo
     * token de refresco (`oldRefreshToken`).
     * Si la sesión no existe, se inserta una nueva. Si la sesión ya existe, se
     * actualiza con los nuevos valores
     * del token de acceso y token de refresco.
     *
     * @param session         La nueva sesión que debe ser insertada o actualizada.
     * @param oldRefreshToken El token de refresco antiguo utilizado para buscar una
     *                        sesión existente.
     * @return La sesión insertada o actualizada.
     */
    @Transactional
    public Session upsert(Session session, String oldRefreshToken) {
        Optional<Session> oldSession = this.findById(oldRefreshToken);
        if (oldSession.isEmpty()) {
            repository.save(session);
            return session;
        } else {
            repository.update(session.getAccessToken(), session.getRefreshToken().toString(), oldRefreshToken);
            return session;
        }
    }
}
