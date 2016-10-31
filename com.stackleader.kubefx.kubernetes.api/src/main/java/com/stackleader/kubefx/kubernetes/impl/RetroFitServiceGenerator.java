package com.stackleader.kubefx.kubernetes.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.stackleader.kubefx.heapster.api.MemoryString;
import io.fabric8.kubernetes.client.Config;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, service = RetroFitServiceGenerator.class)
public class RetroFitServiceGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(RetroFitServiceGenerator.class);
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager x509TrustManager;
    private OkHttpClient.Builder httpClient;

    private Retrofit.Builder builder;

    public RetroFitServiceGenerator() {
        try {
            x509TrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null,
                    new TrustSelfSignedStrategy()).build();
            sslSocketFactory = sslContext.getSocketFactory();
            httpClient = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, x509TrustManager);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public <S> S createService(Class<S> serviceClass, Config config) {
        initializeHttpClient(config);
        builder = new Retrofit.Builder()
                .baseUrl(config.getMasterUrl())
                .addConverterFactory(GsonConverterFactory.create(createCustomGson()));
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    private void initializeHttpClient(Config config) {
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                String credential = Credentials.basic(config.getUsername(), config.getPassword());
                Request.Builder requestBuilder = original.newBuilder()
                        .header("content-type", "application/json")
                        .header("Authorization", credential)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        httpClient.hostnameVerifier((String string, SSLSession ssls) -> true);
    }

//    public static void main(String[] args) throws IOException {
//        HeapsterClient client = ServiceGenerator.createService(HeapsterClient.class);
//        Call<PodCpuUsage> call = client.pods("", "");
//        final retrofit2.Response<PodCpuUsage> execute = call.execute();
//        PodCpuUsage body = execute.body();
//        body.metrics.stream()
//                .filter(metric -> metric.timestamp.equals(body.latestTimestamp))
//                .forEach(metric -> System.out.println(metric.timestamp + " / " + metric.value));
//
//    }
    private Gson createCustomGson() {
        final JsonDeserializer<LocalDateTime> localDateTimeTypeAdapter = new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                LocalDateTime localDateTime = LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
                return localDateTime;
            }
        };
        final JsonDeserializer<MemoryString> memoryStringTypeAdapter = new JsonDeserializer<MemoryString>() {
            @Override
            public MemoryString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new MemoryString(json.getAsJsonPrimitive().getAsLong());
            }
        };
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, localDateTimeTypeAdapter)
                .registerTypeAdapter(MemoryString.class, memoryStringTypeAdapter)
                .create();
        return gson;
    }
}
