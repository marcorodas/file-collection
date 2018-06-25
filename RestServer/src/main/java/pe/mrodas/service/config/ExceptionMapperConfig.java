package pe.mrodas.service.config;

import lombok.Setter;
import pe.mrodas.rest.JsonExceptionMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionMapperConfig extends JsonExceptionMapper {

    @Setter
    private static boolean debugMode = true;

    @Override
    public Response toResponse(Exception exception) {
        Response.Status status = super.getStatus(exception);
        String message = super.getMessage(exception);
        return super.getResponse(exception, status, message, debugMode);
    }

}
