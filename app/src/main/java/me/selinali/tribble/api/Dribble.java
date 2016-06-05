package me.selinali.tribble.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import me.selinali.tribble.BuildConfig;
import me.selinali.tribble.model.Shot;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.Single;

public class Dribble {

  private static volatile Dribble sInstance;

  public static Dribble instance() {
    return sInstance == null ? sInstance = new Dribble() : sInstance;
  }

  private final Endpoints mEndpoints;
  private final Gson mGson = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();

  private Dribble() {
    mEndpoints = new Retrofit.Builder()
        .baseUrl("https://api.dribbble.com/v1/")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(mGson))
        .client(new OkHttpClient.Builder().addInterceptor(chain ->
                chain.proceed(chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + BuildConfig.DRIBBBLE_ACCESS_KEY)
                    .build())
        ).build())
        .build()
        .create(Endpoints.class);
  }

  public Single<List<Shot>> getShots(int page) {
    return mEndpoints.getShots(page).toSingle();
  }

  public Observable<Shot> getShot(int id) {
    return mEndpoints.getShot(id);
  }

  private interface Endpoints {
    @GET("shots")
    Observable<List<Shot>> getShots(@Query("page") int page);

    @GET("shots/{id}")
    Observable<Shot> getShot(@Path("id") int id);
  }
}