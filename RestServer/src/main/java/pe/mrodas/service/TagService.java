package pe.mrodas.service;

import pe.mrodas.entity.Tag;
import pe.mrodas.model.TagDA;
import pe.mrodas.service.config.Secured;
import pe.mrodas.service.config.SecuredRequestFilter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

@Path("tag")
@Produces("application/json")
public class TagService {
    @Context
    SecurityContext context;

    @Secured
    @Path("categories")
    @GET
    public Tag[] getCategories(@QueryParam("root") int idRoot) throws Exception {
        int idUser = SecuredRequestFilter.getUserId(context);
        return TagDA.getCategories(idRoot, idUser).toArray(new Tag[0]);
    }
}
