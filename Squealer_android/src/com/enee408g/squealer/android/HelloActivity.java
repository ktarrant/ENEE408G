package com.enee408g.squealer.android;

import java.io.IOException;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.enee408g.squealer.android.ReceiverFragment;

public class HelloActivity extends FragmentActivity {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
	MainPagerAdapter mMainPagerAdapter;
    ViewPager mViewPager;
    AudioTrack track;
    int minBufferSize=2048;
    float period=(float) (2.0f*Math.PI);


    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
        
        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.//
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mMainPagerAdapter);
        
        // Set up PreferenceHelper
        //PreferenceHelper.setContext(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// Inflate the menu ; add the action bar
    	getMenuInflater().inflate(R.menu.main, menu);
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_settings:
    		Intent intent = new Intent(this, SettingsActivity.class);
    		startActivity(intent);
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    public class MainPagerAdapter extends FragmentStatePagerAdapter {

		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: return new ReceiverFragment();
			case 1: return new TransmitterFragment();
			default: return null;
			}
		}
		@Override
		public int getCount() {
			return 2;
		}
    }
   private class soundTask extends AsyncTask < Float, Void, Void>{

	   @Override
       protected Void doInBackground(Void... foo) {
           short[] buffer = new short[1024];
           this.track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
           float increment = period * frequency / 44100; // angular increment for each sample
           float angle = 0;
           float samples[] = new float[1024];

           this.track.play();

           while (true) {
               for (int i = 0; i < samples.length; i++) {
                   samples[i] = (float) Math.sin(angle);   //the part that makes this a sine wave....
                   buffer[i] = (short) (samples[i] * Short.MAX_VALUE);
                   angle += increment;
               }
               this.track.write( buffer, 0, samples.length );  //write to the audio buffer.... and start all over again!

           }           
       }
	   
	   
	   
	   
   }//end private class
}
