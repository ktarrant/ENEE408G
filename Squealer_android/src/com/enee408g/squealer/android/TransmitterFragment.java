package com.enee408g.squealer.android;

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
	      
	      PreferenceManager.getDefaultSharedPreferences(getActivity())
	      	.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

				@Override
				public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
					gen.setFrequencies((float)PreferenceHelper.getTransmitterCarrierFrequency(),
				    		  (float)PreferenceHelper.getTransmitterModulatorFrequency());
				}
	      	});
	      
	      gen = new SineGenerator(
	    		  (float)PreferenceHelper.getTransmitterCarrierFrequency(),
	    		  (float)PreferenceHelper.getTransmitterModulatorFrequency());
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
		Byte[] b = new Byte[msg.length()];
		for (int i = 0; i < msg.length(); i++) {
			b[i] = 0;
			switch(msg.charAt(i)) {
			case '1': b[i] = 1; break;
			case '2': b[i] = 2; break;
			case '3': b[i] = 3; break;
			default: b[i] = 0;
			}
		}
		gen.play(b);
		goButton.setText(getString(R.string.transmitter_abort_label));
	  }
	  
	  public void stopPlaying() {
		  gen.cancel();
		  goButton.setText(getString(R.string.transmitter_start_label));
	  }
}
