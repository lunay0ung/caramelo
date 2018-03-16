package com.example.luna.caramelo.Main;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.luna.caramelo.Favorites.FavoriteActivity;
import com.example.luna.caramelo.Favorites.FavoriteSite.Site;
import com.example.luna.caramelo.Favorites.FavoriteSite.SiteListAdapter;
import com.example.luna.caramelo.R;
import com.example.luna.caramelo.Settings.SettingActivity;
import com.example.luna.caramelo.TextRecognition.DoorActivity;
import com.example.luna.caramelo.TextRecognition.TextRecognitionActivity;
import com.example.luna.caramelo.Tools.BackPressCloseHandler;
import com.example.luna.caramelo.Wordbook.WordbookActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

    //메인 액티비티에서는 탭 레이아웃/뷰페이저를 통해서 세 개의 탭을 보여줌
    //각각 음악, 뉴스, 기사 카테고리이며, 프래그먼트가 사용됨
    //프래그먼트 명은 MusicFragment, NewsFragment, OthersFragment임
    //커스텀 리스트 뷰를 사용했으며, 리스트뷰 어댑터 클래스 명은 SiteListAdapter임
    //메인에서는 이용가능한 모든 웹사이트 목록을 볼 수 있으며, 여기서 즐겨찾기로 저장한 사이트들은 서버에 저장된 후
    //FavoriteActivity=스크랩 메뉴에서 다시 불러들임.
    //FavoriteActivity에서도 탭레이아웃/뷰페이저가 쓰였으며, 각 탭은 Fav_(주제)Fragment와 연결됨
    //+20171219 현재엔 웹사이트 아닌 웹페이지를 스크랩하는 기능은 구현되지 않았으나 추후 구현할 예정이므로
    //Favorite Activity에서는 웹사이트와 웹페이지 즐겨찾기 목록을 둘다 볼 수 있도록 탭호스트도 쓰임
    //탭호스트 명은 즐겨찾기(for 웹사이트), 북마크(for 웹페이지)임.

public class MainActivity extends AppCompatActivity{

    private String TAG = "메인";

    //탭 레이아웃
    private TabLayout tabLayout;
    private ViewPager viewPager;

    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    List<Site> siteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //카카오톡 로그인 연동을 위해 키 해시를 얻고자 함
        //17-12-24
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.luna.cacao_test", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);

        //FCM
        FirebaseMessaging.getInstance().subscribeToTopic("ALL");
        FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "토큰 인스턴스: "+FirebaseInstanceId.getInstance().getToken()+"");


        // Initializing the TabLayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("음악"));
        tabLayout.addTab(tabLayout.newTab().setText("뉴스"));
        tabLayout.addTab(tabLayout.newTab().setText("기타"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });//


        //탭에 띄워줄 리스트뷰
        ListView listView = (ListView) findViewById(R.id.listView);
        siteList = new ArrayList<Site>();
        SiteListAdapter siteListAdapter = new SiteListAdapter(this, R.layout.site_custom_list, siteList);

    }//onCreate

    //바텀메뉴
    public void goHome(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        //Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
    }

    public void goFavorites(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        startActivity(new Intent(getApplicationContext(), FavoriteActivity.class));
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
        startActivity(new Intent(getApplicationContext(), SettingActivity.class));
        overridePendingTransition(0, 0);
        // finish();
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
