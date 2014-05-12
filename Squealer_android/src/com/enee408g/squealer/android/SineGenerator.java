package com.enee408g.squealer.android;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

public class SineGenerator {
	private static final String TAG = "SineGenerator";
	
	private PlaybackFinishedListener listener = null;
	public interface PlaybackFinishedListener {
		public void onPlaybackFinished(boolean cancelled);
	}
	public void setPlaybackFinishedListener(PlaybackFinishedListener listener) {
		this.listener = listener;
	}
	
	private PlayTask task = null;
	
   public void play(Byte[] msg, int sampleRate, int trackBufferSize, int[] frequencies, 
		   int pulseWidth, double dutyCycle) {
	   cancel();
	   task = new PlayTask(sampleRate, frequencies, pulseWidth, dutyCycle);
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
	
	// Byte mask for each bit-place
	private static final Byte[] MASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80, 
			0x00}; // This last one is to accomodate the final bit - although it is not ready yet
	
	private class PlayTask extends AsyncTask<Byte, Void, Void> {
		// Constants
		private static final int streamType 	= AudioManager.STREAM_MUSIC;
		private static final int channelConfig 	= AudioFormat.CHANNEL_CONFIGURATION_MONO;
		private static final int encoding		= AudioFormat.ENCODING_PCM_16BIT;
		private static final int mode			= AudioTrack.MODE_STREAM;
		// Parameters
		private int sampleRate;
		private int[] frequencies;
		private int pulseWidth;
		private double dutyCycle;
		private double[] digiFreq;
		// Objects
		private AudioTrack track;
		
		public PlayTask(int sampleRate, int[] frequencies, int pulseWidth, double dutyCycle) {
			this.sampleRate = sampleRate;
			this.frequencies = frequencies;
			this.pulseWidth = pulseWidth;
			this.dutyCycle = dutyCycle;
		}
		
		@Override protected void onPreExecute() {
		   // Create the AudioTrack object in Stream Mode
           track = new AudioTrack(streamType, sampleRate, channelConfig , encoding, 
        		    pulseWidth, mode);
           // Generate digital frequencies
		   double scale = 1.0f / (float)frequencies.length;
		   digiFreq = new double[frequencies.length];
		   double twopie = (double) (2.0 * Math.PI);
		   for (int i = 0; i < frequencies.length; i++) {
			   digiFreq[i] = twopie * (double)frequencies[i] / (double)sampleRate;
		   }
		}

		@Override
		protected Void doInBackground(Byte... msg) {
			int cur = 0;
			int angle = 0;
			short[] buffer = new short[pulseWidth];
			int dutyInd = (int)(dutyCycle * (double)pulseWidth);
			double scale = 1.0 / (double)frequencies.length;
			track.play();
			while (!isCancelled() && cur < msg.length) {
				// Get message byte
				byte m = msg[cur];
				// Prepare a buffer
				for (int i = 0; i < pulseWidth; i++) {
				   buffer[i] = 0;
				   if (i < dutyInd) {
					   for (int b = 0; b < digiFreq.length; b++) {
						   if ((m & MASK[b]) != 0) {
							   buffer[i] += (short)(Short.MAX_VALUE * 
									   (scale * Math.sin(angle * digiFreq[b])));
						   }
					   }
				   }
				   // Keep angle seperate from i for consistency across buffers
				   angle++;
				}
				cur++;
				// Write to playback buffer
			    track.write( buffer, 0, buffer.length );
		   }
		   return null;
	   }
		
		@Override protected void onCancelled() {
	       // Stop playback immediately and flush the buffer
	       track.pause();
	       track.flush();
	       track.release();
	       if (listener != null)  listener.onPlaybackFinished(isCancelled());
	       task = null;
		}
		
	   @Override
	   protected void onPostExecute(Void v) {
		   track.stop();
		   track.release();
		   if (listener != null)  listener.onPlaybackFinished(isCancelled());
		   task = null;
	   }
	}
/*	
	private SoundTask task = null;
	private AudioTrack track;
	
	private int[] frequencies;
	private int fartFrequency;
	private int sampleRate;
	private int pulseSampleWidth; 
	private int fartSampleWidth;
	private int pulsesPerBuffer;
    private int trackBufferSize;
    private float dutyCycle;
	private float scale = 1.0f;
	private float[] digiFreq = null;
	private float digiFart;
	private PlaybackFinishedListener listener = null;
	

	
	public interface PlaybackFinishedListener {
		public void onPlaybackFinished(boolean cancelled);
	}
	
	public void setPlaybackFinishedListener(PlaybackFinishedListener listener) {
		this.listener = listener;
	}
   
	public void updatePrefs(Context context) {
		sampleRate 			= PreferenceHelper.getTransmitterSampleRate(context);
		pulseSampleWidth 	= PreferenceHelper.getPulseSampleWidth(context);
		fartSampleWidth 	= PreferenceHelper.getFartSampleWidth(context);
		pulsesPerBuffer 	= PreferenceHelper.getPulsesPerBuffer(context);
	    trackBufferSize 	= PreferenceHelper.getTrackBufferSize(context);
	    dutyCycle			= PreferenceHelper.getDutyCycle(context);
	}
	
	   public SineGenerator(Context context, int fartFreq, int[] frequencies) {
		   updatePrefs(context);
		   setFrequencies(frequencies);
		   setFartFrequency(fartFreq);
		   // Create the AudioTrack object in Stream Mode
           track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, 
        		   AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, 
        		   trackBufferSize, AudioTrack.MODE_STREAM);
	   }
	   
	   public void setFartFrequency(int fartFreq) {
		   this.fartFrequency = fartFreq;
		   this.digiFart = (float)(2.0f * Math.PI * fartFreq) / (float)sampleRate;
	   }
	   
	   public void setFrequencies(int[] j) {
		   // Sets the transmission frequencies and sets them up for processing
		   this.frequencies = j;
		   scale = 1.0f / (float)j.length;
		   digiFreq = new float[j.length];
		   float twopie = (float) (2.0f * Math.PI);
		   
		   for (int i = 0; i < j.length; i++) {
			   digiFreq[i] = twopie * j[i] / (float)sampleRate;
		   }
	   }
	   
	   public void play(byte... msg) {
		   if (task != null) {
			   task.cancel(true);
		   }
		   task = new SoundTask();
		   task.execute(new ByteMsg(msg));
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

	   private class SoundTask extends AsyncTask <ByteMsg, Void, Void> {

		   @Override
		   protected void onPreExecute() {
			   if (task == null) {
				   task = this;
			   }
		   }
		   
		   private short[] createFartBuffer(int angle, short[] buffer) {
			   for (int i = 0; i < buffer.length; i++) {
				   if (i < buffer.length/2) {
					   buffer[i] = (short)(Short.MAX_VALUE * (scale * Math.sin(angle * digiFart)));
				   } else {
					   buffer[i] = 0;
				   }
			   }
			   return buffer;
		   }
		    
		   @Override
	       protected Void doInBackground(ByteMsg... msg) {
			   if (task == this) {
		           int cur = 0;
		           int angle = 0;
		           // Create reusable buffer for transmission
		           short[] buffer = new short[pulsesPerBuffer * pulseSampleWidth];
		           // Createa a buffer for the start/stop fart tone
		           short[] fartBuf = new short[fartSampleWidth];
		           fartBuf = createFartBuffer(angle, buffer);
		           
		           // start playing the track
		           track.play();
		           // Send the fart buffer
		           if (!isCancelled())
		           	   track.write(fartBuf, 0, fartBuf.length);
		           // Create a loop of buffer preparation and track playing
		           while (!isCancelled() && cur < msg[0].getLength()) {
		        	   // Index in current buffer
		        	   for(int bufCur = 0; bufCur < buffer.length; bufCur += pulseSampleWidth) {
		        		   if (cur < msg[0].getLength()) {
				        	   // Get message byte
				        	   byte m = msg[0].getByte(cur);
				        	   // Prepare a buffer
				        	   for (int i = bufCur; i < bufCur + pulseSampleWidth; i++) {
				        		   float pcnt = ((float)(i - bufCur))/((float)pulseSampleWidth);
				        		   buffer[i] = 0;
				        		   if (pcnt < dutyCycle) {
				        			   for (int b = 0; b < digiFreq.length; b++) {
				        				   if ((m & MASK[b]) != 0) {
				        					   buffer[i] += (short)(Short.MAX_VALUE * 
				        							   (scale * Math.sin(angle * digiFreq[b])));
				        				   }
				        			   }
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
		           
		           // Send a finishing fart
		           if (!isCancelled())
		           	   track.write(fartBuf, 0, fartBuf.length);
		           
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
	   
	   private static class ByteMsg {
		   private byte[] msg = null;
		   public ByteMsg(byte[] msg) {
			   this.msg = msg;
		   }
		   
		   public byte getByte(int position) {
			   return msg[position];
		   }
		   
		   public void setByte(int position, byte b) {
			   msg[position] = b;
		   }
		   
		   public int getLength() {
			   return msg.length;
		   }
	   }

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return null;
	}*/
}
