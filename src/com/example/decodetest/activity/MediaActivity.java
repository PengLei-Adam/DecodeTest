package com.example.decodetest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.example.decodetest.R;
import com.example.decodetest.ui.fragment.MediaLibraryFragment;

/**
 * Activity to show the media library like photos and videos.
 * Created by leip on 2016/7/6.
 */
public class MediaActivity extends FragmentActivity {


    public static final int ID_ACTION_MEDIA = R.string.action_view_media;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_layout);

        ViewPager viewPager = (ViewPager)findViewById(R.id.mediaPager);

        MediaPagerAdapter mediaAdapter = new MediaPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mediaAdapter);
    }



    public static class MediaPagerAdapter extends FragmentPagerAdapter {

        private static final int PAGE_COUNT = 2;

        public MediaPagerAdapter(FragmentManager manager){
            super(manager);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i){
                case 0:
                    return MediaLibraryFragment.newInstance(0, "图片");
                case 1:
                    return MediaLibraryFragment.newInstance(1, "视频");
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "图片";
                case 1:
                    return "视频";
                default:
                    return null;
            }
        }
    }
}
