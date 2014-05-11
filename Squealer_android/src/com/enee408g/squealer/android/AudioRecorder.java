package com.enee408g.squealer.android;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

public class AudioRecorder {

	private int BytesPerElement = 2; // 2 bytes in 16bit format
	private int bufferSize;
	private int[] frequencies;
	private int fartFrequency;
	private int sampleRate;
	private int pulseSampleWidth; 
	private int fartSampleWidth;
	private int pulsesPerBuffer;
    private int trackBufferSize;
	private float scale = 1.0f;
	private float dbSens;
	private float[] digiFreq = null;
	private float digiFart;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord recorder = null;
	private FFT fft;
	private int quickLen = 512;
	private FFT miniFFT = new FFT(quickLen);
	private BufferListener bufListener = null;
	private ValueListener valueListener = null;
	private FartListener fartListener = null;
	private StartTask srtTask = null;
	private ListenTask recTask = null;
	private static final String TAG = "AudioRecorder";
	
	public interface BufferListener {
		public void onBufferUpdate(short[] buf);
	}
	public void setBufferListener(BufferListener listener) {
		this.bufListener = listener;
	}
	
	public interface ValueListener {
		public void onValueUpdate(byte[] msg);
	}
	public void setValueListener(ValueListener listener) {
		this.valueListener = listener;
	}
	
	public interface FartListener {
		public void onFartUpdate();
	}
	public void setFartListener(FartListener listener) {
		this.fartListener = listener;
	}
	
	public void updatePrefs(Context context) {
		sampleRate 			= PreferenceHelper.getSampleRate(context);
		pulseSampleWidth 	= PreferenceHelper.getPulseSampleWidth(context);
		fartSampleWidth 	= PreferenceHelper.getFartSampleWidth(context);
		pulsesPerBuffer 	= PreferenceHelper.getPulsesPerBuffer(context);
	    trackBufferSize 	= PreferenceHelper.getTrackBufferSize(context);
		dbSens				= PreferenceHelper.getDbSensitivity(context);
	    bufferSize = pulseSampleWidth*pulsesPerBuffer;
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
	
	public AudioRecorder(Context context) {
		updatePrefs(context);
		fft = new FFT(bufferSize);
		setFrequencies(PreferenceHelper.getAllBitFrequencies(context));
		setFartFrequency(PreferenceHelper.getFartFrequency(context));
	}
	
	public void startRecording() {
		if (recTask == null) {
			recTask = new ListenTask();
			recTask.execute();
		}
	}
	
	public void stopRecording() {
		if (recTask != null) {
			recTask.cancel(true);
			recTask = null;
		}
	}
	
	public boolean isRecording() {
		return recTask != null;
	}
	
	Byte[] MASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80, 
			0x00}; // This last one is to accomodate the final bit - although it is not ready yet
	
	public List<double[]> getBands(double[] fft_result, double sens) {
		double minVal = 0.0;
		double maxVal = 100000.0;
		double binWidth = sampleRate / (double)fft_result.length;
		boolean inBand = false;
		double minFreq = maxVal;
		double maxFreq = minVal;
		List<double[]> bands = new ArrayList<double[]>();
		  for (int i = 0; i < fft_result.length/2; i++) {
			double freq = (double)i * binWidth;
			if (freq < sampleRate/2) {//(freq > minFrequency && freq < (sampleRate - minFrequency)) {
				if (fft_result[i] > sens) {
					if (freq < minFreq) minFreq = freq;
					if (freq > maxFreq) maxFreq = freq;
					if (!inBand) inBand = true;
				} else {
					if (inBand) {
						inBand = false;
						bands.add(new double[] {minFreq, maxFreq});
						minFreq = maxVal;
						maxFreq = minVal;
					}
				}
		  	}
		  }
		 return bands;
	}
	
	  public byte getValue(List<double[]> bands) {
		 byte rval = 0;
		 for (double[] band : bands) {
			 for (int i = 0; i < frequencies.length; i++) {
				 if (frequencies[i] > band[0] && frequencies[i] < band[1]) {
					 rval |= MASK[i];
				 }
			 }
		 }
		 return rval;
		  //return (double[][])bands.toArray(new double[bands.size()][2]);
	  }
	  
	public boolean getFart(List<double[]> bands) {
		for (double[] band : bands) {
			Log.i(TAG, String.format("%f < %f < %f", band[0], (float)fartFrequency, band[1]));
			if (fartFrequency > band[0] && fartFrequency < band[1]) {
				return true;
			}
		}
		return false;
	}
	
	double[] miniInBuf = new double[quickLen];
	double[] miniOutBuf = new double[quickLen];//
	//
	
	public boolean[] quickFartCheck(short[] buf) {
		boolean[] rval = new boolean[buf.length / quickLen];
		int minVal = (int)(((float)(fartFrequency-250)/(float)sampleRate)*(float)quickLen);
	    int maxVal = (int)(((float)(fartFrequency+250)/(float)sampleRate)*(float)quickLen);
		double binWidth = sampleRate/quickLen;
		for (int i = 0; i < rval.length; i++) { // i = chunk #
			rval[i] = false;
			for (int j = 0; j < quickLen; j++) { // index in chunk
				miniInBuf[j] = (double)buf[j+i*quickLen] / 128.0f;
				miniOutBuf[j] = 0;
			}
			miniFFT.fft(miniInBuf, miniOutBuf);
			for (int j = 0; j < quickLen; j++) {
				miniInBuf[j] = 10*Math.log10(miniInBuf[j]*miniInBuf[j]+miniOutBuf[j]*miniOutBuf[j]);
			}
//			rval[i] = getFart(getBands(miniInBuf, dbSens/2));
			//String msg = String.valueOf(minVal) + " < [";
			for (int j = minVal; j < maxVal; j++) {
				//msg += String.valueOf(miniInBuf[j]) + ", ";
				if (miniInBuf[j] > dbSens/2) {
					rval[i] = true;
					break;
				}
			}
		    //msg += "] < " + String.valueOf(maxVal);
		   /// Log.i(TAG, msg);
			//Log.i(TAG, String.valueOf(i) + ": " + String.valueOf(rval[i]));
		}
		return rval;
	}

	public void processBuffer(short[] buf) {
		new runFFT().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buf);
	}
	
	//public void checkForFart(short[] buf) {
	//	new runFFT(true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, buf);
	//}
	
	class runFFT extends AsyncTask<short[], Void, List<Byte>> {
		
		private double[] inBuf;
		private double[] outBuf;
		
		public runFFT() {
			this.inBuf = new double[bufferSize];
			this.outBuf = new double[bufferSize];
		}
		
		@Override
		protected List<Byte> doInBackground(short[]... data) {
			List<Byte> msg = new ArrayList<Byte>();
			for (short[] d : data) {
				if (d != null) {
					//System.out.println("Processing data: " + d.toString());
					// Change to double
					for (int j = 0; j < d.length; j++) {
						inBuf[j] = (double)d[j] / 128.0f;
						outBuf[j] = 0.0f;
					}
					//long t1 = System.nanoTime();
					fft.fft(inBuf, outBuf);
					//System.out.println(String.format("FFT Time: %f", (float)(System.nanoTime()-t1)/1000000000.0f));
					// Change to magnitude
					double[] fft_result = new double[d.length];
					for (int j = 0; j < d.length; j++) {
						fft_result[j] = 10*Math.log10(inBuf[j]*inBuf[j]+outBuf[j]*outBuf[j]);
					}
					List<double[]> bands = getBands(fft_result, dbSens);
					//boolean fartFound = getFart(bands);
					byte val = getValue(bands);
					if (val != 0) msg.add(val);
				}
			}
			return msg;
		}
		
		@Override
		protected void onPostExecute(List<Byte> msg) {
			if (valueListener != null) {
				// Convert to native byte
				byte[] rval = new byte[msg.size()];
				for (int i = 0; i < rval.length; i++) {
					rval[i] = msg.get(i);
				}
				valueListener.onValueUpdate(rval);
			}
		}
	}
	
	
	
	class StartTask extends AsyncTask<Void, Boolean, Void> {
		
		short[] sData;
		double[] inBuf;
		double[] outBuf;
		
		@Override
		protected void onPreExecute() {
		    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
		            sampleRate, RECORDER_CHANNELS,
		            RECORDER_AUDIO_ENCODING, bufferSize * BytesPerElement);

		    recorder.startRecording();
		    srtTask = this;
		    sData = new short[quickLen];
		    inBuf = new double[quickLen];
		    outBuf = new double[quickLen];
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			while (!isCancelled()) {
				int readCount = recorder.read(sData, 0, bufferSize);
				for (int i = 0; i < quickLen; i++) {
					inBuf[i] = (double)sData[i] / 128.0f;
					outBuf[i] = 0.0f;
					//long t1 = System.nanoTime();
				}
				fft.fft(inBuf, outBuf);
				for (int i = 0; i < quickLen; i++) {
					inBuf[i] = 10*Math.log10(inBuf[i]*inBuf[i]+outBuf[i]*outBuf[i]);
				}
			}
			return null;
		}
		
		@Override
		protected void onCancelled() {
			destroy();
		}
	}
	
	class ListenTask extends AsyncTask<Void, short[], Void> {
		short[] sData;
		boolean msgStarted = false;
		boolean aligning = false;
		
		@Override
		protected void onPreExecute() {
		    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
		            sampleRate, RECORDER_CHANNELS,
		            RECORDER_AUDIO_ENCODING, bufferSize * BytesPerElement);

		    recorder.startRecording();
		    recTask = this;
		    sData = new short[bufferSize];
		}
		
		@Override protected void onProgressUpdate(short[]... msg) {
			for (short[] m : msg) {
				if (bufListener != null) bufListener.onBufferUpdate(m);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			while (!isCancelled()) {
				int readCount = recorder.read(sData, 0, quickLen);
				//System.out.println(String.format("Recording time: %f sec", (float)readCount / (float)sampleRate));
				publishProgress(sData);
			}
			Log.i(TAG, "Cancelled!");
			return null;
		}
		
		@Override
		protected void onCancelled() {
			destroy();
		}
		
		@Override
		protected void onPostExecute(Void v) {
			destroy();
		}
	
	}
	
	void destroy() {
	    // stops the recording activity
	    if (null != recorder) {
	        recTask = null;
	        recorder.stop();
	        recorder.release();
	        recorder = null;
	    }
	}
}