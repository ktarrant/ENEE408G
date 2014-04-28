package com.enee408g.squealer.android;

import android.content.Context;

public class DecodeRecording {
	private int[] frequencies;
	
	public static String decodeMessage(Context context, String filename, int frequency, int n) {
		FFT fft = new FFT(n);
		//int overlap = n/4;
		
		return "";
	}
}
