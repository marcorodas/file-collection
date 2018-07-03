package pe.mrodas.model;

import pe.mrodas.entity.Config;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ConfigModel {
    @POST("config")
    Call<Void> insert(@Body Config config);

    @PUT("config")
    Call<Void> update(@Body Config config);
}
