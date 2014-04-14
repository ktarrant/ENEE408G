package com.enee408g.squealer.android;

import android.content.SharedPreferences;
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

public class TransmitterFragment extends Fragment {
	
	  private SoundTask soundTask = null;	
      private Button goButton = null;
	
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_transmitter, container, false);

	      goButton = (Button) rootView.findViewById(R.id.transmitter_button);
	      
	      goButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (soundTask == null) {
					new SoundTask().execute((float)PreferenceHelper.getTransmitterCarrierFrequency());
					goButton.setText(getString(R.string.transmitter_abort_label));
				} else {
					soundTask.cancel(true);
					goButton.setText(getString(R.string.transmitter_start_label));
				}
			} 
	      });
	      
	      return rootView;
	  }
	  
	   private class SoundTask extends AsyncTask <Float, Void, Void> {
		   
		    AudioTrack track;
		    int minBufferSize=2048;
		    float period=(float) (2.0f*Math.PI);

		   @Override
		   protected void onPreExecute() {
			   if (soundTask == null) {
				   soundTask = this;
			   }
		   }
		    
		   @Override
	       protected Void doInBackground(Float... frequencies) {
			   if (soundTask == this) {
				   int bufferSize = 10240;
		           short[] buffer = new short[bufferSize];
		           int sampleRate = 44100;
		           
		           this.track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
		        		   AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
		        		   minBufferSize, AudioTrack.MODE_STREAM);
		           
		           float[] digiFreq = new float[frequencies.length];
			        for (int i = 0; i < frequencies.length; i++) {
			        		digiFreq[i] = period*frequencies[i]/(float)sampleRate;
			        }
		           int angle = 0;
		           float samples[] = new float[bufferSize];
	
		           this.track.play();
		           
		           while (!isCancelled()) {
		               for (int i = 0; i < samples.length; i++) {
		            	   samples[i] = 0;
		            	   for (int j = 0; j < frequencies.length;j++) {
		            		   samples[i] += (float) Math.sin(i*digiFreq[j]);   //the part that makes this a sine wave....
		            	   }
		                   buffer[i] = (short) (samples[i] * Short.MAX_VALUE);
		                   angle += 1;
		               }
		               this.track.write( buffer, 0, samples.length );  //write to the audio buffer.... and start all over again!
	
		           }

			   }
			   
			   soundTask = null;
			   return null;
	       }

	   }//end private class
}
