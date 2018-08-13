package pe.mrodas.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import pe.mrodas.entity.FileItem;
import pe.mrodas.entity.Tag;
import pe.mrodas.helper.FileHelper;
import pe.mrodas.model.FileItemDA;
import pe.mrodas.service.config.Secured;

@Path("file")
@Produces("application/json")
public class FileService {
    @Secured
    @GET
    public FileItem[] select(@QueryParam("tagsId") List<Integer> tagsId) throws Exception {
        return FileItemDA.select(tagsId).toArray(new FileItem[0]);
    }

    interface FileResponseBuilder {
        Response build(String fileName, String mimeType, FileItem fileItem);
    }

    private Response buildFileResponse(Integer idFile, FileResponseBuilder builder) throws Exception {
        FileItem fileItem = FileItemDA.select(idFile);
        if (fileItem != null && fileItem.getIdFile() > 0) {
            String fileName = String.format("%s.%s", fileItem.getMd5(), fileItem.getExtension());
            String mimeType = URLConnection.guessContentTypeFromName(fileName);
            return builder.build(fileName, mimeType, fileItem);
        } else {
            throw new NotFoundException(String.format("File not Found! (idFile=%d)", idFile));
        }
    }

    @Secured
    @GET
    @Path("download/{idFile}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("idFile") Integer idFile) throws Exception {
        return buildFileResponse(idFile, (fileName, mimeType, fileItem) -> {
            Response.ResponseBuilder builder = Response.ok(fileItem.getContent(), mimeType);
            return builder.header("content-disposition", "attachment; filename = " + fileName)
                    .build();
        });
    }

    @Secured
    @GET
    @Path("{idFile}")
    public Response getFile(@PathParam("idFile") Integer idFile) throws Exception {
        return buildFileResponse(idFile, (fileName, mimeType, fileItem) -> {
            StreamingOutput stream = output -> output.write(fileItem.getContent());
            return Response.ok(stream, mimeType).build();
        });
    }

    @Secured
    @POST
    @Path("missing")
    public FileItem[] getMissingFilesId(@QueryParam("idRoot") int idRoot, List<String> md5List) throws Exception {
        return FileItemDA.select(idRoot, md5List).toArray(new FileItem[0]);
    }

    @Secured
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response insert(@FormDataParam("idTag") String idTag,
                           @FormDataParam("file") InputStream inputStream,
                           @FormDataParam("file") FormDataContentDisposition fileMetaData) throws Exception {
        String name = fileMetaData.getFileName();
        String extension = FileHelper.getExtension(name);
        String fileName = name.substring(0, name.length() - extension.length() - 1);
        FileItem fileItem = new FileItem().setMd5(fileName).setExtension(extension);
        Tag tag = new Tag().setIdTag(Integer.parseInt(idTag));
        fileItem.getTags().add(tag);
        try {
            int id = FileItemDA.insert(fileItem, inputStream);
            URI uri = URI.create("/file-collection/rest/file/" + id);
            return Response.created(uri).build();
        } catch (Exception e) {
            String regex = ".*?\\(e:Duplicate entry '(.*?)' for key 'md5'\\)";
            if (e.getMessage().matches(regex)) {
                //If md5 already exists, don't throw an error.
                return Response.ok().build();
            } else {
                throw e;
            }
        }
    }

}
