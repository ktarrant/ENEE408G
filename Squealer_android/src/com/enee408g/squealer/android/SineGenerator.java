package com.enee408g.squealer.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

public class SineGenerator {
	
	private SoundTask task = null;
	private Float[] frequencies;
	private AudioTrack track;
	
	private int pulseSampleWidth = 2048;
	private int pulsesPerBuffer = 2;
    private int trackBufferSize = 2048;
	private int sampleRate = 44100;
	private float scale = 1.0f;
	private float[] digiFreq = null;
	private PlaybackFinishedListener listener = null;
	
	// Byte mask for each bit-place
	Byte[] MASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80};
	
	public interface PlaybackFinishedListener {
		public void onPlaybackFinished(boolean cancelled);
	}
	
	public void setPlaybackFinishedListener(PlaybackFinishedListener listener) {
		this.listener = listener;
	}
   
	   public SineGenerator(Float... frequencies) {
		   setFrequencies(frequencies);
		   // Create the AudioTrack object in Stream Mode
           track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
        		   AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
        		   trackBufferSize, AudioTrack.MODE_STREAM);
	   }
	   
	   public void setFrequencies(Float... frequencies) {
		   // Sets the transmission frequencies and sets them up for processing
		   this.frequencies = frequencies;
		   scale = 1.0f / (float)frequencies.length;
		   digiFreq = new float[frequencies.length];
		   float twopie = (float) (2.0f * Math.PI);
		   
		   for (int i = 0; i < frequencies.length; i++) {
			   digiFreq[i] = twopie * frequencies[i] / (float)sampleRate;
		   }
	   }
	   
	   public void play(Byte... msg) {
		   if (task != null) {
			   task.cancel(true);
		   }
		   task = new SoundTask();
		   task.execute(msg);
	   }
	   
	   public void cancel() {
		   if (task != null) {
			   task.cancel(true);
		   }
		   task = null;
	   }
	   
	   public boolean isPlaying() {
		   return (task != null);
	   }

	   private class SoundTask extends AsyncTask <Byte, Void, Void> {

		   @Override
		   protected void onPreExecute() {
			   if (task == null) {
				   task = this;
			   }
		   }
		    
		   @Override
	       protected Void doInBackground(Byte... msg) {
			   if (task == this) {
		           short[] buffer = new short[pulsesPerBuffer * pulseSampleWidth];
		           int cur = 0;
		           int angle = 0;
		           
		           // start playing the track
		           track.play();
		           // Create a loop of buffer preparation and track playing
		           while (!isCancelled() && cur < msg.length) {
		        	   // Index in current buffer
		        	   for(int bufCur = 0; bufCur < buffer.length; bufCur += pulseSampleWidth) {
		        		   if (cur < msg.length) {
				        	   // Get message byte
				        	   byte m = msg[cur];
				        	   // Prepare a buffer
				        	   for (int i = bufCur; i < bufCur + pulseSampleWidth; i++) {
				        		   buffer[i] = 0;
			        			   for (int b = 0; b < digiFreq.length; b++) {
			        				   if ((m & MASK[b]) != 0)
			        					   buffer[i] += (short)(Short.MAX_VALUE * (scale * Math.sin(angle * digiFreq[b])));
			        			   }
				        		   // Keep angle seperate from i for consistency across buffers
				        		   angle++;
				        	   }
				        	   cur++;
		        		   } else break;
		        	   }
		        	   
		        	   // Write to playback buffer
		               track.write( buffer, 0, buffer.length );
				   }
		           
		           if (isCancelled()) {
			           // Stop playback immediately and flush the buffer
			           track.pause();
			           track.flush();
		           } else {
		        	   // Let the buffer finish
		        	   track.stop();
		           }
			   }
			   
			   task = null;
			   return null;
	       }
		   
		   @Override
		   protected void onPostExecute(Void v) {
			   if (listener != null) {
				   listener.onPlaybackFinished(isCancelled());
			   }
		   }

	   }//end private class
}
