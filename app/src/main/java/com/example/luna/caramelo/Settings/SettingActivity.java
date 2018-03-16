package com.example.luna.caramelo.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.luna.caramelo.Settings.Account.AccountManagementActivity;
import com.example.luna.caramelo.Settings.Account.JoinGeneralActivity;
import com.example.luna.caramelo.Settings.Account.JoinKakaoActivity;
import com.example.luna.caramelo.Settings.Account.LoginGeneralActivity;
import com.example.luna.caramelo.Settings.Account.SharedPrefManager;
import com.example.luna.caramelo.Settings.Account.User;
import com.example.luna.caramelo.Settings.QnA.QnaListActivity;
import com.example.luna.caramelo.TextRecognition.DoorActivity;
import com.example.luna.caramelo.TextRecognition.TextRecognitionActivity;
import com.example.luna.caramelo.Tools.BackPressCloseHandler;
import com.example.luna.caramelo.Favorites.FavoriteActivity;
import com.example.luna.caramelo.Main.MainActivity;
import com.example.luna.caramelo.R;
import com.example.luna.caramelo.Favorites.FavoriteSite.VolleySingleton;
import com.example.luna.caramelo.Wordbook.WordbookActivity;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/*
* 회원가입 및 로그인 진행 가능.
*
* 계정관리, 사용설정, 고객센터-3가지 세부항목으로 이루어져 있다.
* 위의 서비스는 로그인 후에만 이용 가능하다.
* */

public class SettingActivity extends AppCompatActivity {

    //태그
    static final String TAG = "설정SettingActivity";
    static final String TAG_KAKAO = "카카오로그인settingActivity";

    private Context mContext;

    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    //request code
    static final int loginStatus_Request = 1;

    //로그인 후 이메일, 유저네임을 보여줄 부분 (로그인 전엔 여기로 로그인해달라는 메시지)
    TextView tv_loginEmail, tv_loginUsername;

    //로그인 버튼
    Button btn_goGeneral; //일반 로그인 버튼
    LoginButton btn_kakaoLogin; //카카오톡 로그인 버튼 17-12-24

    private SessionCallback callback; //카카오 로그인을 위한 콜백

    /*
    * 카카오 로그인 버튼을 누르면 계정선택 창이 뜬다(카카오톡으로 로그인, 카카오 계정으로 로그인)
    *
    * 어떤 걸 선택하든 JoinGeneralActivity로 감. 이메일은 받고 유저네임은 새로 설정하게 한 후 서버에 type-kakao와 같이 넣어준다
    * 로그인 된 화면은 일반 로그인 된 화면의 버튼과 같다
    * */


    //계정관리 버튼
    Button btn_profile;


    //구글 로그인관련
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_GET_TOKEN = 9002;
    String username, email, password;
    EditText et_username;
    TextView tv_usernameMsg, tv_toolbar;

    //고객센터 항목
    TextView tv_userGuide; //'이용안내' 메뉴
    TextView tv_inquiry; //1:1문의

    String type; //가입타입(일반/카카오)
    Boolean wantToCloseDialog;

    //데이터 환경 변화 시 알림 허용 스위치
    Switch switch_dataAlarm;

    //알림 허용 스위치가 값을 NetworkChangeReceiver에서 공유하기 위해
    public static boolean isChecked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        //현재 유저는 뉴규?
        final User user = SharedPrefManager.getmInstance(this).getUser();

        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);


        //BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        tv_toolbar = (TextView) findViewById(R.id.tv_toolbar); //툴바에 넣어줄 글씨
        tv_toolbar.setText("설정");

        //객체 pick
        btn_kakaoLogin = (LoginButton) findViewById(R.id.btn_kakaoLogin); //카카오 로그인 버튼
        btn_goGeneral = (Button) findViewById(R.id.btn_goGeneral);
        //tv_logOut= (TextView) findViewById(R.id.tv_logOut);
        tv_loginUsername = (TextView) findViewById(R.id.tv_loginUsername);
        tv_loginEmail = (TextView) findViewById(R.id.tv_loginEmail);
        btn_profile = (Button) findViewById(R.id.btn_profile);
        tv_userGuide = (TextView) findViewById(R.id.tv_userGuide); //이용 안내
        tv_inquiry = (TextView) findViewById(R.id.tv_inquiry); //1:1문의


        //데이터 환경 변경시 알림 허용 여부
        switch_dataAlarm = (Switch) findViewById(R.id.switch_dataAlarm);

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefManager.SHARED_PREF_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor editor= sharedPreferences.edit();

        //스위치의 체크 이벤트를 위한 리스너 등록
        //로그인 안 돼있으면 정보를 저장할 수 없다! -> 로그인 여부 체크해야 함
        switch_dataAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //알림 허용 시
                if(isChecked == true) {
                    //TODO 이 값을 저장해야됨-> 2017-12-22 아직 서버에 저장하지 않아서 로그아웃 후 재로그인하면 스위치 꺼져있음.
                    //ㄴ굳이 서버에 저장해야 하나 생각도 듦. 일단 이런 어플은 로그인/로그아웃을 자주 안 하잖앙

                    if(!SharedPrefManager.getmInstance(getApplicationContext()).isLoggedIn()){
                        Toast.makeText(SettingActivity.this, "로그인 후 이용하실 수 있습니다.", Toast.LENGTH_SHORT).show();;
                        isChecked = false;
                        switch_dataAlarm.setChecked(isChecked);
                    } else {
                        //로그인 된 상태라면 KEY_DATA_ALARM 키값으로 알림허용, 즉 true를 저장한다
                        isChecked = true;
                        switch_dataAlarm.setChecked(isChecked);
                        editor.putBoolean(SharedPrefManager.KEY_DATA_ALARM, true);
                        editor.apply(); //apply를 안 해주면서 왜 저장이 안 되지, 하고 있었다 ^^.
//
                    }
                } else if(isChecked ==false) {
                    //editor.putBoolean(SharedPrefManager.KEY_DATA_ALARM, false);
                    //ㄴ디폴트가 false이므로 굳이 또 저장할 필요가 없어서 무효화 --> 아님. 만약 로그인이 유지된 상태에서 스위치를 켰다 끄면 true인 상태가 그대로 저장되어있잖아 ㅠㅠ
                    editor.putBoolean(SharedPrefManager.KEY_DATA_ALARM, false);
                    editor.apply();
                }

            }//onCheckedChanged
        });//setOnCheckedChangeListener

        //데이터 알람 허용/비허용 여부에 따라 스위치에 on/off 해두어야 함
        if(user.isGetDataNoti() == true) {
            switch_dataAlarm.setChecked(true);
        } else {
            switch_dataAlarm.setChecked(false);
        }


        //로그인 여부 체크
        //로그인 되어 있다면
        if(SharedPrefManager.getmInstance(this).isLoggedIn()){
            tv_loginEmail.setText(user.getEmail()); //유저이메일 세팅
            tv_loginUsername.setText(user.getUsername()); //유저네임 세팅
            btn_goGeneral.setVisibility(View.GONE); //일반로그인 버튼 숨기기
            btn_kakaoLogin.setVisibility(View.GONE); //카카오 로그인 버튼 숨기기
            //tv_logOut.setVisibility(View.VISIBLE); //로그아웃 버튼 보이기
            btn_profile.setVisibility(View.VISIBLE); //계정관리 버튼 보이기
        }


        //위젯에 리스너 달기
        btn_goGeneral.setOnClickListener(mClickListner);
        //tv_logOut.setOnClickListener(mClickListner);
        tv_userGuide.setOnClickListener(mClickListner);
        btn_profile.setOnClickListener(mClickListner);
        tv_inquiry.setOnClickListener(mClickListner);

        //카카오톡 콜백
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

    }




    // 버튼 혹은 기타 뷰 클릭
    //***리스너 달아주는 것 잊지마!
    View.OnClickListener mClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //일반 로그인
                case R.id.btn_goGeneral : {
                    Intent intent;
                    intent = new Intent(SettingActivity.this, LoginGeneralActivity.class);
                    startActivityForResult(intent, loginStatus_Request);
                    overridePendingTransition(0, 0);
                    //finish(); //뒤로 가기를 누르면 바로 세팅 액티비티화면으로 오도록 세팅액티비티를 그대로 둔다
                    return;
                }//btn_goGeneral

                //프로필
                case R.id.btn_profile: {

                    startActivity(new Intent(SettingActivity.this, AccountManagementActivity.class));
                    overridePendingTransition(0, 0);
                    //finish(); //뒤로 가기를 누르면 바로 세팅 액티비티화면으로 오도록 세팅액티비티를 그대로 둔다
                    return;
                }//btn_profile
                //로그아웃
                case R.id.tv_logOut: {
                    SharedPrefManager.getmInstance(getApplicationContext()).logout();
                    finish();
                    return;
                }//tv_logOut

                //'이용안내' 액티비티로 이동
                case R.id.tv_userGuide: { //리스너를 안 달아주고 왜 클릭이벤트가 안 먹히는지 의아해했네 ^^..
                    startActivity(new Intent(SettingActivity.this, GuideActivity.class));
                    overridePendingTransition(0, 0);
                    //finish(); ->이용안내 액티비티에서 뒤로 가기를 누르면 바로 세팅 액티비티화면으로 오도록 세팅액티비티를 그대로 둔다
                    return;
                }//tv_userGuide

                case R.id.tv_inquiry : { //1:1로 문의하거나 내역을 확인할 수 있는 액티비티로 이동한다

                    if(!SharedPrefManager.getmInstance(getApplicationContext()).isLoggedIn()) {
                        Toast.makeText(getApplicationContext(), "로그인 후 이용하실 수 있습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(getApplicationContext(), QnaListActivity.class));
                        overridePendingTransition(0,0);
                    }//else

                    return;
                }//tv_inquiry
            }//switch
        }//onClick
    };//OnClickListner

    //인텐트씨의 귀환
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == loginStatus_Request) {
            Log.d(TAG,"리퀘스트코드 = loginStatus_Request");
            if(resultCode == Activity.RESULT_OK) {
                //TODO 이메일로 유저네임도 서버에서 가져와서 배치해줘야됨.
                String getUsername = data.getStringExtra("getUsername");
                String getEmail = data.getStringExtra("getEmail");
                tv_loginUsername.setText(getUsername);
                tv_loginEmail.setText(getEmail);
                Log.d(TAG, "받은 이메일:"+getEmail);

                //로그인 버튼 없애기
                btn_goGeneral.setVisibility(View.GONE);
                //btn_goGoogle.setVisibility(View.GONE);

                //로그아웃 생기기
                //btn_logOut.setVisibility(View.VISIBLE);
            }
        }//loginStatus_Request

        //간편로그인 시 호출, 없으면 간편로그인 시 로그인 성공화면으로 넘어가지 않음
//        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
//            return;
//        }//if

    }//onActivityResult

    //TODO 테스트용. 카카오 로그인 완성되면 지울 것
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }//onDestroy

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            Toast.makeText(SettingActivity.this, "세션연결성공", Toast.LENGTH_SHORT).show();
            Log.d(TAG_KAKAO,"세션연결 성공");
            requestMe();
           //redirectJoinActivity();

        }//onSessionOpened

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception !=null) {
                Log.d(TAG_KAKAO,"세션연결 실패ㅠㅠ");
                Toast.makeText(getApplicationContext(), "세션연결실패", Toast.LENGTH_SHORT).show();
                Logger.e(exception);
            }
          //  setContentView(R.layout.activity_main);
            //세션연결 실패 시 로그인화면 다시 불러옴
        }//onSessionOpenFailed
    }//SessionCallback

    private void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {

            }

            @Override
            public void onNotSignedUp() {

            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                email = userProfile.getEmail();
                type = "kakao";
                Log.d(TAG, "세션연결후"+userProfile.getEmail()+type);
                Toast.makeText(getApplicationContext(), ""+userProfile.getEmail(), Toast.LENGTH_SHORT).show();

                checkAccount();
            }//onSuccess
        });//MeResponseCallback
    }//requestMe


    private void checkAccount() {

        //데이터를 가져오너라
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_KAKAO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.d("정보, 리스폰스 정체 파악", response);
                        Log.d(TAG,"여기오냐3"); //옴
                        Log.d(TAG, "리스폰스"+email+type); //제대로 받아옴
                        Log.d(TAG,"리스폰스 내용"+response);
                        if(response.equals("noUser")) { //기존 데이터베이스에 없는 유저 정보. 이메일은 그대로 받고 가입시키면 된다.
                            Toast.makeText(getApplicationContext(), "유저네임을 설정해주세요.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SettingActivity.this, JoinKakaoActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("type", type);
                            startActivity(intent);
                            return;

                        }

                        if(response.equals("error")) {
                            Toast.makeText(getApplicationContext(), "데이터 조회 과정에서 에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(response.equals("general")) { //계정이 디비에 있긴 있는데 타입이 제너럴이면, 이메일로 가입한 거니까 이메일 입력해서 로그인하게 해야한다
                            Toast.makeText(getApplicationContext(), "일반 로그인이 필요한 계정입니다.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SettingActivity.this, JoinGeneralActivity.class));
                            return;
                        }

                        //위의 모든 경우가 아니고 제대로 로그인 될 수 있는 계정이라면, 정보를 담아서 셰어드에 저장한당
                        try {
                            //converting response to json object
                            JSONObject userInfo = new JSONObject(response);

                            Log.d("정보1", response);

                            String TAG = "유저정보";
                            Log.d(TAG, String.valueOf(userInfo.getInt("user_no")));
                            Log.d(TAG, userInfo.getString("user_no"));
                            //Log.d(TAG, String.valueOf(userInfo.getInt("created_at")));
                            Log.d(TAG, userInfo.getString("created_at"));
                            //created_at..이거 string임, php에서 date함수로 가져와서 연/월/일 형식으로 바꿔서 그런가? -> 당연한듯 ㅡㅡ...':' 이런ㄱ ㅔ있는데 인티져겠니..?
                            Log.d(TAG, userInfo.getString("email"));
                            Log.d(TAG, userInfo.getString("username"));
                            Log.d(TAG, userInfo.getString("type"));


                            //creating a new user object
                            //실제로 유저에게 보여줄 건 이메일, 유저네임이지만,
                            //앞으로 다른 정보가 어떤 게 필요하고 어떻게 쓰일지 아직 모르므로
                            //모든 정보를 일단 유저 객체에 넣는다
                            User user = new User(
                                    userInfo.getString("user_no"),
                                    userInfo.getString("email"),
                                    userInfo.getString("username"),
                                    userInfo.getString("created_at"),
                                    userInfo.getString("type"),
                                    false
                            );

                            //false는 데이터 환경 변경 시 nofi를 받냐 안 받느냐에 대한 것. 2017-12-22에 추가
                            //SettingActivity의 '사용설정'항목의 스위치와 관련.
                            Log.d("유저오브젝트 체크, 정보", userInfo.getString("user_no"));
                            Log.d("유저오브젝트 데이터 노티 체크여부", user.isGetDataNoti()+"");

                            //storing the user in shared preferences
                            SharedPrefManager.getmInstance(getApplicationContext()).userLogin(user);

                            //starting the profile activity
                            finish();
                            startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                            overridePendingTransition(0, 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }//catch

                    }//onResponse
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("type", type);
                Log.d(TAG, email+type+"");
                return params;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }//checkAccount



    //카카오톡 로그아웃
    //TODO 아직 안 쓰지만 혹시 쓸지도 몰라서 보관 17-12-24
    private void requestLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_kakaoLogin.setVisibility(View.GONE);
                        Toast.makeText(SettingActivity.this, "로그아웃 성공", Toast.LENGTH_SHORT).show();
                    }//run
                });//Runnable
            }//onCompleteLogout
        });//LogoutResponseCallback
    }//requestLogout




    //바텀메뉴
    public void goHome(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(0, 0);
        //Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
    }

    public void goFavorites(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        startActivity(new Intent(getApplicationContext(), FavoriteActivity.class)); //현재위치
        overridePendingTransition(0, 0);
        //  finish();
    }
    public void goWordbook(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        startActivity(new Intent(getApplicationContext(), WordbookActivity.class));
        overridePendingTransition(0, 0);
        //  finish();
    }

    public void goRecognizeText(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        startActivity(new Intent(getApplicationContext(), TextRecognitionActivity.class));
        overridePendingTransition(0, 0);
    }


    public void goSettings(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        //startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        //overridePendingTransition(0, 0);
        // finish();
    }

    //메뉴
//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//        //메뉴 이동 위해
//        Intent intent;
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.nav_home:
//                    intent = new Intent(SettingActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(0, 0);
//                    return true;
//                case R.id.nav_favoite:
//                    startActivity(new Intent(SettingActivity.this, FavoriteActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
//                case R.id.nav_notebook:
//                    startActivity(new Intent(SettingActivity.this, WordbookActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
//                case R.id.nav_setting:
//                    overridePendingTransition(0, 0);
//
//                    return true;
//            }
//            return false;
//        }
//    };
//

    //뒤로 가기 두번 누르면 종료
    @Override public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }


    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

}
