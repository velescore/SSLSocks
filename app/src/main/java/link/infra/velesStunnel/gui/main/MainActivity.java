package link.infra.velesStunnel.gui.main;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;

import link.infra.velesStunnel.R;
import link.infra.velesStunnel.gui.AdvancedSettingsActivity;
import link.infra.velesStunnel.gui.OpenVPNIntegrationHandler;
import link.infra.velesStunnel.gui.keymgmt.KeyEditActivity;
import link.infra.velesStunnel.gui.keymgmt.KeyFragment;
import link.infra.velesStunnel.gui.keymgmt.KeyRecyclerViewAdapter;
import link.infra.velesStunnel.service.StunnelIntentService;
import link.infra.velesStunnel.service.StunnelProcessManager;

import static link.infra.velesStunnel.gui.keymgmt.KeyEditActivity.ARG_EXISTING_FILE_NAME;

public class MainActivity extends AppCompatActivity implements KeyFragment.OnListFragmentInteractionListener {

	private FloatingActionButton fabAdd;
	public static final String CHANNEL_ID = "NOTIFY_CHANNEL_1";
	private WeakReference<KeyFragment> keysFragment;
	private static final int KEY_EDIT_REQUEST = 1;
	private OpenVPNIntegrationHandler openVPNIntegrationHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		ViewPager mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(mViewPager);

		mViewPager.addOnPageChangeListener(onPageChangeListener);

		fabAdd = findViewById(R.id.fab);
		fabAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, KeyEditActivity.class);
				startActivityForResult(intent, KEY_EDIT_REQUEST);
			}
		});

		// attempt extraction in activity, to make service start faster
		StunnelProcessManager.checkAndExtract(this);
		StunnelProcessManager.setupConfig(this);

		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = getString(R.string.notification_channel);
			String description = getString(R.string.notification_desc);
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			if (notificationManager != null) {
				notificationManager.createNotificationChannel(channel);
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, AdvancedSettingsActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	class SectionsPagerAdapter extends FragmentPagerAdapter {

		SectionsPagerAdapter(FragmentManager fm) {
			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@NonNull
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a Fragment.
			switch (position) {
				case 0:
					Fragment frag = StartFragment.newInstance(new StartFragment.OnFragmentInteractionListener() {
						@Override
						public void onFragmentStartInteraction() {
							StunnelIntentService.start(getApplicationContext());
							String openVpnProfile = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString("open_vpn_profile", "");
							if (openVpnProfile != null && openVpnProfile.trim().length() > 0) {
								openVPNIntegrationHandler = new OpenVPNIntegrationHandler(MainActivity.this, new Runnable() {
									@Override
									public void run() {}
								}, openVpnProfile, false);
								openVPNIntegrationHandler.bind();
							}
						}

						@Override
						public void onFragmentStopInteraction() {
							stopStunnelService();
							if (openVPNIntegrationHandler != null) {
								openVPNIntegrationHandler.disconnect();
							}
						}
					});
					StunnelIntentService.checkStatus(MainActivity.this);
					return frag;
				case 1:
					return LogFragment.newInstance();
				case 2:
					return ConfigEditorFragment.newInstance();
				case 3:
					keysFragment = new WeakReference<>(KeyFragment.newInstance());
					return keysFragment.get();
//				case 3:
//					return ServersFragment.newInstance(1, new ServersFragment.OnListFragmentInteractionListener() {
//						@Override
//						public void onListFragmentInteraction(DummyContent.DummyItem item) {
//							AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
//							alertDialog.setTitle("hi");
//							alertDialog.setMessage("this is my app");
//
//							alertDialog.show();
//						}
//					});
			}

			throw new RuntimeException("Invalid fragment reached");
		}

		@Override
		public int getCount() {
			Resources res = getResources();
			String[] tabs = res.getStringArray(R.array.tabs_array);
			return tabs.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Resources res = getResources();
			String[] tabs = res.getStringArray(R.array.tabs_array);
			return tabs[position];
		}
	}

	private final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {} // nothing needed here

		@Override
		public void onPageSelected(int position) {
			if (position == 3) {
				fabAdd.show();
			} else {
				fabAdd.hide();
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {} // nothing needed here
	};

	private void stopStunnelService() {
		Intent intent = new Intent(this, StunnelIntentService.class);
		stopService(intent);
	}

	public void onListFragmentInteraction(KeyRecyclerViewAdapter.KeyItem item) {
		Intent intent = new Intent(MainActivity.this, KeyEditActivity.class);
		intent.putExtra(ARG_EXISTING_FILE_NAME, item.filename);
		startActivityForResult(intent, KEY_EDIT_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == KEY_EDIT_REQUEST) {
			if (resultCode == RESULT_OK) {
				if (keysFragment != null) {
					KeyFragment frag = keysFragment.get();
					if (frag != null) {
						frag.updateList(this); // Ensure list is up to date
					}
				}
			}
		} else if (requestCode == OpenVPNIntegrationHandler.PERMISSION_REQUEST) {
			if (resultCode == RESULT_OK && openVPNIntegrationHandler != null) {
				openVPNIntegrationHandler.doVpnPermissionRequest();
			}
		} else if (requestCode == OpenVPNIntegrationHandler.VPN_PERMISSION_REQUEST) {
			if (resultCode == RESULT_OK && openVPNIntegrationHandler != null) {
				openVPNIntegrationHandler.connectProfile();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		StunnelIntentService.checkStatus(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (openVPNIntegrationHandler != null) {
			openVPNIntegrationHandler.unbind();
		}
	}
}
