package com.example.luna.caramelo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//일반 로그인+가입하는 유저를 위한 액티비티
//참고: 가입유형은 이렇게 일반general과 구글로그인google로 나뉘며, 이 유형은 account테이블에 type이라는 필드에 저장됨

public class LoginGeneralActivity extends AppCompatActivity {

    //바텀 내비게인션 메뉴를 위한 것
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        //메뉴 이동 위해
        Intent intent;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    intent = new Intent(LoginGeneralActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_favoite:
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_notebook:
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_setting:
                    finish();
                    startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                    overridePendingTransition(0, 0);

                    return true;
            }
            return false;
        }
    };

    //디버깅용
    private static String TAG = "로그인LoginGeneral";

    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    //인텐트
    Intent intent;

    //객체
    AutoCompleteTextView atv_loginEmail;
    EditText et_loginPassword;
    Button btn_loginGeneral, btn_goTojoinGeneral; //로그인할 것이냐, 아니면 가입창으로 이동할 것이냐
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_general);


        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);

        //객체 pick
        atv_loginEmail = (AutoCompleteTextView) findViewById(R.id.atv_loginEmail);
        et_loginPassword = (EditText) findViewById(R.id.atv_loginPassword);
        btn_loginGeneral = (Button) findViewById(R.id.btn_loginGeneral);
        btn_goTojoinGeneral = (Button) findViewById(R.id.btn_goTojoinGeneral);

        //리스너 달기
        btn_loginGeneral.setOnClickListener(mOnClickListener);
        btn_goTojoinGeneral.setOnClickListener(mOnClickListener);


    }//oncreate

    View.OnClickListener mOnClickListener;//리스너

    {
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_loginGeneral: {
                        userLogin(); //로그인합니다
                        overridePendingTransition(0, 0);
                        return;
                    }//btn_loginGeneral
                    case R.id.btn_goTojoinGeneral: { //아직 가입 안 했으므로 가입하는 액티비티로 꼬꼬
                        intent = new Intent(LoginGeneralActivity.this, JoinGeneralActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return;
                    }//btn_joinGeneral
                }//switch
            }//onClick
        };
    }

    //가입할 땐 이메일과 유저네임을 요구하지만, 로그인할 땐 이메일로 한다
     private void userLogin() {
        final String email = atv_loginEmail.getText().toString();
        final String password = et_loginPassword.getText().toString();

         // Reset errors.
         atv_loginEmail.setError(null);
         et_loginPassword.setError(null);

         // Store values at the time of the login attempt.

         boolean cancel = false;
         View focusView = null;

         //비밀번호 입력칸이 비어있거나 비밀번호가 짧으면(내 앱에선 4자 이상 되어야함) 에러메시지를 띄운다
         if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
             et_loginPassword.setError(getString(R.string.error_invalid_password));
             focusView = et_loginPassword;
             cancel = true;
         }

         // 이메일 주소가 유효한지, 비어있지는 않은지 검사
         if (TextUtils.isEmpty(email)) {
             atv_loginEmail.setError(getString(R.string.error_field_required));
             focusView = atv_loginEmail;
             cancel = true;
         } else if (!isEmailValid(email)) {
             atv_loginEmail.setError(getString(R.string.error_invalid_email));
             focusView = atv_loginEmail;
             cancel = true;
         }
         //이메일, 패스워드칸이 비어있거나 유효하지 않은 값이 입력되면 로그인 진행 안 되고
         //부적절한 에딧텍스트뷰에 포커스가 고정됨
         if (cancel) {
             // There was an error; don't attempt login and focus the first
             // form field with an error.
             focusView.requestFocus();
         } else {

             //showProgress(true);

         //모든 것이 완벽하다면, 이제 로그인 시작
             StringRequest stringRequest = new StringRequest(Request.Method.POST, URLs.URL_LOGIN,
                     new Response.Listener<String>() {
                         @Override
                         public void onResponse(String response) {
                             Log.d("정보, 리스폰스 정체 파악", response);

                             if(response.equals("noMatch")) {

                                 Toast.makeText(LoginGeneralActivity.this, "이메일 혹은 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                return;
                             }
                              if(response.equals("noUser")) {
                                 Toast.makeText(LoginGeneralActivity.this, "등록되지 않은 계정입니다.", Toast.LENGTH_SHORT).show();
                                 return;
                             }

                             if(response.equals("kakao")) {
                                 //이메일이 조회되긴 하는데 카카오로 가입했으면
                                 Toast.makeText(LoginGeneralActivity.this, "카카오 계정으로 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                                 return;
                             }
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
                             Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     }) {
                 @Override
                 protected Map<String, String> getParams() throws AuthFailureError {
                     Map<String, String> params = new HashMap<>();
                     params.put("email", email);
                     params.put("password", password);
                     return params;
                 }
             };

             VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);


         }//조건문(모든 것이 완벽쓰)

     }//userLogin

    //적절한 이메일 형식인가?
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    //패스워드가 적절한가? (4자 이상인지만 검사)
    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your  own logic
        return password.length() >= 4;
    }



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

}//activity
