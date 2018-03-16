package com.example.luna.caramelo.Settings.Account;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.luna.caramelo.R;
import com.example.luna.caramelo.Tools.BackPressCloseHandler;

/*
*[설정]화면에서 계정관리 버튼을 누르면 뜨는 액티비티
* 회원정보수정, 로그아웃, 회원탈퇴의 세부항목으로 이동할 수 있다
*로그아웃을 제외한 항목은 또 다른 액티비티로 이어진다
* */
public class AccountManagementActivity extends AppCompatActivity {

    private static final String log = "계정관리액티비티 로그";


    String username, email; //현재 유저정보
    String password;

    TextView tv_toolbar;
    TextView tv_updateInfo; //회원정보 수정
    TextView tv_logOut; //로그아웃
    TextView tv_withdraw; //탈퇴


    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountmanagement);

        initViews(); //변수 참조 메소드


        //현재 로그인 된 유저의 정보를 가져온다
        final User user = SharedPrefManager.getmInstance(this).getUser();
        username = user.getUsername(); //유저네임
        email = user.getEmail(); //유저계정

        //툴바에 현재 위치를 적어준다
        tv_toolbar.setText("계정 관리");

        //클릭리스너 달아주기
        tv_updateInfo.setOnClickListener(mClickListner);
        tv_logOut.setOnClickListener(mClickListner);
        tv_withdraw.setOnClickListener(mClickListner);


        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);

    }//onCreate


    //클릭리스너
    View.OnClickListener mClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_updateInfo:
                    //회원정보를 수정하는 액티비티로 보낸다
                    startActivity(new Intent(AccountManagementActivity.this, UpdateInfoActivity.class));
                    //애니메이션 없앰
                    overridePendingTransition(0, 0);
                    return;

                case R.id.tv_logOut:
                    logOut(); //로그아웃 시킨다
                    return;

                case R.id.tv_withdraw:
                    confirmToWithdraw();

                    return;
            }//switch
        }//onClick
    };//OnClickListener


    //유저가 수정한 닉네임과 비밀번호를 서버로 보낸 후 제대로 수정되었는지 여부를 판별한다


    //로그아웃 다이얼로그
    private void logOut() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountManagementActivity.this);
        alertDialogBuilder
                .setMessage("로그아웃합니다.")
                .setCancelable(false) //뒤로 가기 클릭 시 취소가능설정 여부
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPrefManager.getmInstance(getApplicationContext()).logout();
                        dialog.cancel();
                        finish(); //액티비티 finish
                    }//onClick
                })

                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel(); //다이얼로그를 벗어난다
                    }//onClick
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }//logOut

    //탈퇴 다이얼로그
    //정말 탈퇴할 거냐고 한번 더 묻는다
    //재차 탈퇴의사를 표시하면 비밀번호를 다시 한번 입력받는다
    //비밀번호가 일치하면 탈퇴를 진행한 후 세팅액티비티로 보내고,
    //일치하지 않으면 그대로 일치하지 않는다는 메시지를 보낸다
    private void confirmToWithdraw() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountManagementActivity.this);
        alertDialogBuilder
                .setMessage("정말 탈퇴하시겠습니까?")
                .setCancelable(false) //뒤로 가기 클릭 시 취소가능설정 여부
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //애니메이션 없앰
                        overridePendingTransition(0, 0);
                        startActivity(new Intent(AccountManagementActivity.this, WithdrawActivity.class));
                    }//onClick
                })

                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel(); //다이얼로그를 벗어난다
                    }//onClick
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }//withdrawAccount


    private void initViews() { //변수 참조

        tv_toolbar = (TextView) findViewById(R.id.tv_toolbar);
        tv_updateInfo = (TextView) findViewById(R.id.tv_updateInfo);
        tv_logOut = (TextView) findViewById(R.id.tv_logOut);
        tv_withdraw = (TextView) findViewById(R.id.tv_withdraw);


    }//initViews

    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

    //뒤로 가기 두번 누르면 종료
    @Override public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }



}//AccountManagementActivity
