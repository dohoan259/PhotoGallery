package com.example.hoanbk.photogallery;

import android.support.v4.app.Fragment;

/**
 * Created by hoanbk on 3/23/2017.
 */

public class PhotoPageActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PhotoPageFragment();
    }
}
