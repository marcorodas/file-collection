package pe.mrodas.model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface FileModel {
    @GET("files")
    Call<List<String>> getFiles(@Query("tagsId") List<Integer> tagsId);
}
