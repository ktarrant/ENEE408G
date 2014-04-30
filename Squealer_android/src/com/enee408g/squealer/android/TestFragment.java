package com.enee408g.squealer.android;

import java.io.IOException;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.enee408g.squealer.android.AudioRecorder.UpdateListener;

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
	private AudioRecorder recorder = null;
	
	  /** The main dataset that includes all the series that go into a chart. */
	  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	  /** The main renderer that includes all the renderers customizing a chart. */
	  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  /** The chart view that displays the data. */
	  private GraphicalView mChartView;
	  
	  private double minFrequency = 18000.0;
	  
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
	  
	  public void addNewSeries(double[] setToPlot, double sampleFrequency) {
	        String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
	        // create a new series of data
	        XYSeries series = new XYSeries(seriesTitle);
	        if (mDataset.getSeriesCount() > 0) {
	        	mDataset.removeSeries(0);
	        }
	        double binWidth = sampleFrequency / (double)setToPlot.length;
	        mDataset.addSeries(series);
	        for (int x = 0; x < setToPlot.length/2; x++) {
	        	if (binWidth * x > minFrequency) series.add(binWidth*x, setToPlot[x]);
	        	//else series.add(binWidth*x, 0);
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
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater,
	          ViewGroup container, Bundle savedInstanceState) {
	      View rootView = inflater.inflate(
	              R.layout.fragment_test, container, false);
	      
	      final Button startButton = (Button) rootView.findViewById(R.id.test_button_record);
	      final EditText numberDisplay = (EditText) rootView.findViewById(R.id.test_numberDisplay);

	      recorder = new AudioRecorder();
	      recorder.setUpdateListener(new UpdateListener() {
			@Override
			public void onUpdate(double[] fft_result, double sampleFrequency) {
				startButton.setText("Start Test");
				addNewSeries(fft_result, sampleFrequency);
			}
	      });
	      
	      startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isRecording = !isRecording;
                if (isRecording) {
                    startButton.setText("Stop Test");
                    recorder.getPiece();
                } else {
                	// TODO: Cancel recording here
                    //startButton.setText("Start Test");
                    //recorder.stopRecording();
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
