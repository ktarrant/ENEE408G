package com.enee408g.squealer.android;

import java.io.IOException;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.media.MediaRecorder;

public class ReceiverFragment extends Fragment {
	
	private AudioRecorder record=null;
	boolean isListening=false;//false means not listening
	
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_receiver, container, false);
	      
	      Button startButton = (Button) rootView.findViewById(R.id.receiver_button);
	       
	      record = new AudioRecorder();
	      
	      startButton.setOnClickListener(new OnClickListener() {
	    	  
	    	  
	    	  
			@Override
			public void onClick(View arg0) {
				isListening=!isListening;
				if(isListening)
				{
					Intent intent = new Intent(getActivity(), AudioRecorder.class);
					startActivity(intent);
				}
				else
				{
				
				}
			}
	    	  
	      });
	      
	      Log.i("ENEE408G", "ReceiverFragment onCreateView");
	      
	      
	      return rootView;
	      
	  }
	      
	      
	      
	  
}
