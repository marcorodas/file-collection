package pe.mrodas.service.config;

import pe.mrodas.model.UserDA;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.security.Principal;

/**
 * @author skynet
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecuredRequestFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            String token = this.getToken(requestContext);
            int userId = UserDA.validateToken(token);
            SecurityContext context = this.getSecurityContext(userId, requestContext);
            requestContext.setSecurityContext(context);
        } catch (Exception e) {
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage()).build();
            requestContext.abortWith(response);
        }
    }

    //JWT: https://www.arquitecturajava.com/introduccion-a-json-web-token/
    private String getToken(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader != null) {
            if (authHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ")) {
                return authHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
            }
        }
        return null;
    }

    public static int getUserId(SecurityContext context) throws Exception {
        int id = Integer.parseInt(context.getUserPrincipal().getName());
        if (id > 0) {
            return id;
        }
        throw new Exception("Invalid User!");
    }

    private SecurityContext getSecurityContext(int userId, ContainerRequestContext requestContext) {
        return new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> String.valueOf(userId);
            }

            @Override
            public boolean isUserInRole(String role) {
                return requestContext.getSecurityContext().isUserInRole(role);
            }

            @Override
            public boolean isSecure() {
                return requestContext.getSecurityContext().isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return requestContext.getSecurityContext().getAuthenticationScheme();
            }
        };
    }
}
