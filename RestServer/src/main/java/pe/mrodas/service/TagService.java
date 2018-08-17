package pe.mrodas.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import pe.mrodas.entity.Tag;
import pe.mrodas.entity.TagListsToSave;
import pe.mrodas.model.TagDA;
import pe.mrodas.service.config.Secured;
import pe.mrodas.service.config.SecuredRequestFilter;

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
    public Tag[] getTags(@QueryParam("root") int idRoot, @QueryParam("hint") String hint, @QueryParam("includeCat") boolean includeCat) throws Exception {
        return TagDA.getTags(idRoot, hint, includeCat).toArray(new Tag[0]);
    }

    @Secured
    @Path("file")
    @GET
    public Tag[] getFileTags(@QueryParam("md5") String md5) throws Exception {
        return TagDA.getFileTags(md5).toArray(new Tag[0]);
    }

    @Secured
    @POST
    public Tag insert(@QueryParam("root") int idRoot, Tag tag) throws Exception {
        try {
            return TagDA.insert(idRoot, tag);
        } catch (Exception e) {
            String regex = ".*?\\(e:Duplicate entry '(.*?)' for key 'name'\\)";
            if (e.getMessage().matches(regex)) {
                return TagDA.getTag(idRoot, tag.getName());
            }
            throw e;
        }
    }

    @Secured
    @Path("lists")
    @PUT
    public Response save(@QueryParam("md5") String md5, TagListsToSave tagListsToSave) throws Exception {
        TagDA.save(md5, tagListsToSave);
        return Response.noContent().build();
    }
}
