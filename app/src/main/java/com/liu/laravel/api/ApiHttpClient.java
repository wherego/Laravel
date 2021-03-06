package com.liu.laravel.api;

import android.text.TextUtils;

import com.liu.laravel.BuildConfig;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 项目名称：Laravel
 * 类描述：网络请求封装
 * 创建人：liuxuhui
 * 创建时间：2017/3/15 16:21
 * 修改人：liuxuhui
 * 修改时间：2017/3/15 16:21
 * 修改备注：
 */

public class ApiHttpClient {

    private static final int DEFAULT_TIMEOUT = 5;

    private static TopicApi topicApi;

    private static TokenApi tokenApi;

    private static UserApi userApi;

    private static String mToken = "";

    public static String getmToken() {
        return mToken;
    }

    public static void setmToken(String mToken) {
        ApiHttpClient.mToken = mToken;
    }

    private static ApiHttpClient mHttpClient;

    public static ApiHttpClient getInstance(){
        if(mHttpClient == null){
            mHttpClient = new ApiHttpClient();
        }
        return mHttpClient;
    }

    public TokenApi getTokenApi(){
        return tokenApi == null ? configRetrofit(TokenApi.class, true) : tokenApi;
    }

    public TopicApi getTopicApi(){
        return topicApi == null ? configRetrofit(TopicApi.class, false) : topicApi;
    }

    public UserApi getUserApi(){
        return userApi == null ? configRetrofit(UserApi.class, true) : userApi;
    }

    private <T> T configRetrofit(Class<T> service, boolean isNeedToken){
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(BuildConfig.API_BASE_URL)
                .client(configClient(isNeedToken))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        Retrofit retrofit = builder.build();
        return retrofit.create(service);
    }

    /**
     * 创建OkHttpClient
     * @param isNeedToken 是否需要token
     * @return
     */
    private OkHttpClient configClient(final boolean isNeedToken){
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

        //添加请求头 Header
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                builder.addHeader("X-Client-Platform", "Android");
                builder.addHeader("X-Client-Version", BuildConfig.VERSION_NAME);
                builder.addHeader("X-Client-Build", String.valueOf(BuildConfig.VERSION_CODE));

                builder.removeHeader("Accept");
                if(isNeedToken){
                    builder.addHeader("Accept", "application/vnd.PHPHub.v1+json");
                }else {
                    builder.addHeader("Accept", "application/vnd.OralMaster.v1+json");
                }
                if (!TextUtils.isEmpty(mToken)) {
                    builder.addHeader("Authorization", "Bearer " + mToken);
                }

                Request request = builder.build();
                return chain.proceed(request);
            }
        };

        //Log信息拦截
        if(BuildConfig.DEBUG){
            Interceptor logInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Response response = chain.proceed(request);
                    ResponseBody responseBody = response.body();
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE);
                    Buffer buffer = source.buffer();
                    Charset UTF8 = Charset.forName("UTF-8");

                    String json = buffer.clone().readString(UTF8);
                    Logger.json(json);
                    Logger.d("REQUEST_URL=====" + request.url().toString());
                    return response;
                }
            };
            okHttpClient.addInterceptor(logInterceptor);
        }

        okHttpClient.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        okHttpClient.addInterceptor(headerInterceptor);
        return okHttpClient.build();
    }
}
