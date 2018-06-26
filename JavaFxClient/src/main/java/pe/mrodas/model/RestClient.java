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

    public static class ErrorInfo {
        private String url, modelClass, bodyResponse;

        ErrorInfo(String url, String modelClass, String bodyResponse) {
            this.url = url;
            this.modelClass = modelClass;
            this.bodyResponse = bodyResponse;
        }

        public String getUrl() {
            return url;
        }

        public String getModelClass() {
            return modelClass;
        }

        public String getBodyResponse() {
            return bodyResponse;
        }
    }

    private static String baseUrl;
    private static String token;
    private static boolean tokenIsSet;
    private static Retrofit retrofit;
    private static Function<ErrorInfo, String> errorHandler;

    public static void setBaseUrl(String baseUrl) {
        RestClient.baseUrl = baseUrl;
    }

    public static void setToken(String token) {
        tokenIsSet = false;
        RestClient.token = token;
    }

    public static void setErrorHandler(Function<ErrorInfo, String> errorHandler) {
        RestClient.errorHandler = errorHandler;
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

    private static String getExceptionMessage(String modelClass, Response<?> response, Function<ErrorInfo, String> onError) throws IOException {
        ResponseBody body = response.errorBody();
        if (body == null) {
            return "Null ErrorInfo Body Response";
        }
        if (onError != null || errorHandler != null) {
            String url = response.raw().request().url().toString();
            ErrorInfo errorInfo = new ErrorInfo(url, modelClass, body.string());
            return onError == null ? errorHandler.apply(errorInfo) : onError.apply(errorInfo);
        }
        return "Server ErrorInfo & ErrorInfo Handler Not Set";
    }

    public static <T, R> void execute(Retrofit retro, Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<R> onSuccess, Function<ErrorInfo, String> onError) throws Exception {
        T modelObj = retro.create(modelClass);
        Response<R> response = modelMethod.apply(modelObj).execute();
        if (response.isSuccessful()) {
            R responseBody = response.body();
            if (responseBody == null) {
                throw new Exception("Null Body Response");
            }
            onSuccess.accept(responseBody);
        } else {
            try {
                String exceptionMessage = RestClient.getExceptionMessage(modelClass.toString(), response, onError);
                throw new Exception(exceptionMessage);
            } catch (IOException e) {
                throw new Exception("I/O ErrorInfo: ErrorInfo Reading Response");
            }
        }
    }

    public static <T, R> void execute(Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<R> onSuccess) throws Exception {
        RestClient.execute(RestClient.retrofit(), modelClass, modelMethod, onSuccess, null);
    }

    public static <T, R> void execute(Class<T> modelClass, Function<T, Call<R>> modelMethod, Consumer<R> onSuccess, Function<ErrorInfo, String> onError) throws Exception {
        RestClient.execute(RestClient.retrofit(), modelClass, modelMethod, onSuccess, onError);
    }
}
