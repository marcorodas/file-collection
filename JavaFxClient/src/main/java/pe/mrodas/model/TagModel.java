package pe.mrodas.model;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

import pe.mrodas.entity.Tag;
import pe.mrodas.entity.TagListsToSave;

public interface TagModel {
    @GET("tag/categories")
    Call<List<Tag>> getCategories(@Query("root") int idRoot);

    @GET("tag")
    Call<List<Tag>> getTagSuggestions(@Query("root") int idRoot, @Query("hint") String search, @Query("includeCat") boolean includeCat);

    @GET("tag/file")
    Call<List<Tag>> getFileTags(@Query("md5") String md5);

    @POST("tag")
    Call<Tag> save(@Query("root") int idRoot, @Body Tag tag);

    @PUT("tag/lists")
    Call<ResponseBody> saveTagLists(@Query("md5") String md5, @Body TagListsToSave tagListsToSave);
}
