package com.example.pbac.web.security;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.annotation.Nullable;
import lombok.Data;

/**
 * Result es una clase genérica utilizada para representar el resultado de una
 * operación,
 * con posibles valores de éxito (Ok) o error (Err).
 *
 * Esta clase se utiliza para manejar resultados de manera consistente,
 * encapsulando tanto
 * los valores válidos como los errores en un solo objeto para ser usados en el
 * Controller.
 *
 * @param <Ok>  Tipo de valor de éxito, que representa el resultado positivo de
 *              la operación.
 * @param <Err> Tipo de valor de error, que representa el error ocurrido durante
 *              la operación.
 */

@Data
public class Result<Ok, Err> {
    @Nullable
    private Ok ok;
    @Nullable
    private Err err;

    public boolean isOk() {
        return ok != null;
    }

    public boolean isErr() {
        return err != null;
    }

    public String toJson() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
