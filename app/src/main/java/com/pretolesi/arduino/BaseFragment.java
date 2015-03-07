package com.pretolesi.arduino;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;

/**
 *
 */
public class BaseFragment extends Fragment implements ProgressUpdate {

    private static final String TAG = "BaseFragment";

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onProgressUpdate(ProgressUpdateData[] pud) {

    }

    @Override
    public void onProgressUpdateConnectionChanged(ProgressUpdateData[] pud) {

    }
}
