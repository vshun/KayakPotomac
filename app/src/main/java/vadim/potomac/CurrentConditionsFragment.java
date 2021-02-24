package vadim.potomac;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import vadim.playpotomac.R;
import vadim.potomac.model.ForecastWeather;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CurrentConditionsFragment extends Fragment {
	private Playspots mPlayspots = null;
	private List<ForecastWeather> mWeatherInfo = null;
	private float mCurrentLevel;
	private WeakReference<DownloadWeatherData> weatherTaskWeakReference;
	private WeakReference<USGSTask> usgsTaskWeakReference;
	private WeakReference<TideTask> tideTaskWeakReference;
	private static final String TAG = "Potomac.Current";
	
    
    @Override
    public void onCreate (Bundle savedState) {
    	super.onCreate(savedState);
    	setRetainInstance(true);
		try {
			// playspots are needed to be initialized prior to task execution
			mPlayspots = new Playspots (getResources().getXml(R.xml.playspots));
			refresh();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}		  	
    }

	public void refresh () {
		startNewWeatherAsyncTask();
		startNewUSGSAsyncTask(mPlayspots);
		startNewTideAsyncTask();
	}

    private void startNewWeatherAsyncTask() {
    	DownloadWeatherData asyncTask = new DownloadWeatherData(this);
        this.weatherTaskWeakReference = new WeakReference<>(asyncTask);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString (R.string.weatherForecastUrl));
    }

	private boolean isWeatherAsyncTaskPendingOrRunning() {
   		return this.weatherTaskWeakReference != null &&
				this.weatherTaskWeakReference.get() != null &&
				!this.weatherTaskWeakReference.get().getStatus().equals(Status.FINISHED);

    }

    private void startNewUSGSAsyncTask(Playspots playspots) {
    	USGSTask asyncTask = new USGSTask(this, playspots);
        this.usgsTaskWeakReference = new WeakReference<>(asyncTask);
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString (R.string.usgsUrl));
    }
    
    private boolean isUSGSAsyncTaskPendingOrRunning() {
        return this.usgsTaskWeakReference != null &&
              this.usgsTaskWeakReference.get() != null && 
              !this.usgsTaskWeakReference.get().getStatus().equals(Status.FINISHED);
    }


	private void startNewTideAsyncTask() {
		TideTask asyncTask = new TideTask (this);
		this.tideTaskWeakReference = new WeakReference<>(asyncTask);
		asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString (R.string.tideForecastUrl));
	}

	private boolean isTideAsyncTaskPendingOrRunning() {
		return this.tideTaskWeakReference != null &&
				this.tideTaskWeakReference.get() != null &&
				!this.tideTaskWeakReference.get().getStatus().equals(Status.FINISHED);
	}

	@Override
    public void onResume() {
        super.onResume();
        if (isWeatherAsyncTaskPendingOrRunning()||
        		isUSGSAsyncTaskPendingOrRunning() ||
				isTideAsyncTaskPendingOrRunning()) {
            LinearLayout nowProgress = Objects.requireNonNull(getView()).findViewById(R.id.nowProgress);
            if (nowProgress != null)
            	nowProgress.setVisibility(View.VISIBLE);       }	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        return inflater.inflate(R.layout.now, container, false);
    }

    public Playspots getPlayspots () {
    	return mPlayspots;
    }
 	public float getCurrentLevel() {
		return mCurrentLevel;
	}
	public void setCurrentLevel(float currentLevel) {
		this.mCurrentLevel = currentLevel;
	}
	public List<ForecastWeather> getWeatherInfo() {
		return mWeatherInfo;
	}
	public void setWeatherInfo(List<ForecastWeather> weatherInfo) {
		mWeatherInfo = weatherInfo;
	}
}
