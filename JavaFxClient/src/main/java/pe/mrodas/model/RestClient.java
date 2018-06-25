package pe.mrodas.model;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class RestClient implements Interceptor {

    private static final String BASE_URL = "http://localhost:9090/file-collection/rest/";
    private static String token;
    private static Retrofit retrofit;
    private static RestClient restClient;

    public static void setToken(String token) {
        restClient = null;
        RestClient.token = token;
    }

    public static <T> T create(Class<T> service) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (restClient == null && token != null) {
            restClient = new RestClient();
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(restClient)
                    .build();
            retrofit = retrofit.newBuilder()
                    .client(client)
                    .build();
        }
        return retrofit.create(service);
    }

    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(newRequest);
    }

}
