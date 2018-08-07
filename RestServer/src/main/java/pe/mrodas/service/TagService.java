package pe.mrodas.service;

import pe.mrodas.entity.Tag;
import pe.mrodas.model.TagDA;
import pe.mrodas.service.config.Secured;
import pe.mrodas.service.config.SecuredRequestFilter;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
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

    @Secured
    @GET
    public Tag[] getTags(@QueryParam("root") int idRoot, @QueryParam("hint") String hint) throws Exception {
        int idUser = SecuredRequestFilter.getUserId(context);
        return TagDA.getTags(idRoot, idUser, hint).toArray(new Tag[0]);
    }

    @Secured
    @POST
    public Tag save(@QueryParam("root") int idRoot, Tag tag) throws Exception {
        return TagDA.save(idRoot, tag);
    }
}
