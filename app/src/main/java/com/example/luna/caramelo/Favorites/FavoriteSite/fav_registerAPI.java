package com.example.luna.caramelo.Favorites.FavoriteSite;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by LUNA on 2017-12-14.
 */

public interface fav_registerAPI {

    @FormUrlEncoded //서버에 post data를 보낼 거면 이걸 써줘야됨
    @POST("/register_favorite.php")
    public void makeFavorite (
      @Field("email") String email,
      @Field("username") String username,
      @Field("category") String category,
      @Field("name") String name,
      @Field("des") String des,
      @Field("url") String url,
      @Field("type") String type,
      Callback<Response> callback);
        //ㄴ레트로핏 라이브러리에 포함됨. 이걸로 서버에서의 결과를 받아옴

}//fav_registerAPI
