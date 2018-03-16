package com.example.luna.caramelo.Favorites;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.caramelo.Favorites.Bookmark.Bookmark;
import com.example.luna.caramelo.Favorites.Bookmark.BookmarkAdapter;
import com.example.luna.caramelo.Favorites.Bookmark.BookmarkModel;
import com.example.luna.caramelo.Favorites.FavoriteSite.Fav_TabPagerAdapter;
import com.example.luna.caramelo.Favorites.FavoriteSite.Site;
import com.example.luna.caramelo.Main.WebviewActivity;
import com.example.luna.caramelo.Settings.Account.User;
import com.example.luna.caramelo.TextRecognition.DoorActivity;
import com.example.luna.caramelo.TextRecognition.TextRecognitionActivity;
import com.example.luna.caramelo.Tools.ApiClient;
import com.example.luna.caramelo.Tools.ApiService;
import com.example.luna.caramelo.Tools.BackPressCloseHandler;
import com.example.luna.caramelo.Main.MainActivity;
import com.example.luna.caramelo.R;
import com.example.luna.caramelo.Settings.SettingActivity;
import com.example.luna.caramelo.Settings.Account.SharedPrefManager;
import com.example.luna.caramelo.Tools.ResponseModel;
import com.example.luna.caramelo.Favorites.Bookmark.RecyclerItemTouchHelper;
import com.example.luna.caramelo.Wordbook.WordbookActivity;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, BookmarkAdapter.BookmarkAdapterListener, SwipeRefreshLayout.OnRefreshListener {
    //뒤로 가기 두번-> 종료
    private BackPressCloseHandler backPressCloseHandler;

    //탭레이아웃과 뷰페이저
    private TabLayout tabLayout;
    private ViewPager viewPager;

    //private SiteListAdapter_minus siteListAdapter_minus;
    private List<Site> siteList;

    //기본 레이아웃 -> for 스낵바
    ConstraintLayout constraintLayout;

    //북마크 리스트
    private List<Bookmark> bookmarkList;
    RecyclerView recyclerView;
    BookmarkAdapter bookmarkAdapter;

    String username, email; //현재 유저정보
    String url, title; //북마크 관련 정보

    //북마크 편집 시 BookmarkUpdateActiviy에서 보내는 데이터를 담을 변수
    public static final int REQUEST_UPDATE = 1;
    String newTitle;
    int  position;

    //당겨서 새로고침 구현 위한 레이아웃 변수
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        //로그인 안 돼있으면 이용 못
        if(!SharedPrefManager.getmInstance(this).isLoggedIn()) {
            //msg.setText("로그인 후 이용하실 수 있습니다.");

            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SettingActivity.class));
        } else {

            final User user = SharedPrefManager.getmInstance(this).getUser();
            username = user.getUsername();
            email = user.getEmail();
        }

        //뒤로 가기 두번 -> 종료
        backPressCloseHandler = new BackPressCloseHandler(this);

        //바텀내비게이션 대신 커스텀 바텀툴바 쓰기로...그러나 일단 코드 박제
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        TextView msg = (TextView) findViewById(R.id.textView_fav);

        //탭에 띄워줄 사이트 즐겨찾기 리스트뷰
        ListView listView = (ListView) findViewById(R.id.listView);

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

        //북마크 리사이클러뷰
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        bookmarkList = new ArrayList<>(); //북마크 재료
        bookmarkAdapter = new BookmarkAdapter(this, bookmarkList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //액티비티 레이아웃
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);

        //당겨서 새로고침!
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources( //새로고침 아이콘 알록달록 꾸며주기 ㅋㅋㅋ
                android.R.color.holo_red_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
        );

        getBookmark(username, email); //북마크리스트를 불러온다
        Log.e("북마크","불러옴");

        // 리사이클러뷰에 아이템 터치 헬퍼를 달아준다
        // 좌->우로 슬라이드할 때 목록을 삭제하도록 한다
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this );
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback1 = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // attaching the touch helper to recycler view
        new ItemTouchHelper(itemTouchHelperCallback1).attachToRecyclerView(recyclerView);

    }//onCreate


    //북마크 목록을 가져온다
    private void getBookmark(final String username, final String email) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<BookmarkModel> call = apiService.getBookmark(username, email);
        call.enqueue(new Callback<BookmarkModel>() {
            @Override
            public void onResponse(Call<BookmarkModel> call, Response<BookmarkModel> response) {
                BookmarkModel bookmarkModel = response.body();
                if(bookmarkModel.getStatus() == 1 ){
                    Log.e("북마크","불러오기 성공");
                    bookmarkList = bookmarkModel.getBookmarkList();
                    bookmarkAdapter.setBookmarkList(getApplicationContext(),bookmarkList);

                    recyclerView.setAdapter(bookmarkAdapter);

                    //최신순 정렬
                    Comparator<Bookmark> newest = new Comparator<Bookmark>() {
                        @Override
                        public int compare(Bookmark o1, Bookmark o2) {
                            return o2.getAt().compareTo(o1.getAt());
                        }//compare
                    };//comparater

                    Collections.sort(bookmarkList, newest);
                    bookmarkAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(FavoriteActivity.this, bookmarkModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }//onResponse

            @Override
            public void onFailure(Call<BookmarkModel> call, Throwable t) {
                Toast.makeText(FavoriteActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }//onFailure
        });
    }//getBookmark


    private void DeleteBookmark(String username, String url, String title){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.deleteBookmark(username, url, title);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                if(responseModel.getStatus() == 1) {
                    Snackbar snackbar = Snackbar.make(constraintLayout, "단어장에서 삭제합니다.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    Toast.makeText(FavoriteActivity.this, responseModel.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Toast.makeText(FavoriteActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }//onFailure
        });
    }//DeleteBookmark

    @Override //북마크 리스트 눌렀을 때
    public void onBookmarkSelected(Bookmark bookmark) {
        //Toast.makeText(this, ""+bookmark.getTitle(), Toast.LENGTH_SHORT).show();

        //북마크 리스트를 클릭하면 해당 url을 웹뷰 액티비티로 보낸다
        //웹뷰 액티비티에서 웹페이지를 띄움
        Intent intent = new Intent(FavoriteActivity.this, WebviewActivity.class);
        intent.putExtra("url", bookmark.getUrl());
        startActivity(intent);
        //뒤로 가기 누르면 바로 이 액티비티로 오도록 액티비티를 finish해주진 않음.
    }//onBookmarkSelected


    //단어목록을 왼쪽으로 슬라이드하면 해당 단어가 삭제되도록 한다
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof BookmarkAdapter.MyViewHolder) {
            title = bookmarkList.get(viewHolder.getAdapterPosition()).getTitle();
            url = bookmarkList.get(viewHolder.getAdapterPosition()).getUrl();
            DeleteBookmark(username, url, title);

            //리사이클러뷰에서 아이템 제거
            bookmarkAdapter.removeItem(viewHolder.getAdapterPosition());

        }//if

    }//onSwiped

    //뒤로 가기 두번 누르면 종료
    @Override public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }//onBackPressed


    @Override
    public void onRefresh() { //유저가 리스트를 끝까지 당겼다가 놓으면 호출되는 메소드
        getBookmark(username, email);
        mSwipeRefreshLayout.setRefreshing(false); //새로고침 완료
    }

    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }


    //바텀메뉴
    public void goHome(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        overridePendingTransition(0, 0);
        //Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
    }

    public void goFavorites(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        //startActivity(new Intent(getApplicationContext(), FavoriteActivity.class)); //현재위치
        //overridePendingTransition(0, 0);
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


//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//        //메뉴 이동 위해
//        Intent intent;
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.nav_home:
//                    intent = new Intent(FavoriteActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(0, 0);
//                    return true;
//                case R.id.nav_favoite:
//                    overridePendingTransition(0, 0);
//                    return true;
//                case R.id.nav_notebook:
//                    startActivity(new Intent(FavoriteActivity.this, WordbookActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
//                case R.id.nav_setting:
//                    startActivity(new Intent(FavoriteActivity.this, SettingActivity.class));
//                    overridePendingTransition(0, 0);
//                    return true;
//            }
//            return false;
//        }
//    };

}//activity
