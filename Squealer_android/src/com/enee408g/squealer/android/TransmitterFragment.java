package com.enee408g.squealer.android;

import java.io.UnsupportedEncodingException;

import com.enee408g.squealer.android.SineGenerator.PlaybackFinishedListener;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class TransmitterFragment extends Fragment {
	
	  private SineGenerator gen = null;
      private Button goButton = null;
      private EditText textView = null;
	
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_transmitter, container, false);

	      goButton = (Button) rootView.findViewById(R.id.transmitter_button);
	      textView = (EditText) rootView.findViewById(R.id.transmitter_message);
	      
	      gen = new SineGenerator(getActivity(), PreferenceHelper.getFartFrequency(getActivity()),
	    		  PreferenceHelper.getAllBitFrequencies(getActivity()));
	      gen.setPlaybackFinishedListener(new PlaybackFinishedListener() {
			@Override
			public void onPlaybackFinished(boolean cancelled) {
				if (!cancelled) {
					goButton.setText(getString(R.string.transmitter_start_label));
				}
			}
	      });
	      
	      goButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!gen.isPlaying()) {
					startPlaying();
				} else {
					stopPlaying();
				}
			} 
	      });
	      
	      
	      return rootView;
	  }
	  
	  public void startPlaying() {
		String msg = textView.getText().toString();
		byte[] b;
		try {
			b = msg.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		gen.play(b);
		goButton.setText(getString(R.string.transmitter_abort_label));
	  }
	  
	  public void stopPlaying() {
		  gen.cancel();
		  goButton.setText(getString(R.string.transmitter_start_label));
	  }
}
