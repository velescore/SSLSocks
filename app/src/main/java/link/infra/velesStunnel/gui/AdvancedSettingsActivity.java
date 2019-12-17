package link.infra.velesStunnel.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import link.infra.velesStunnel.BuildConfig;
import link.infra.velesStunnel.R;
import link.infra.velesStunnel.service.StunnelProcessManager;

public class AdvancedSettingsActivity extends AppCompatActivity {

	private MutableLiveData<String> stunnelVersionStringMutable = new MutableLiveData<>();
	private LiveData<String> stunnelVersionString = stunnelVersionStringMutable;
	private Thread checkVersionThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_settings);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

		checkVersionThread = new Thread(new Runnable() {
			@Override
			public void run() {
				stunnelVersionStringMutable.postValue(new StunnelProcessManager().checkStunnelVersion(AdvancedSettingsActivity.this));
			}
		});
		checkVersionThread.start();
	}

	public static class AdvancedSettingsActivityFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.advanced_settings, rootKey);

			Preference appVersionPreference = findPreference("version");
			if (appVersionPreference != null) {
				final String versionString = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
				appVersionPreference.setSummaryProvider(new Preference.SummaryProvider<Preference>() {
					@Override
					public CharSequence provideSummary(Preference preference) {
						return versionString;
					}
				});
			}
		}

		@Override
		public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			final Preference stunnelVersionPreference = findPreference("stunnelVersion");
			final AdvancedSettingsActivity asa = (AdvancedSettingsActivity) getActivity();
			if (stunnelVersionPreference != null && asa != null) {
				asa.stunnelVersionString.observe(getViewLifecycleOwner(), new Observer<String>() {
					@Override
					public void onChanged(String s) {
						stunnelVersionPreference.setSummary(s);
					}
				});
			}

			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	@Override
	protected void onDestroy() {
		checkVersionThread.interrupt();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
