package com.example.luna.caramelo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LUNA on 2017-12-15.
 */

//171219 현재 아직 쓰임은 없으나 추후 사용할 수도 있을 것 같아서 일단 보관
//나중에도 안 쓸 것 같으면 삭제
public class Result {


        @SerializedName("error")
        private Boolean error;

        @SerializedName("message")
        private String message;

        @SerializedName("favorite")
        private Favorite favorite;

        public Result(Boolean error, String message, Favorite favorite) {
            this.error = error;
            this.message = message;
            this.favorite = favorite;
        }

        public Boolean getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public Favorite getFavorite() {
            return favorite;
        }

}
