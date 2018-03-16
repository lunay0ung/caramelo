package com.example.luna.caramelo.Main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by LUNA on 2017-12-12.
 */
//메인 액티비티에서 사용한 뷰페이저를 위한 것
//음악, 뉴스, 기타탭과 음악, 뉴스, 기타 프래그먼트를 연결
public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                MusicFragment musicFragment = new MusicFragment();
                return musicFragment;
            case 1:
                NewsFragment newsFragment = new NewsFragment();
                return newsFragment;
            case 2:
                OthersFragment othersFragment = new OthersFragment();
                return othersFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}