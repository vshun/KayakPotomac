package vadim.potomac;


import java.util.List;
import java.util.Locale;

import vadim.playpotomac.R;
import vadim.potomac.model.ForecastWeather;
import vadim.potomac.model.NoaaData;
import vadim.potomac.util.HttpUtil;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

public class KayakPotomacActivity extends FragmentActivity implements
		ActionBar.TabListener, 
		FragmentActivityCommunicator,
		OnSharedPreferenceChangeListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	
	private static final String TAG = "PlayPotomac.Main";

	// fragment position
	private static final int CURRENT_POSITION = 0;
	private static final int FORECAST_POSITION = 1;
	private static final int PLOT_POSITION = 2;
	private static final int PLAYSPOT_POSITION = 3;
	
	static private final int NUM_ITEMS = 4;
		
	@SuppressWarnings("ConstantConditions")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kayak_potomac);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
		actionBar.setHomeButtonEnabled(false);
		// Specify that we will be displaying tabs in the action bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(NUM_ITEMS-1);
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@SuppressWarnings("ConstantConditions")
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
			registerOnSharedPreferenceChangeListener (this);				

		exitIfNoConnection();
	}


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		reloadFragmentsData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kayak_potomac, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			reloadFragmentsData();
			return true;
		case R.id.preferences:
			Intent settingsActivity = new Intent(getBaseContext(),
					KayakPreferenceActivity.class);
			startActivity(settingsActivity);
			return true;		
		case R.id.exit:
			finish();
			return true;
		case R.id.about:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.About));
			builder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
			builder.show();
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void exitIfNoConnection() {
		try {
			if (!HttpUtil.checkInternetConnection(getApplicationContext())) 
				makeAndShowDialogBox();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	private void makeAndShowDialogBox(){

		new AlertDialog.Builder(this)
        //set message, title, and icon
        .setTitle("No internet")
        .setMessage(getString(R.string.NoConnection))
        .setIcon(R.drawable.ic_menu_close_clear_cancel)
        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                exitIfNoConnection();
            }
        })//setPositiveButton
        .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
             finish();
         }
        })//setNegativeButton
        .create();
	}
	
	private void reloadFragmentsData() {
		CurrentConditionsFragment ccf = (CurrentConditionsFragment)
				mSectionsPagerAdapter.getRegisteredFragment(CURRENT_POSITION);		
		ForecastConditionsFragment fcf = (ForecastConditionsFragment)
				mSectionsPagerAdapter.getRegisteredFragment(FORECAST_POSITION);
		PlotFragment pf = (PlotFragment)
				mSectionsPagerAdapter.getRegisteredFragment(PLOT_POSITION);
		PlayspotsFragment psf = (PlayspotsFragment)
				mSectionsPagerAdapter.getRegisteredFragment(PLAYSPOT_POSITION);
		// first refresh last 2 fragments so they have clean slate, it will be 
		// populated by main 2 fragments during their refresh that follows
		psf.refresh();
		pf.refresh();
		ccf.refresh();
		fcf.refresh();
	}
	
	@Override
	public void onLevelLoaded() {
		// signal to playspot fragment	
		runOnUiThread(new Runnable() { //Use the runOnUIThread method to do your UI hanlding in the UI Thread
			public void run()   {
				try {
					PlayspotsFragment pf = (PlayspotsFragment)
						mSectionsPagerAdapter.getRegisteredFragment(PLAYSPOT_POSITION);
				if (pf != null)	pf.populatePlayspotsTab();
		        Log.d("Vadim", "after onLevelLoaded"+(pf!=null));			
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}				}    
		});
	}

	@Override
	public void onForecastLoaded(final NoaaData noaaData) {
		// send a signal to plot fragment
		runOnUiThread(new Runnable() { //Use the runOnUIThread method to do your UI handling in the UI Thread
			public void run()   {
				try {
					PlotFragment plotFragment = (PlotFragment)
						mSectionsPagerAdapter.getRegisteredFragment(PLOT_POSITION);
			        if (plotFragment != null) 
			        	plotFragment.setNoaaData (noaaData);	
				} catch (java.text.ParseException e) {
				   	Log.e(TAG, e.getMessage());
				}
			}    
		});
	}
	
	public Playspots getPlayspots () {
		Playspots ps = null;
		CurrentConditionsFragment f = (CurrentConditionsFragment)
				mSectionsPagerAdapter.getRegisteredFragment(CURRENT_POSITION);		
		if (f != null)
			ps = f.getPlayspots();
		return ps;
	}
	
    public float getCurrentLevel () {
    	float level = 0f;
		CurrentConditionsFragment f = (CurrentConditionsFragment)
				mSectionsPagerAdapter.getRegisteredFragment(CURRENT_POSITION);		
		if (f != null)
			level=f.getCurrentLevel();
		return level;
    }
    
    public List<ForecastWeather> getWeatherInfo () {
		List<ForecastWeather>  wi = null;
    	CurrentConditionsFragment f = (CurrentConditionsFragment)
				mSectionsPagerAdapter.getRegisteredFragment(CURRENT_POSITION);		
		if (f != null)
			wi = f.getWeatherInfo();
		return wi;
    }
    
    public NoaaData getNoaaData () {
    	NoaaData nd = null;
    	ForecastConditionsFragment f = (ForecastConditionsFragment)
				mSectionsPagerAdapter.getRegisteredFragment(FORECAST_POSITION);		
		if (f != null)
			nd = f.getNoaaData();
		return nd;
    }
	
	private class SectionsPagerAdapter extends FragmentPagerAdapter {
		final SparseArray<Fragment> registeredFragments = new SparseArray<>();
		private final Context context;
		
		SectionsPagerAdapter(Context context, FragmentManager fm) {
			super(fm);
			this.context = context;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			Fragment fragment = null;
			
			switch (position) {
			case CURRENT_POSITION:
				fragment = new CurrentConditionsFragment();
				break;
			case FORECAST_POSITION:	
				fragment = new ForecastConditionsFragment();
				break;
			case PLOT_POSITION:
				fragment = new PlotFragment();
				break;
			case PLAYSPOT_POSITION:
				fragment = new PlayspotsFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		@Override
	    public Object instantiateItem(ViewGroup container, int position) {
	        Fragment fragment = (Fragment) super.instantiateItem(container, position);
	        registeredFragments.put(position, fragment);
	        return fragment;
	    }

	    @Override
	    public void destroyItem(ViewGroup container, int position, Object object) {
	        registeredFragments.remove(position);
	        super.destroyItem(container, position, object);
	    }

	    Fragment getRegisteredFragment(int position) {
	        return registeredFragments.get(position);
	    }

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			int titleID;
			
			switch (position) {
			case 0:
				titleID = R.string.title_section1;
				break;
			case 1:
				titleID = R.string.title_section2;
				break;
			case 2:
				titleID = R.string.title_section3;
				break;
			case 3:
				titleID = R.string.title_section4;
				break;
			default:
				titleID = 0;
			}
			return titleID != 0 ? context.getString(titleID).toUpperCase(l) : null;
		}
	}
}
