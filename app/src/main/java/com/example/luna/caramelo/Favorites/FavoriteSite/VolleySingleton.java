package com.example.luna.caramelo.Favorites.FavoriteSite;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by LUNA on 2017-12-11.
 */
    //다양 한 곳에서 쓰임
    //1) 구글 계정을 통한 로그인이 아닌 일반 로그인 시 volley를 이용해서 유저 정보를 불러들임
    //2) fav_news, music, othersFragment에서 쓰임. 즉, 즐겨찾기 목록을 주제별로 보여줄 때 쓰임.
    //  참고로 즐겨찾기 등록은 어댑터에서(SiteListAdapter) 하며, 그때는 레트로핏을 사용했음
public class VolleySingleton {

    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private VolleySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
