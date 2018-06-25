package pe.mrodas.model;

import pe.mrodas.entity.User;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.POST;

public class Service {

    private static final String BASE_URL = "http://localhost:9090/file-collection/rest/";
    private static String token;
    private static Retrofit retrofit;

    public static void setToken(String token) {
        Service.token = token;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            Retrofit.Builder builder = new Retrofit.Builder();

            retrofit = builder
                    .baseUrl(BASE_URL)
                    .build();
        }
        return retrofit;
    }

    public interface Login {
        @POST("login/auth")
        Call<User> auth();
    }


}
