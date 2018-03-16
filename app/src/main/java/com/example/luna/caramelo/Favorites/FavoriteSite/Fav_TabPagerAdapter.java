package com.example.luna.caramelo.Favorites.FavoriteSite;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by LUNA on 2017-12-12.
 */

//favorite activity=스크랩 메뉴에 쓰인 뷰 페이저를 위한 것
public class Fav_TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;

    public Fav_TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                Fav_MusicFragment fav_musicFragment = new Fav_MusicFragment();
                return fav_musicFragment;
            case 1:
                Fav_NewsFragment fav_newsFragment= new Fav_NewsFragment();
                return fav_newsFragment;
            case 2:
                Fav_OthersFragment fav_othersFragment = new Fav_OthersFragment();
                return fav_othersFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}