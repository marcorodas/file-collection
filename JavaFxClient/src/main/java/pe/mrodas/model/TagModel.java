package pe.mrodas.model;

import pe.mrodas.entity.Tag;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface TagModel {
    @GET("tag/categories")
    Call<List<Tag>> getCategories(@Query("root") int idRoot);
    @GET("tag")
    Call<List<Tag>> getTagSuggestions(@Query("root") int idRoot, @Query("hint") String search);
}
