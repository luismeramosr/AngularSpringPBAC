package com.example.pbac.web.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        Result<Void, Error> result = new Result<Void, Error>();
        result.setOk(null);
        result.setErr(new Error("No tiene los permisos necesarios para realizar esa acción.",
                "Access Denied: User does not have the required permissions to access this resource.",
                ErrorKind.AccessDenied));
        var writer = response.getWriter();
        response.setHeader("Content-Type", "application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        writer.write(result.toJson());
        writer.flush();
        writer.close();
    }
}
