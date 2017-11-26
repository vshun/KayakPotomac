package vadim.potomac;

import java.text.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;

import vadim.potomac.model.NoaaData;
import vadim.potomac.model.RiverForecast;

import android.app.Activity;
import android.graphics.Color;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class PlotFragment extends Fragment {

   	private boolean graphDisplayed = false;
	private static final String TAG = "PlayPotomac.Plot";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.plot, container, false);
        Activity parent = getActivity();
        NoaaData noaaData = ((FragmentActivityCommunicator)parent).getNoaaData();
        try {
        	if (noaaData != null) // data is already available
        		setNoaaData(noaaData);
		} catch (ParseException e) {
			Log.e(TAG, e.getMessage());
		}
        
        return rootView;
    }
    
    public void refresh () {
      	
    	View rootView = getView();
    	if (rootView == null) return; // not inflated yet
    	LinearLayout chart_container=(LinearLayout)rootView.findViewById(R.id.plotImage);
    	chart_container.removeAllViews();
        graphDisplayed = false;
    }
    

    @SuppressWarnings("ConstantConditions")
    public void setNoaaData (NoaaData noaaData) throws ParseException {
    	View rootView = getView();
    	if (rootView == null || graphDisplayed ) 
    		return; // not inflated yet or already displayed
	
    	XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

    	TimeSeries noaaSeries = new TimeSeries("Forecast");
    	TimeSeries trendSeries = new TimeSeries("Trend");
   
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
  
		// find min and max ranges
		double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE, minX = Double.MAX_VALUE, maxX=Double.MIN_VALUE;
        for (RiverForecast rf : noaaData.getForecast()) {
        	if (rf.getLevel() != null) { 
        		double dLevel = Double.parseDouble(rf.getLevel());
        		// also compute min and max for ranges	
        		if (minY > dLevel)		minY = dLevel;
        		if (maxY < dLevel)		maxY = dLevel;
   
        		double tLevel = (Math.round(noaaData.getTrendingLevel(rf.getDate())*100.0))/100.0d;
           		// also compute min and max for ranges	
        		if (minY > tLevel)		minY = tLevel;
        		if (maxY < tLevel)		maxY = tLevel;

        		Date date = df.parse(rf.getDate());
        		long time=date.getTime();
        		if (minX>time) minX=time;
        		if (maxX<time) maxX=time;
        		noaaSeries.add(date, dLevel);
        		trendSeries.add(date, tLevel);
        	}
        }
        dataset.addSeries(noaaSeries);
        dataset.addSeries(trendSeries);
        
        int[] colors = new int[] { Color.BLUE, Color.GREEN };
        PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT };
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setRenderer(renderer, colors, styles);

        long hoursPad = 1000*60*60*6;
        setChartSettings(renderer, minX-hoursPad,
        				maxX+hoursPad, minY-0.1, maxY+0.1, Color.GRAY, Color.LTGRAY, Color.MAGENTA);
        renderer.setXLabels(5);
        renderer.setYLabels(10);
/*  disabled under V2.2
        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
          SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
          seriesRenderer.setDisplayChartValues(true);
        }
*/
        GraphicalView chart= ChartFactory.getTimeChartView(getActivity(), dataset, renderer, "EEE");
        LinearLayout chart_container=(LinearLayout)rootView.findViewById(R.id.plotImage);
        chart_container.addView(chart);
        
        graphDisplayed = true;
	}
    

    private void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
      renderer.setAxisTitleTextSize(16);
      renderer.setChartTitleTextSize(20);
      renderer.setLabelsTextSize(50);
      renderer.setLegendTextSize(50);
      renderer.setPointSize(25f);
      renderer.setMargins(new int[] { 20, 30, 15, 20 });
      int length = colors.length;
      for (int i = 0; i < length; i++) {
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(colors[i]);
        r.setPointStyle(styles[i]);
        r.setLineWidth(5f);
        renderer.addSeriesRenderer(r);
      }
    }
  
    /**
     * Sets a few of the series renderer settings.
     * @param renderer the renderer to set the properties to
     * @param xMin the minimum value on the X axis
     * @param xMax the maximum value on the X axis
     * @param yMin the minimum value on the Y axis
     * @param yMax the maximum value on the Y axis
     * @param axesColor the axes color
     * @param labelsColor the labels color
     */
    private void setChartSettings(XYMultipleSeriesRenderer renderer,
                                  double xMin, double xMax, double yMin, double yMax, int axesColor,
                                  int labelsColor, int yLabelColor) {
      renderer.setXAxisMin(xMin);
      renderer.setXAxisMax(xMax);
      renderer.setYAxisMin(yMin);
      renderer.setYAxisMax(yMax);
      renderer.setAxesColor(axesColor);
      renderer.setLabelsColor(labelsColor);
      renderer.setYLabelsColor(0, yLabelColor);
    }
}	
