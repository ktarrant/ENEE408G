package com.enee408g.squealer.android;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.support.v4.app.NavUtils;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
        if (hasHeaders()) {
            Button button = new Button(this);
            button.setText("Restore Defaults");
            button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					//PreferenceHelper.restoreDefaults();
				}
            });
            setListFooter(button);
        }
    }
	
	// Necessary in API 19
	@Override
	protected boolean isValidFragment (String fragmentName)
	{
	  if(FreqPrefsFragment.class.getName().equals(fragmentName))
	      return true;
	  return false;

	}

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default: return super.onOptionsItemSelected(item);
		}
	}
	
	private void openSettings() {
	    Intent intent = new Intent(this, SettingsActivity.class);
	    startActivity(intent);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
	
	public static class FreqPrefsFragment extends PreferenceFragment {
		
		SharedPreferences prefs = null;
		
		public OnSharedPreferenceChangeListener prefListener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences prefs, String key) {
				updatePreference(prefs, key);
			}
		};
		
		public void updatePreference(SharedPreferences prefs, String key) {
		    Preference pref = findPreference(key);

		    if (pref instanceof EditTextPreference) {
		        EditTextPreference listPref = (EditTextPreference) pref;
		        pref.setSummary(prefs.getString(key, "None" ) + " Hz");
		    }
		}
		
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_freq);
    		
    		
        }
        
        @Override
        public void onResume() {
        	super.onResume();
        	
        	prefs.registerOnSharedPreferenceChangeListener(prefListener);
        	
        	//for (int i = 0;i < this.getPreferenceScreen().getPreferenceCount(); );
        	//}
        }
        
        @Override
        public void onPause() {
        	super.onPause();
        	
        	prefs.unregisterOnSharedPreferenceChangeListener(prefListener);
        }
	}
}
