package com.enee408g.squealer.android;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorder {
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format
	private static final int RECORDER_SAMPLERATE = 8000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord recorder = null;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	private FFT fft = new FFT(BufferElements2Rec);
	private short[] audBuf = new short[BufferElements2Rec];
	private double[] inBuf = new double[BufferElements2Rec];
	private double[] outBuf = new double[BufferElements2Rec];
	private UpdateListener listener = null;
	private int maxFreq = 0;
	
	public interface UpdateListener {
		public void onUpdate(int dominantFrequency);
	}
	public void setUpdateListener(UpdateListener listener) {
		this.listener = listener;
	}
	

	public void startRecording() {

	    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
	            RECORDER_SAMPLERATE, RECORDER_CHANNELS,
	            RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

	    recorder.startRecording();
	    isRecording = true;
	    recordingThread = new Thread(new Runnable() {
	        public void run() {
	            processLiveData();
	        }
	    }, "AudioRecorder Thread");
	    recordingThread.start();
	}

	    //convert short to byte
	private byte[] short2byte(short[] sData) {
	    int shortArrsize = sData.length;
	    byte[] bytes = new byte[shortArrsize * 2];
	    for (int i = 0; i < shortArrsize; i++) {
	        bytes[i * 2] = (byte) (sData[i] & 0x00FF);
	        bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
	        sData[i] = 0;
	    }
	    return bytes;

	}

	  private void processData(short[] data) {
		  for (int i = 0; i < data.length; i++) {
			  inBuf[i] = (double)data[i] / 128.0f;
		  }
		  fft.fft(inBuf, outBuf);
		  int dom = 0;
		  for (int i = 0; i < outBuf.length; i++) {
			  double samp = outBuf[i];
			  double max = outBuf[dom];
			  if (samp > max || samp < -max) {
				  dom = i;
			  }
		  }
		  maxFreq = dom;
	  }
	
	private void processLiveData() {
	    // Write the output audio in byte
		
		// Write the output audio in byte

	    String filePath = "/sdcard/Squealer_test.csv";
	    short sData[] = new short[BufferElements2Rec];

	    FileOutputStream os = null;
	    try {
	        os = new FileOutputStream(filePath);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os);
	
	    while (isRecording) {
	        // gets the voice output from microphone to byte format

	        recorder.read(sData, 0, BufferElements2Rec);
	        System.out.println("Processing data: " + sData.toString());
			for (int i = 0; i < sData.length; i++) {
				inBuf[i] = (double)sData[i] / 128.0f;
			}
			fft.fft(inBuf, outBuf);
			
			// Create string
			String msg = "";
			for (int i = 0; i < outBuf.length; i++) {
				 msg += String.format("%f, ", outBuf[i]);
			}
			msg += "\n";
	        try {
	            // // writes the data to file from buffer
	            // // stores the voice buffer
	            outputStreamWriter.write(msg);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
        try {
			outputStreamWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopRecording() {
	    // stops the recording activity
	    if (null != recorder) {
	        isRecording = false;
	        recorder.stop();
	        recorder.release();
	        recorder = null;
	        recordingThread = null;
	        if (listener != null) listener.onUpdate(RECORDER_SAMPLERATE / BufferElements2Rec * maxFreq);
	    }
	}
}