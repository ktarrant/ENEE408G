package com.enee408g.squealer.android;

import java.io.IOException;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

public class ReceiverFragment extends Fragment {
	
	private final static String TAG = "ReceiverFragment";
	
	boolean isRecording = false;//false means not listening
	
	private static String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
            Log.i(TAG, "RECEIVED MESSAGE: " + DecodeRecording.decodeMessage(getActivity(), mFileName, 800, 8));
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
	
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_receiver, container, false);
	      
          mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
          mFileName += "/AudioRecorder.3gp";
	      
	      final Button startButton = (Button) rootView.findViewById(R.id.receiver_button_record);
	      final Button playButton = (Button) rootView.findViewById(R.id.receiver_button_play);
	      
	      startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isRecording = !isRecording;
                onRecord(isRecording);
                if (isRecording) {
                    startButton.setText("Stop recording");
                } else {
                    startButton.setText("Start recording");
                }
			}
	    	  
	      });
	      
	      playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			  isRecording = !isRecording;
              onPlay(isRecording);
              if (isRecording) {
                  playButton.setText("Stop playing");
              } else {
                  playButton.setText("Start playing");
              }
			}
	      });
	      
	      Log.i("ENEE408G", "ReceiverFragment onCreateView");
	      
	      
	      return rootView;
	      
	  }
	      
    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
	      
	  
}
