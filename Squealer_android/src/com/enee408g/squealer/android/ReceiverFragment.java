package com.enee408g.squealer.android;

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
	long lastTime = System.currentTimeMillis();
	
	@Override
	public void onPause() {
		super.onPause();
		if (recorder != null) recorder.stopDetection();
	}
	
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_receiver, container, false);
	      
	      final Button startButton = (Button) rootView.findViewById(R.id.receiver_button_record);
	      final EditText numberDisplay = (EditText) rootView.findViewById(R.id.receiver_message);
	      
	      recorder = new AudioRecorder();
	      recorder.setValueListener(new ValueListener() {
			@Override
			public void onValueUpdate(byte value) {
				if (value != 0) {
					curMsg += new String(new byte[] {value});
					numberDisplay.setText(curMsg);
				}
				// Try to give the UI a break by not overburdening it calls
				long now = System.currentTimeMillis();
				if (now - lastTime > 200) {
					numberDisplay.setText(curMsg);
					lastTime = now;
				}
			}
	      });
	      startButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
	                if (recorder.isDetecting()) {
	                    startButton.setText(getString(R.string.receiver_start_label));
	                    recorder.stopDetection();
	                } else {
	                	startButton.setText(getString(R.string.receiver_abort_label));
	                	int[] frequencies	= PreferenceHelper.getAllBitFrequencies(getActivity());
	                	int bufferSize 		= PreferenceHelper.getPulseSampleWidth(getActivity());
	                	int sampleFrequency = PreferenceHelper.getSampleRate(getActivity());
	                	double dbSens		= PreferenceHelper.getDbSensitivity(getActivity());
	                	recorder.startDetection(frequencies, bufferSize, sampleFrequency, 512, 32, dbSens);
	                	curMsg = "";
	                }
				}
	      });
	      return rootView;
	      
	  }
	  
}
