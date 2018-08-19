package pe.mrodas.model;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

import pe.mrodas.entity.FileItem;

public interface FileModel {
    @GET("file")
    Call<List<FileItem>> getFiles(@Query("tagsId") List<Integer> tagsId);

    @GET("file/untagged")
    Call<List<FileItem>> getFilesUntagged(@Query("categoryId") int idCategory);

    @GET("file/{idFile}")
    Call<ResponseBody> getFile(@Path("idFile") int idFile);

    @POST("file/missing")
    Call<List<FileItem>> getMissingFilesId(@Query("idRoot") int idRoot, @Body List<String> md5List);

    @DELETE("file")
    Call<ResponseBody> delete(@Query("md5") String md5);

    @Multipart
    @POST("file")
    Call<ResponseBody> uploadFile(@PartMap() Map<String, RequestBody> partMap,
                                  @Part MultipartBody.Part file);
}
