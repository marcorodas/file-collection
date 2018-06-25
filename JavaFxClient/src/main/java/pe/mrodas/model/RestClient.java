package pe.mrodas.model;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

public class RestClient {

    private static String baseUrl;
    private static String token;
    private static boolean tokenIsSet;
    private static Retrofit retrofit;

    public static void setBaseUrl(String baseUrl) {
        RestClient.baseUrl = baseUrl;
    }

    public static void setToken(String token) {
        tokenIsSet = false;
        RestClient.token = token;
    }

    private static okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(newRequest);
    }

    public static Retrofit retrofit() throws Exception {
        if (retrofit == null) {
            if (baseUrl == null) {
                throw new Exception("baseUrl is not set");
            }
            retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (!tokenIsSet && token != null) {
            tokenIsSet = true;
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(RestClient::intercept)
                    .build();
            retrofit = retrofit.newBuilder()
                    .client(client).build();
        }
        return retrofit;
    }

    public static <T, R> void execute(Retrofit retro, Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<R> onSuccess, Function<String, String> onError) throws Exception {
        T modelObj = retro.create(modelClass);
        Response<R> response = modelMethod.apply(modelObj).execute();
        if (response.isSuccessful()) {
            R responseBody = response.body();
            if (responseBody == null) {
                throw new Exception("Error: response body is null");
            } else {
                onSuccess.accept(responseBody);
            }
        } else {
            ResponseBody errorBody = response.errorBody();
            if (errorBody == null) {
                throw new Exception("Server Error");
            } else {
                String errorMessage = onError.apply(errorBody.string());
                throw new Exception(errorMessage);
            }
        }
    }

    public static <T, R> void execute(Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<R> onSuccess, Function<String, String> onError) throws Exception {
        RestClient.execute(RestClient.retrofit(), modelClass, modelMethod, onSuccess, onError);
    }
}
