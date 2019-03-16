package vadim.potomac;


import java.lang.ref.WeakReference;
import java.util.Objects;

import vadim.playpotomac.R;
import vadim.potomac.model.NoaaData;

import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
	
public class ForecastConditionsFragment extends Fragment {
	private NoaaData mNoaaData;
	private WeakReference<DownloadForecastData> forecastTaskWeakReference;

	@Override
	public void onCreate (Bundle savedState) {
    	super.onCreate(savedState);
    	setRetainInstance(true);
    	
    	startNewForecastAsyncTask ();
    }

	public void refresh () {
		startNewForecastAsyncTask ();		
	}
	
    private void startNewForecastAsyncTask() {
    	DownloadForecastData asyncTask = new DownloadForecastData(this);	    			
        this.forecastTaskWeakReference = new WeakReference<>(asyncTask);
        asyncTask.execute(getString (R.string.levelForecastUrl));
    }
    
    private boolean isForecastAsyncTaskPendingOrRunning() {
        return this.forecastTaskWeakReference != null &&
              this.forecastTaskWeakReference.get() != null && 
              !this.forecastTaskWeakReference.get().getStatus().equals(Status.FINISHED);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.noaa_data, container, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (isForecastAsyncTaskPendingOrRunning()	) {
            LinearLayout noaaProgress = Objects.requireNonNull(getView()).findViewById(R.id.noaaProgress);
            if (noaaProgress != null)
            	noaaProgress.setVisibility(View.VISIBLE);          	
        }
    }
    
    public NoaaData getNoaaData () { return mNoaaData; }
    public void setNoaaData (NoaaData noaaData) { mNoaaData = noaaData; }
    
}
