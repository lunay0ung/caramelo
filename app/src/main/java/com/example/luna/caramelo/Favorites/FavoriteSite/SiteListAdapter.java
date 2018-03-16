package com.example.luna.caramelo.Favorites.FavoriteSite;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


import com.example.luna.caramelo.R;
import com.example.luna.caramelo.Settings.Account.SharedPrefManager;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;


/**
 * Created by LUNA on 2017-12-11.
 */

public class SiteListAdapter  extends ArrayAdapter<Site> {

    //'사이트'리스트에 들어갈 리스트 값들
    public List<Site> siteList;

    //액티비티 컨텍스트
    Context context;

    //리스트 아이템을 위한 레이아웃 리소스
    int resource;

    //리스틑 아이템 포지션
    int position;


    //즐겨찾기 를 만들기 위한 php주소
    public static final String ROOT_URL_makeFavorite = "http://13.124.67.214/caramelo/favorite/";
    //전체 주소: 13.124.67.214/caramelo/favorite/register_favorite.php


    //현재 로그인한 유저를 가져오기 위해
    //SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    String email, username;
    //ㄴ즐겨찾기 데이터베이스에 유저 이메일과 유저 네임을 넣는다

    //웹사이트인가 웹페이지인가
    String category;

    //즐겨찾기 데이터베이스에 넣을 사이트 정보
    //int image; ->이미지 정보는 drawable이라서 서버에 넣는 과정이 복잡하기도 하고, 굳이 이미지를 저장할 필요가 없을 것 같아서 일단 폐기
    String name, des, url, type; //name은 리스트 목록의 제목 부분, des는 description, 설명. url은 사이트 주소, type은 music/news/others 중 무엇인지 밝히는 부분.
                                 //type은 즐겨찾기에 저장한 목록을 스크랩 메뉴에서 불러들일 때 어떤 music, news, others 중 어떤 프래그먼트에 뿌려줘야 하는지 구별하기 위해서 저장


    public SiteListAdapter(Context context, int resource, List<Site> siteList) {
        super(context, resource, siteList);

        this.context = context;
        this.resource = resource;
        this.siteList = siteList;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        //리스트 아이템을 보여주는 xml파일 뷰를 얻어오기
        //이를 위해서는 layoutinflater가 필요하다
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        //뷰 가져오기
        View view = layoutInflater.inflate(resource, null, false);

        //뷰 element를 가져오기
        ImageView iv_photo = view.findViewById(R.id.iv_photo);
        TextView tv_listName = view.findViewById(R.id.tv_listName);
        TextView tv_listDes = view.findViewById(R.id.tv_listDes);
        final Button scrap = view.findViewById(R.id.btn_listAdd);




        //지정된 포지션의 사이트들을 가져오기
        final Site site = siteList.get(position);

        //리스트 기본 구성요소 채워주기(웹사이트 로고 이미지, 웹사이트 설명하는 제목인 name, 설명 내용인 des)
        iv_photo.setImageDrawable(context.getDrawable(site.getImage()));
        tv_listName.setText(site.getName());
        tv_listDes.setText(site.getDes());

        //사이트를 내 리스트로 넣는 add버튼에 리스너 달기
        scrap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                     //로그인 됐으면 할 수 있고, 로그인 안 됐으면 로그인 후 이용할 수 있다고 말해준다.
                     //로그인 안 되어 있다면,
                    if(!SharedPrefManager.getmInstance(context).isLoggedIn()) {
                        Toast.makeText(context, "즐겨찾기 기능은 로그인 후 이용하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    } else {

                        //Toast.makeText(getContext(), position+"번 아이템", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getContext(), siteList.get(position).getType()+"", Toast.LENGTH_SHORT).show();

                        //원래 makeFavorite()이라는 메소드를 만들어서 여기에 메소드명만 썼었는데,
                        //그렇게 하니까 position 떄문인지 계속 목록의 첫번째 값만 불러져 와서....
                        //여기에 넣으니까 값 자체는 잘 불러져 오는데 자꾸 internal server error 500이....

                        //현재 로그인한 유저를 가져오기 위해
                        SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        //현재 로그인 한 유저 정보
                        email = sharedPreferences.getString(SharedPrefManager.KEY_EMAIL, null);
                        username = sharedPreferences.getString(SharedPrefManager.KEY_USERNAME, null);
                        category = "website";


                        //웹사이트 정보
                        //이미지가 현재 int값이므로 ㅌ테스트용으로 걍 name을 받아오기로 임시설정
                        //image = siteList.get(position).getImage();
                        name = siteList.get(position).getName();
                        des = siteList.get(position).getDes();
                        url = siteList.get(position).getUrl();
                        type = siteList.get(position).getType();

                        Log.d("클릭한 값 정체",email+username+category+name+des+type+"......."+url);

                        RestAdapter adapter = new RestAdapter.Builder()
                                     .setEndpoint(ROOT_URL_makeFavorite)
                                    .build();


                        Log.d("결과보기", email+username+category+name+des+type+":"+url);

                        //인터페이스 객체 만들기
                        fav_registerAPI api = adapter.create(fav_registerAPI.class);

                        Log.d("결과오나","1");

                        //makeFavorite메소드 정의
                        api.makeFavorite(

                                email,
                                username,
                                category,
                                name,
                                des,
                                url,
                                type,

                                new Callback<retrofit.client.Response>() {
                                    @Override
                                    public void success(retrofit.client.Response result, retrofit.client.Response response) {

                                        BufferedReader reader = null;

                                        //An string to store output from the server
                                        String output = "";

                                        try {
                                            //Initializing buffered reader
                                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));

                                            //Reading the output in the string
                                            output = reader.readLine();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                        if(output.equals("noMore")) {
                                            Log.d("중복데이터 아웃풋", output);
                                            Toast.makeText(context, "이미 즐겨찾기로 등록된 사이트입니다.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if(output.equals("failure")) {
                                            Log.d("등록 실패 아웃풋", output);
                                            Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if(output.equals("noData")) {
                                            Log.d("노데이터 아웃풋", output);
                                            Toast.makeText(context, output, Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        //성공했을 경우

                                        Toast.makeText(context, "해당 목록이 즐겨찾기에 등록되었습니다.", Toast.LENGTH_SHORT).show();

                                        Log.d("데이터 등록 후 아웃풋", output);


                                        //TODO 등록된 데이터를 즐겨찾기 리스틑에 뿌려줘야 한당.....
                                        //0) 리스트뷰 잘려서 나오는 거 수정 --done
                                        //1) 타입에 따라 다른 프래그먼트로 보낸다 -> 타입을 꺼내왓
                                        //2) 프래그먼트에서 리스틑뷰로 뿌려준다. -> 타입에 따라 각기 다른 프래그먼트로 데이터를 보내랏.
                                        //3) 이미지 처리 -> 일단..1, 2, 다 한 후에 .ㅎ

                                        //Displaying the output as a toast
                                       // Toast.makeText(context, output, Toast.LENGTH_LONG).show();
                                    }//success

                                    @Override
                                    public void failure(RetrofitError error) {
                                        Toast.makeText(context, error.toString(),Toast.LENGTH_LONG).show();
                                    }//failure
                                }//callback

                        ); //api.makeFavorite

                }//로그인 여부 조건문
            }//onClick
        });//클릭 리스너
        //뷰 반환
        return view;
    }//getView




    //아이템 리스트의 사이트들을 더한다
    private void addSite(final int position) {
        //TODO shared에 넣는 코드 짜기? or 서버에?
    }//addSite




    //지정한 위치에 있는 데이터와 관계된 아이템의 ID를 리턴

    @Override
    public long getItemId(int position) {
        return position;
    }


    //지정한 위치에 있는 데이터 리턴
    @Nullable
    @Override
    public Site getItem(int position) {
        return siteList.get(position);
    }


    public void addSite(int image, String name, String des, String url, String kind) {

        Site site = new Site(image, name, des, url, kind);
        site.setImage(image);
        site.setName(name);
        site.setDes(des);
        site.setUrl(url);
        site.setType(kind);
        siteList.add(site);
    }

}//SiteListAdapter