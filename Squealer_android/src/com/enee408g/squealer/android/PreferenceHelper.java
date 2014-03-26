package com.enee408g.squealer.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {
	private static Context mContext;
	private static String RECEIVER_CARRIER;
	private static String RECEIVER_MODULATOR;
	private static String RECEIVER_MESSAGE;
	private static String TRANSMITTER_CARRIER;
	private static String TRANSMITTER_MODULATOR;
	private static String TRANSMITTER_MESSAGE;
	
	// Default setting values
	private static final int defaultCarrier = 21000;
	private static final int defaultModulator = 22000;
	private static final String defaultMessage = "Hello world!";
	
	public static void setContext(Context context) {
		mContext = context;
		// Init tag names from context
		RECEIVER_CARRIER = mContext.getString(R.string.pref_receiver_carrier);
		RECEIVER_MODULATOR = mContext.getString(R.string.pref_receiver_modulator);
		RECEIVER_MESSAGE = mContext.getString(R.string.pref_receiver_message);
		TRANSMITTER_CARRIER = mContext.getString(R.string.pref_transmitter_carrier);
		TRANSMITTER_MODULATOR = mContext.getString(R.string.pref_transmitter_modulator);
		TRANSMITTER_MESSAGE = mContext.getString(R.string.pref_transmitter_message);
		// Load values
		initPreferences();
	}
	
	public static void initPreferences() {
		SharedPreferences prefs = PreferenceManager.
				getDefaultSharedPreferences(mContext);
		
		// Write to both Receiver and Transmitter
		SharedPreferences.Editor e = prefs.edit();
		e.putInt(RECEIVER_CARRIER, 
				prefs.getInt(RECEIVER_CARRIER, defaultCarrier));
		e.putInt(RECEIVER_MODULATOR, 
				prefs.getInt(RECEIVER_MODULATOR, defaultModulator));
		e.putString(RECEIVER_MESSAGE, 
				prefs.getString(RECEIVER_MESSAGE, defaultMessage ));
		e.putInt(TRANSMITTER_CARRIER, 
				prefs.getInt(TRANSMITTER_CARRIER, defaultCarrier));
		e.putInt(TRANSMITTER_MODULATOR, 
				prefs.getInt(TRANSMITTER_MODULATOR, defaultModulator));
		e.putString(TRANSMITTER_MESSAGE, 
				prefs.getString(TRANSMITTER_MESSAGE, defaultMessage ));
		e.commit();
	}
	
	public static int getReceiverCarrierFrequency() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getInt(RECEIVER_CARRIER, defaultCarrier);
	}
	public static int getReceiverModulatorFrequency() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getInt(RECEIVER_MODULATOR, defaultModulator);
	}
	public static String getReceiverMessage() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(RECEIVER_MESSAGE, defaultMessage);
	}
	public static int getTransmitterCarrierFrequency() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getInt(TRANSMITTER_CARRIER, defaultCarrier);
	}
	public static int getTransmitterModulatorFrequency() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getInt(TRANSMITTER_MODULATOR, defaultModulator);
	}
	public static String getTransmitterMessage() {
		return PreferenceManager.getDefaultSharedPreferences(mContext)
				.getString(TRANSMITTER_MESSAGE, defaultMessage);
	}
}
