package com.enee408g.squealer.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {
	
   public static float getDbSensitivity(Context context) {
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Float.parseFloat(prefs.getString("db_sens", "40.0"));
   }
   
   public static float getDutyCycle(Context context) {
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Float.parseFloat(prefs.getString("dutyCucle", "0.75"));
   }
   
   public static int getSampleRate(Context context) {
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Integer.parseInt(prefs.getString("sampleRate", "44100"));
   }
   
   public static int getPulseSampleWidth(Context context) {
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Integer.parseInt(prefs.getString("pulseSampleWidth", "2048"));
   }
   
   public static int getFartSampleWidth(Context context) {
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Integer.parseInt(prefs.getString("fartSampleWidth", "8096"));
   }
   
   public static int getPulsesPerBuffer(Context context) {
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Integer.parseInt(prefs.getString("pulsesPerBuffer", "2"));
   }
   
   public static int getTrackBufferSize(Context context) {
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Integer.parseInt(prefs.getString("trackBufferSize", "2048"));
   }
	
   public static int getBitFrequency(Context context, int position) {
	   String pref_key = String.format("freq_b%d", position);
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Integer.parseInt(prefs.getString(pref_key, "0"));
   }
   
   public static int[] getAllBitFrequencies(Context context) {
	   int[] rval = new int[9];
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   for (int i = 0; i < rval.length; i++) {
		   String pref_key = String.format("freq_b%d", i);
		   rval[i] = Integer.parseInt(prefs.getString(pref_key, "0"));
	   }
	   return rval;
   }
   
   public static String[] getAllFrequencyKeys() {
	   String[] rval = new String[9];
	   for (int i = 0; i < rval.length; i++) {
		   rval[i] = String.format("freq_b%d", i);
	   }
	   return rval;
   }
   
   public static int getFartFrequency(Context context) {
	   String pref_key = "freq_fart";
	   SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	   return Integer.parseInt(prefs.getString(pref_key, "0"));
   }
   
   public static void setFrequencyDefaults(Context context) {
	   setFrequencyRange(context, 16000, 500);
   }
   
   public static void setFrequencyRange(Context context, int start, int interval) {
	   String[] keys = getAllFrequencyKeys();
	   SharedPreferences.Editor e = 
			   PreferenceManager.getDefaultSharedPreferences(context).edit();
	   for (int i = 0; i < keys.length; i++) {
		   e.putString(keys[i], String.format("%d", start + interval*i));
	   }
	   e.commit();
   }
}
