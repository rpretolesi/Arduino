package com.pretolesi.arduino;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 */
public class BaseFragment extends Fragment implements ProgressUpdate {

    private static final String TAG = "BaseFragment";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Log.d(TAG, "onAttach()");
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Log.d(TAG, "onCreate()");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateView()");
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Log.d(TAG, "onActivityCreated()");
    }
    @Override
    public void onViewStateRestored (Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Log.d(TAG, "onViewStateRestored()");
    }
    @Override
    public void onStart () {
        super.onStart();
        // Log.d(TAG, "onStart()");
    }
    @Override
    public void onResume (){
        super.onResume();
        // Log.d(TAG, "onResume()");
    }
    @Override
    public void onPause() {
        super.onPause();
        // Log.d(TAG, "onPause()");
    }
    @Override
    public void onStop () {
        super.onStop();
        // Log.d(TAG, "onStop()");
    }
    @Override
    public void onDestroyView () {
        super.onDestroyView();
        // Log.d(TAG, "onDestroyView()");
    }
    @Override
    public void onDestroy () {
        super.onDestroy();
        // Log.d(TAG, "onDestroy()");
    }
    @Override
    public void onDetach () {
        super.onDetach();
        // Log.d(TAG, "onDetach()");
    }

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
