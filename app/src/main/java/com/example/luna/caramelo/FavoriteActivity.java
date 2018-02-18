package com.example.luna.caramelo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    //탭레이아웃과 뷰페이저
    private TabLayout tabLayout;
    private ViewPager viewPager;

    //private SiteListAdapter_minus siteListAdapter_minus;
    private List<Site> siteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        TextView msg = (TextView) findViewById(R.id.textView_fav);

        //탭에 띄워줄 리스트뷰
        ListView listView = (ListView) findViewById(R.id.listView);

        //로그인 안 돼있으면 이용 못
        if(!SharedPrefManager.getmInstance(this).isLoggedIn()) {
            //msg.setText("로그인 후 이용하실 수 있습니다.");

            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SettingActivity.class));
        }

        //탭호스트
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        //첫번째 탭
        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("tab spec1");
        tabSpec1.setContent(R.id.tab1);
        tabSpec1.setIndicator("즐겨찾기");
        tabHost.addTab(tabSpec1);

        //두번째 탭
        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("tab spec2");
        tabSpec2.setContent(R.id.tab2);
        tabSpec2.setIndicator("북마크");
        tabHost.addTab(tabSpec2);

        //탭 색 바꾸기->현재 구별 안 되므로 나중에 색깔 변경
        //tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.);
        //tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color);
        //백그라운드 컬러를 바꾸는 건 너무 지저분한 거 같아서....탭 선택 시 아래에 생기는 선 색깔을 파란색으로 바꿈
        tabHost.getTabWidget().getChildAt(0).getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        tabHost.getTabWidget().getChildAt(1).getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

        //탭 레이아웃
        // Initializing the TabLayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("음악"));
        tabLayout.addTab(tabLayout.newTab().setText("뉴스"));
        tabLayout.addTab(tabLayout.newTab().setText("기타"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Creating TabPagerAdapter adapter
        Fav_TabPagerAdapter fav_tabPagerAdapter = new Fav_TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(fav_tabPagerAdapter);
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
        });

    }//onCreate


    //뒤로 가기 두번 누르면 종료
    @Override public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }//onBackPressed


    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        //메뉴 이동 위해
        Intent intent;

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    intent = new Intent(FavoriteActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_favoite:
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_notebook:
                    startActivity(new Intent(FavoriteActivity.this, NotebookActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_setting:
                    startActivity(new Intent(FavoriteActivity.this, SettingActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        }
    };

}//activity
