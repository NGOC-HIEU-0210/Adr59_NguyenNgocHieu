package com.hieu.resource;

import com.hieu.dto.RecognizeResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class DebugExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = Logger.getLogger(DebugExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        log.error("Loi xu ly request:", exception);

        int status = 500;
        if (exception instanceof jakarta.ws.rs.WebApplicationException wae) {
            status = wae.getResponse().getStatus();
        }

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(RecognizeResponse.error(exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName()))
                .build();
    }
}
