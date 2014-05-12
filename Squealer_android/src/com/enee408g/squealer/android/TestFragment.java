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

import com.enee408g.squealer.android.AudioRecorder.PowerListener;
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
	private String curMsg = "";
	private AudioRecorder recorder = null;
	private float startTime = 0.0f;
	
	  /** The main dataset that includes all the series that go into a chart. */
	  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  /** The main renderer that includes all the renderers customizing a chart. */
	  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  /** The chart view that displays the data. */
	  private GraphicalView mChartView;
	  
	@Override
	public void onPause() {
		super.onPause();
		if (recorder != null) recorder.stopDetection();
	}
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  	super.onCreate(savedInstanceState);
		    // set some properties on the main renderer
		    mRenderer.setApplyBackgroundColor(true);
		    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		    mRenderer.setAxisTitleTextSize(16);
		    mRenderer.setChartTitleTextSize(20);
		    mRenderer.setLabelsTextSize(15);
		    mRenderer.setLegendTextSize(15);
		    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		    mRenderer.setZoomButtonsVisible(true);
		    mRenderer.setPointSize(5);
	  }
	  
	  public void createSeriesList(String[] labels) {
		  XYMultipleSeriesDataset rval = new XYMultipleSeriesDataset();
		  for (int i = 0; i < labels.length; i++) {
			  String label = labels[i];
			  XYSeries series = new XYSeries(label);
			  rval.addSeries(series);
			  int r = 0;
			  int g = 0;
			  int b = 0;
			  if (i % 2 == 0) r = 255;
			  if (i % 4 == 0) g = 255;
			  if (i % 8 == 0) b = 255;
			  XYSeriesRenderer renderer = new XYSeriesRenderer();
			  renderer.setColor(Color.rgb(r, g, b));
			  this.mRenderer.addSeriesRenderer(renderer);
		  }
		  this.mDataset = rval;  
	  }
	  
	  public void addPoint(double[] pts, int maxPoints) {
		  float now = ((float)System.currentTimeMillis()/1000.0f) - startTime;
		  for (int i = 0; i < this.mDataset.getSeriesCount(); i++) {
			  XYSeries series = this.mDataset.getSeriesAt(i);
			  series.add(now, pts[i]);
			  if (series.getItemCount() > maxPoints) series.remove(0);
		  }
		  mChartView.repaint();
	  }
	  
	  public void clearSeries() {
		  for (int i = 0; i < this.mDataset.getSeriesCount(); i++) {
			  this.mDataset.getSeriesAt(i).clear();
		  }
	  }
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_test, container, false);
	      
	      final Button startButton = (Button) rootView.findViewById(R.id.test_button_record);
	      final EditText numberDisplay = (EditText) rootView.findViewById(R.id.test_numberDisplay);

	      recorder = new AudioRecorder();
	      recorder.setPowerListener(new PowerListener() {
			@Override
			public void onBufferUpdate(double[] power) {
				addPoint(power, 256);
			}
	      });
	      recorder.setValueListener(new ValueListener() {
			@Override
			public void onValueUpdate(byte value) {
				if (value != 0) {
					curMsg += new String(new byte[] {value});
					numberDisplay.setText(curMsg);
				}
			}
	      });
	      startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                if (recorder.isDetecting()) {
                    startButton.setText(getString(R.string.receiver_start_label));
                    recorder.stopDetection();
                    //numberDisplay.setText("Aborted.");
                } else {
                	startButton.setText(getString(R.string.receiver_abort_label));
                	//numberDisplay.setText("Waiting for fart...");
                	int[] frequencies	= PreferenceHelper.getAllBitFrequencies(getActivity());
                	int bufferSize 		= PreferenceHelper.getReceiverBufferSize(getActivity());
                	int sampleFrequency = PreferenceHelper.getReceiverSampleRate(getActivity());
                	double dbSens		= PreferenceHelper.getDbSensitivity(getActivity());
                	int fftSize 		= PreferenceHelper.getFFTSize(getActivity());
                	int fftOverlap		= PreferenceHelper.getFFTOverlap(getActivity());
                	recorder.startDetection(frequencies, bufferSize, sampleFrequency, fftSize, fftOverlap, dbSens);
                	startTime = ((float)System.currentTimeMillis())/1000.0f;
                	clearSeries();
                	curMsg = "";
                }
			}
	    	  
	      });
	      int[] frequencies	= PreferenceHelper.getAllBitFrequencies(getActivity());
	      // Create chart
	      String[] labels = new String[frequencies.length];
	      for (int i = 0; i < labels.length; i++) {
	      	labels[i] = String.format("%d Hz", frequencies[i]);
	      }
	      createSeriesList(labels);
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
