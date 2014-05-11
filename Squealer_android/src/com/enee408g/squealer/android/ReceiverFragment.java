package com.enee408g.squealer.android;


import com.enee408g.squealer.android.AudioRecorder.BufferListener;
import com.enee408g.squealer.android.AudioRecorder.ValueListener;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class ReceiverFragment extends Fragment {
	
	private final static String TAG = "ReceiverFragment";
	private AudioRecorder recorder = null;
	Byte[] MASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80, 
			0x00}; // This last one is to accomodate the final bit - although it is not ready yet
	String curMsg = "";
	
	@Override
	public void onPause() {
		super.onPause();
		if (recorder != null) recorder.stopRecording();
	}
	
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_receiver, container, false);
	      
	      final Button startButton = (Button) rootView.findViewById(R.id.receiver_button_record);
	      final EditText numberDisplay = (EditText) rootView.findViewById(R.id.receiver_message);
	      
	      recorder = new AudioRecorder(getActivity());
	      recorder.setBufferListener(new BufferListener() {
			@Override
			public void onBufferUpdate(short[] msg) {
				recorder.processBuffer(msg);
			}
	      });
	      recorder.setValueListener(new ValueListener() {
			@Override
			public void onValueUpdate(byte[] msg) {
				curMsg += new String(msg);//String.format("%8s", Integer.toBinaryString(m)).replace(' ', '0');
				numberDisplay.setText(curMsg);
			}
	      });
	      startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                if (recorder.isRecording()) {
                    startButton.setText(getString(R.string.receiver_start_label));
                    recorder.stopRecording();
                } else {
                	startButton.setText(getString(R.string.receiver_abort_label));
                	recorder.startRecording();
                	curMsg = "";
                }
			}
	    	  
	      });
	      return rootView;
	      
	  }
	  
}
