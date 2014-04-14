package com.enee408g.squealer.android;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ReceiverFragment extends Fragment {
	
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_receiver, container, false);
	      
	      Log.i("ENEE408G", "ReceiverFragment onCreateView");
	      
	      return rootView;
	  }
}
