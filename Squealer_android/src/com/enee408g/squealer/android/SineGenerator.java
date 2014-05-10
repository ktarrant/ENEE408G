package com.enee408g.squealer.android;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

public class SineGenerator {
	
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
	
	// Byte mask for each bit-place
	Byte[] MASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80, 
			0x00}; // This last one is to accomodate the final bit - although it is not ready yet
	
	public interface PlaybackFinishedListener {
		public void onPlaybackFinished(boolean cancelled);
	}
	
	public void setPlaybackFinishedListener(PlaybackFinishedListener listener) {
		this.listener = listener;
	}
   
	public void updatePrefs(Context context) {
		sampleRate 			= PreferenceHelper.getSampleRate(context);
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
				   buffer[i] = (short)(Short.MAX_VALUE * (scale * Math.sin(angle * digiFart)));
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
		           //if (!isCancelled())
		           //	   track.write(fartBuf, 0, fartBuf.length);
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
}
