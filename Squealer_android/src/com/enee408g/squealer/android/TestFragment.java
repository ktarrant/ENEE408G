package com.enee408g.squealer.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.enee408g.squealer.android.AudioRecorder.BufferListener;
import com.enee408g.squealer.android.AudioRecorder.FartListener;
import com.enee408g.squealer.android.AudioRecorder.ValueListener;

import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

public class TestFragment extends Fragment {
	
	private final static String TAG = "TestFragment";
	private boolean isRecording = false;
	private boolean startedMessage = false;
	private AudioRecorder recorder = null;
	
	  /** The main dataset that includes all the series that go into a chart. */
	  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  /** The main renderer that includes all the renderers customizing a chart. */
	  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  /** The chart view that displays the data. */
	  private GraphicalView mChartView;
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  	super.onCreate(savedInstanceState);
		    // set some properties on the main renderer
		    //mRenderer.setApplyBackgroundColor(true);
		    //mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		    mRenderer.setAxisTitleTextSize(16);
		    mRenderer.setChartTitleTextSize(20);
		    mRenderer.setLabelsTextSize(15);
		    mRenderer.setLegendTextSize(15);
		    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		    mRenderer.setZoomButtonsVisible(true);
		    mRenderer.setPointSize(5);
	  }
	  
	  public void addNewSeries(double[] setToPlot, double sampleFrequency, double minFrequency) {
	        String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
	        // create a new series of data
	        XYSeries series = new XYSeries(seriesTitle);
	        if (mDataset.getSeriesCount() > 0) {
	        	mDataset.removeSeries(0);
	        }
	        double binWidth = sampleFrequency / (double)setToPlot.length;
	        mDataset.addSeries(series);
	        for (int x = 0; x < setToPlot.length/2; x++) {
	        	if (binWidth * x > minFrequency) { // only check desired frequencies
	        		series.add(binWidth*x, setToPlot[x]);
	        	}
	        }
	        //mCurrentSeries = series;
	        // create a new renderer for the new series
	        XYSeriesRenderer renderer = new XYSeriesRenderer();
	        mRenderer.addSeriesRenderer(renderer);
	        // set some renderer properties
	        //renderer.setPointStyle(PointStyle.CIRCLE);
	        //renderer.setFillPoints(true);
	        //renderer.setDisplayChartValues(true);
	        //renderer.setDisplayChartValuesDistance(10);
	        //mCurrentRenderer = renderer;
	        //setSeriesWidgetsEnabled(true);
	        mChartView.repaint();
	  }
	  
		Byte[] MASK = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte)0x80, 
				0x00}; // This last one is to accomodate the final bit - although it is not ready yet
	  
	  public byte getValue(double[] fft_result, double minFrequency, double sampleFrequency) {
		double binWidth = sampleFrequency / (double)fft_result.length;
		float dbsense = PreferenceHelper.getDbSensitivity(getActivity());
		boolean inBand = false;
		double minVal = 0.0;
		double maxVal = 100000.0;
		double minFreq = maxVal;
		double maxFreq = minVal;
		ArrayList<double[]> bands = new ArrayList<double[]>();
		  for (int i = 0; i < fft_result.length/2; i++) {
			double freq = (double)i * binWidth;
			if (freq > minFrequency && freq < (sampleFrequency - minFrequency)) {
				if (fft_result[i] > dbsense) {
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
		 byte rval = 0;
		 for (double[] band : bands) {
			 int[] freqs = PreferenceHelper.getAllBitFrequencies(getActivity());
			 for (int i = 0; i < freqs.length; i++) {
				 if (freqs[i] > band[0] && freqs[i] < band[1]) {
					 rval |= MASK[i];
				 }
			 }
		 }
		 return rval;
		  //return (double[][])bands.toArray(new double[bands.size()][2]);
	  }
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_test, container, false);
	      
	      final Button startButton = (Button) rootView.findViewById(R.id.test_button_record);
	      final EditText numberDisplay = (EditText) rootView.findViewById(R.id.test_numberDisplay);

	      recorder = new AudioRecorder(getActivity());
	      recorder.setBufferListener(new BufferListener() {
			@Override
			public void onBufferUpdate(short[] msg) {
				//Log.i(TAG, String.format("Buffer received: %d", msg.length));
				//if (startedMessage) {
					recorder.processBuffer(msg);
				//} else {
				//	recorder.checkForFart(msg);
				//}
//				double binWidth = sampleFrequency / (double)fft_result.length;
//				startButton.setText("Start Test");
//				startButton.setEnabled(true);
//				byte msg = getValue(fft_result, minFrequency, sampleFrequency);
//				String outMsg = String.format("%16s", Integer.toBinaryString(msg)).replace(' ', '0');
//				numberDisplay.setText(outMsg);
//				addNewSeries(fft_result, sampleFrequency, minFrequency);
			}
	      });
	      recorder.setValueListener(new ValueListener() {
			@Override
			public void onValueUpdate(byte[] msg) {
//				Log.i(TAG, String.format("FFT received: %d", msg.length));
				String outMsg = "";
				for (byte m : msg)
					outMsg += String.format("%8s", Integer.toBinaryString(m)).replace(' ', '0');
				numberDisplay.setText(outMsg);
			}
	      });
//	      recorder.setFartListener(new FartListener() {
//			@Override
//			public void onFartUpdate() {
//				if (startedMessage) {
//					recorder.stopRecording();
//					numberDisplay.setText(getString(R.string.receiver_start_label) + " (Success)");
//				} else {
//					numberDisplay.setText("Recording!");
//					startedMessage = true;
//				}
//			}
//	      });
	      startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                if (recorder.isRecording()) {
                    startButton.setText(getString(R.string.receiver_start_label));
                    recorder.stopRecording();
                    //numberDisplay.setText("Aborted.");
                } else {
                	startButton.setText(getString(R.string.receiver_abort_label));
                	//numberDisplay.setText("Waiting for fart...");
                	//startedMessage = false;
                	recorder.startRecording();
                }
			}
	    	  
	      });
	      
	      // Create chart
	      
	      LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.chart);
	      mChartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);
	      // enable the chart click events
	      mRenderer.setClickEnabled(true);
	      mRenderer.setSelectableBuffer(10);
	      mChartView.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	          // handle the click event on the chart
	          SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
	          if (seriesSelection == null) {
	            Toast.makeText(getActivity(), "No chart element", Toast.LENGTH_SHORT).show();
	          } else {
	            // display information of the clicked point
	            Toast.makeText(getActivity(),
	                "Chart element in series index " + seriesSelection.getSeriesIndex()
	                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
	                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
	                    + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
	          }
	        }
	      });
	      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
	          LayoutParams.FILL_PARENT));
	      
	      return rootView;
	      
	  }
	  
}
