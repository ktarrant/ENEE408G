package com.enee408g.squealer.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TransmitterFragment extends Fragment {
	
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_transmitter, container, false);

	      
	      
	      return rootView;
	  }
	  
	  // Updates all the controls to their preference values
	  public void update() {
		  
	  }
}
