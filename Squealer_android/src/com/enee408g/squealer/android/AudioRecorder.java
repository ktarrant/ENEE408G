package com.enee408g.squealer.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

public class AudioRecorder {

	private static final String TAG = "AudioRecorder";
	
	// PowerListener - updates the caller on the power of given frequencies
	private PowerListener mPowerListener = null;
	public interface PowerListener {
		public void onBufferUpdate(double[] power);
	}
	public void setPowerListener(PowerListener powerListener) {
		this.mPowerListener = powerListener;
	}
	
	// ValueListener - updates the caller on the guessed value of the window
	private ValueListener mValueListener = null;
	public interface ValueListener {
		public void onValueUpdate(byte value);
	}
	public void setValueListener(ValueListener valueListener) {
		this.mValueListener = valueListener;
	}
	
	// Extract Byte - computes the value of a sample from the power levels
	private static final Byte[] MASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80, 
		0x00}; // This last one is to accomodate the final bit - although it is not ready yet
	private static byte extractByte(double[] power, double dbSensitivity) {
	  byte rval = 0;
	  for (int i = 0; i < power.length; i++) {
		  if (power[i] > dbSensitivity) {
			  rval |= MASK[i];
		  }
	  }
	  return rval;
	}
	
	// DetectTask which tracks the given frequencies' power
	private DetectTask mDetectTask = null;
	public void startDetection(int[] frequencies, int bufferSize, int sampleRate, 
			int fftSize, int overlap, double dbSensitivity) {
		stopDetection();
		this.mDetectTask = new DetectTask(bufferSize, sampleRate, fftSize, overlap, dbSensitivity);
		this.mDetectTask.execute(frequencies);
	}
	public void stopDetection() {
		if (this.mDetectTask != null) {
			this.mDetectTask.cancel(true);
			this.mDetectTask = null;
		}
	}
	public boolean isDetecting() {
		return (this.mDetectTask != null);
	}
	
	// DetectTask - monitors the microphone for given frequencies.
	class DetectTask extends AsyncTask<int[], double[], Void> {
		// Constants
		private static final int RECORDER_CHANNELS 			= AudioFormat.CHANNEL_IN_MONO;
		private static final int RECORDER_AUDIO_ENCODING 	= AudioFormat.ENCODING_PCM_16BIT;
		private static final int BYTES_PER_ELEMENT 			= 2; // 2 bytes in 16bit format
		// Parameters of the recording task
		private int bufferSize;
		private int sampleRate;
		private int fftSize;
		private int overlap;
		private double dbSensitivity;
		// Buffers
		private short[]  micBuf;
		private double[] realBuf;
		private double[] imagBuf;
		private double[] windBuf;
		// Objects
		private AudioRecord recorder;
		private FFT fft;
		// Round up to next higher power of 2 (return x if it's already a power of 2).
		int pow2roundup (int x) {
		    if (x < 0) return 0;
		    --x;
		    x |= x >> 1;
		    x |= x >> 2;
		    x |= x >> 4;
		    x |= x >> 8;
		    x |= x >> 16;
		    return x+1;
		}
		private double[] genWindow(int bufferSize) {
			// Creates a hamming window
			double[] rval = new double[bufferSize];
			for (int i = 0; i < bufferSize; i++) {
				rval[i] = 0.54 - 0.46*Math.cos(2.0f*Math.PI*((double)i)/((double)bufferSize));
			}
			return rval;
		}
		// Save and generate important parameters at creation
		public DetectTask(int bufferSize, int sampleRate, int fftSize, int overlap, double dbSensitivity) {
			this.bufferSize = bufferSize;
			this.sampleRate = sampleRate;
			this.fftSize = pow2roundup(fftSize);
			this.fft = new FFT(this.fftSize);
			this.windBuf = genWindow(this.fftSize);
			this.overlap = overlap;
			this.dbSensitivity = dbSensitivity;
		}
		// When the task is started, we need to prepare for streaming
		@Override protected void onPreExecute() {
			// Initialize buffers//
		    this.micBuf  = new short [this.bufferSize];
		    this.realBuf = new double[this.fftSize];
		    this.imagBuf = new double[this.fftSize];
		    // Start the recording
		    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
		            this.sampleRate, RECORDER_CHANNELS,
		            RECORDER_AUDIO_ENCODING, this.bufferSize * BYTES_PER_ELEMENT);
		    recorder.startRecording();
		}
		// Finds the mode of an input list
		private byte getMode(byte[] list) {
			byte mode = 0;
			int modeFreq = 0;
			for (int i = 0; i < list.length; i++) {
				int curFreq = 1;
				for (int j = i+1; j < list.length; j++) {
					if (list[i] == list[j]) curFreq++;
				}
				if (curFreq > modeFreq) {
					mode = list[i];
					modeFreq = curFreq;
				}
			}
			return mode;
		}
		// Performs a STFT on a given window and publishes the results
		private void runSTFT(short[] buf, int count, int[] frequencies) {
			int cur = 0;
			int inc = this.fftSize - this.overlap;
			double[][] powerBuf = new double[count / inc][frequencies.length];
			while (cur+this.fftSize <= count) {
				double[] tempBuf  = new double[frequencies.length];
				for (int i = 0; i < this.fftSize; i++) {
					// Applies the Hamming window
					realBuf[i] = (double)this.micBuf[cur+i] / 128.0f * windBuf[i];
					imagBuf[i] = 0.0f; // need to clear y or it breaks
				}
				//Log.i(TAG, "Running fft...");
				this.fft.fft(realBuf, imagBuf);
				for (int i = 0; i < frequencies.length; i++) {
					float index = (float)frequencies[i]/(float)sampleRate*(float)this.fftSize;
					int iFlr = (int)Math.floor(index);
					tempBuf[i]  = (index-iFlr)/((float)powerBuf.length)*
							10*Math.log10(realBuf[iFlr]*realBuf[iFlr] + imagBuf[iFlr]*imagBuf[iFlr]);
					tempBuf[i] += (iFlr+1.0f-index)/((float)powerBuf.length)*
							10*Math.log10(realBuf[iFlr+1]*realBuf[iFlr+1]+ imagBuf[iFlr+1]*imagBuf[iFlr+1]);
				}
				powerBuf[cur / inc] = tempBuf;
				cur += inc;
			}
			publishProgress(powerBuf);
		}
		// The streaming function
		@Override protected Void doInBackground(int[]... frequencies) {
			while (!isCancelled()) {
				int readCount = recorder.read(this.micBuf, 0, this.bufferSize);
				runSTFT(this.micBuf, readCount, frequencies[0]);
			}
			return null;
		}
		// Receives updates containing the power of desired frequencies
		@Override protected void onProgressUpdate(double[]... power) {	
			if (power.length == 0) return;
			double[] pavg = new double[power[0].length];
			byte[] valBuf = new byte[power.length];
			for (int i = 0; i < power.length; i++) {
				if (mPowerListener != null) mPowerListener.onBufferUpdate(power[i]);
				valBuf[i] = extractByte(power[i], this.dbSensitivity);
			}
			if (mValueListener != null) {
				byte val = getMode(valBuf);
				//Log.i(TAG, String.format("%X", val));
				mValueListener.onValueUpdate(val);
			}
		}
		@Override protected void onCancelled() {
			destroy();
		}
		@Override protected void onPostExecute(Void vd) {
			destroy();
		}
		// destroy recorder instance
		void destroy() {
		    // stops the recording activity
		    if (null != recorder) {
		        recorder.stop();
		        recorder.release();
		        recorder = null;
		    }
		}
	}
}