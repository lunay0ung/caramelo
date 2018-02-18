//package com.example.luna.caramelo;
//
//import android.app.Activity;
//import android.app.Fragment;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import retrofit.Callback;
//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//
///**
// * Created by LUNA on 2017-12-26.
// */
//FIXME 각각 즐겨찾기 프래그먼트(FAV_~Fragment.java)에 어댑터를 달아줘서 이 클래스는 현재 쓰임이 없으나, 혹시 몰라서 일단 보관은 해둔다
////SiteListAdapter에는 +버튼이 있는 커스텀리스트뷰를 세팅함
////그런데 즐겨찾기 목록에도 +버튼이 있는 리스트가 올라오는 것은 부자연스러움...추가 대신 삭제 기능이 필요하므로
////그래서 plus대신 minus버튼이 있는 뷰를 뿌려줄 수있도록 새로운 어댑터 생성 ㅠㅠ
////==> 삭제할 때 서버에 보낼 데이터 세팅이 필요
//public class SiteListAdapter_minus extends ArrayAdapter<Site> {
//    //'사이트'리스트에 들어갈 리스트 값들
//    public List<Site> siteList;
//
//    //액티비티 컨텍스트
//    Context context;
//
//    //리스트 아이템을 위한 레이아웃 리소스
//    int resource;
//
//    //리스틑 아이템 포지션
//    int position;
//
//    //현재 로그인한 유저를 가져오기 위해
//    //SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, Context.MODE_PRIVATE);
//    String email, username;
//    //ㄴ즐겨찾기 데이터베이스에 유저 이메일과 유저 네임을 넣는다
//
//
//    //웹사이트인가 웹페이지인가
//    String category;
//
//    //즐겨찾기 데이터베이스에서 데이터를 조회할 때 쓸 수 있는 변수들
//    String name, des, url, type; //name은 리스트 목록의 제목 부분, des는 description, 설명. url은 사이트 주소, type은 music/news/others 중 무엇인지 밝히는 부분.
//    //type은 즐겨찾기에 저장한 목록을 스크랩 메뉴에서 불러들일 때 어떤 music, news, others 중 어떤 프래그먼트에 뿌려줘야 하는지 구별하기 위해서 저장
//
//
//
//    public SiteListAdapter_minus(Context context, int resource, List<Site> siteList) {
//        super(context, resource, siteList);
//
//        this.context = context;
//        this.resource = resource;
//        this.siteList = siteList;
//    }
//
//
//    @NonNull
//    @Override
//    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
//        //return super.getView(position, convertView, parent);
//
//
//        //리스트 아이템을 보여주는 xml파일 뷰를 얻어오기
//        //이를 위해서는 layoutinflater가 필요하다
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//
//        //뷰 가져오기
//        final View view = layoutInflater.inflate(resource, null, false);
//
//        //뷰 element를 가져오기
//        ImageView iv_photo = view.findViewById(R.id.iv_photo);
//        TextView tv_listName = view.findViewById(R.id.tv_listName);
//        TextView tv_listDes = view.findViewById(R.id.tv_listDes);
//        final Button delete = view.findViewById(R.id.btn_listDelete);
//
//
//        //지정된 포지션의 사이트들을 가져오기
//        final Site site = siteList.get(position);
//
//        //리스트 기본 구성요소 채워주기(웹사이트 로고 이미지, 웹사이트 설명하는 제목인 name, 설명 내용인 des)
//        iv_photo.setImageDrawable(context.getDrawable(site.getImage()));
//        tv_listName.setText(site.getName());
//        tv_listDes.setText(site.getDes());
//
//        //즐겨찾기에 등록한 사이트를 지운다
//        delete.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//
//                //Toast.makeText(context, position+"번 클릭", Toast.LENGTH_SHORT).show();
//
//                //로그인 됐으면 할 수 있고, 로그인 안 됐으면 로그인 후 이용할 수 있다고 말해준다.
//                //로그인 안 되어 있다면,
//                if(!SharedPrefManager.getmInstance(context).isLoggedIn()) {
//                    Toast.makeText(context, "즐겨찾기 기능은 로그인 후 이용하실 수 있습니다.", Toast.LENGTH_SHORT).show();
//                    return;
//                } else {
//                    //현재 로그인한 유저를 가져오기 위해
//                    SharedPreferences sharedPreferences = context.getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, Context.MODE_PRIVATE);
//                    //현재 로그인 한 유저 정보
//                    email = sharedPreferences.getString(SharedPrefManager.KEY_EMAIL, null);
//                    url = siteList.get(position).getUrl();
//
//                    //deleteFavorite();
//
//
//
//                    //로그인 되어있으면 -> (-) 버튼을 눌렀을 때 서버에 사용자 정보 및 즐겨찾기 데이터 목록을 보내서 데이터베이스에서 목록을 삭제해야 한다
//                    //필요한 건 유저 이메일과, url만 있으면 될 듯
//                    //처음에는 유저 이메일을 통해서 즐겨찾기 정보가 저장되어 있는지 보고, 정보가 있다면 url을 통해 데이터베이스에서 삭제한당
//                    //Toast.makeText(context, email+"----"+url, Toast.LENGTH_SHORT).show();
//                    //deleteFavorite();
//
//
//
////                    if(type.equals("music")) {
////                        ((Activity) context).startActivity(new Intent(getContext(),FavoriteActivity.class));
////                    } else if (type.equals("news")) {
////
////                    } else if (type.equals("others")) {
////
////                    }
//
//
//                    //siteList.clear(); //일단 비운 후 다시 채워준다..?
//                    //setNotifyOnChange(true); //아랫줄에 이어 얘를 추가해도 안 됨
//                   // notifyDataSetChanged(); //이것만 하면 바로바로 리스트 갱신이 안 된다
//                   // notifyDataSetInvalidated();
//
//
//                }//else 로그인 여부 조건문
//            }//onClick
//        });//클릭 리스너
//        //뷰 반환
//        return view;
//    }//getView
//
//
//    private void deleteFavorite() {
//
//        //데이터를 가져오너라
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.DELETE_FAVORITE,
//                new com.android.volley.Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.d("정보, 리스폰스 정체 파악", response);
//
//
//                        if(response.equals("deleted")) {
//                            Toast.makeText(getContext(), "해당 목록이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
//
//                            Log.d("반응1_삭제", response);
//                            return;
//                        }
//
//                        if(response.equals("noData")) {
//                            //해당 유저네임으로 즐찾 검색한 결과 등록된 데이터 없씀.
//                            //애초에 데이터가 없다면 리스트뷰 자체가 보일 일이 없지만..혹시 몰라서....
//                            Toast.makeText(getContext(), "데이터 조회 과정에서 에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
//                            Log.d("반응2_에러", response);
//                            return;
//                        }
//
//                        Log.d("반응_이도저도아닌", response);
//                    }//onResponse
//                },
//                new com.android.volley.Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                }) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//                params.put("email", email);
//                params.put("url", url);
//                return params;
//            }
//        };
//
//        VolleySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
//
//    }//deleteFavorite
//
//    //지정한 위치에 있는 데이터와 관계된 아이템의 ID를 리턴
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//
//    //지정한 위치에 있는 데이터 리턴
//    @Nullable
//    @Override
//    public Site getItem(int position) {
//        return siteList.get(position);
//    }
//
//
//
//}//SiteListAdapter_minus
