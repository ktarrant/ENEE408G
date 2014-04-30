package com.enee408g.squealer.android;

import java.io.IOException;

import com.enee408g.squealer.android.AudioRecorder.UpdateListener;

import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

public class TestFragment extends Fragment {
	
	private final static String TAG = "TestFragment";
	private boolean isRecording = false;
	private AudioRecorder recorder = null;

	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_test, container, false);
	      
	      final Button startButton = (Button) rootView.findViewById(R.id.test_button_record);
	      final EditText numberDisplay = (EditText) rootView.findViewById(R.id.test_numberDisplay);

	      recorder = new AudioRecorder();
	      recorder.setUpdateListener(new UpdateListener() {
			@Override
			public void onUpdate(int dominantFrequency) {
				numberDisplay.setText(String.format("%d Hz", dominantFrequency));
			}
	      });
	      
	      startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isRecording = !isRecording;
                if (isRecording) {
                    startButton.setText("Stop Test");
                    recorder.startRecording();
                } else {
                    startButton.setText("Start Test");
                    recorder.stopRecording();
                }
			}
	    	  
	      });
	      
	      return rootView;
	      
	  }
	  
}
