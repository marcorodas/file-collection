package pe.mrodas.service;

import pe.mrodas.entity.Config;
import pe.mrodas.model.ConfigDA;
import pe.mrodas.service.config.Secured;
import pe.mrodas.service.config.SecuredRequestFilter;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Path("config")
@Produces("application/json")
public class ConfigService {

    @Context
    SecurityContext context;

    @Secured
    @POST
    public void insert(Config config) throws Exception {
        ConfigDA.insert(SecuredRequestFilter.getUserId(context), config);
    }

    @Secured
    @PUT
    public void update(Config config) throws Exception {
        ConfigDA.update(SecuredRequestFilter.getUserId(context), config);
    }

}
