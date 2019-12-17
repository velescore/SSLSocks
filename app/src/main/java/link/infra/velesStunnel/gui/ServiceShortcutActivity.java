package link.infra.velesStunnel.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import link.infra.velesStunnel.service.StunnelIntentService;

public class ServiceShortcutActivity extends Activity {
	OpenVPNIntegrationHandler openVPNIntegrationHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StunnelIntentService.start(this);
		String openVpnProfile = PreferenceManager.getDefaultSharedPreferences(this).getString("open_vpn_profile", "");
		if (openVpnProfile != null && openVpnProfile.trim().length() > 0) {
			openVPNIntegrationHandler = new OpenVPNIntegrationHandler(this, new Runnable() {
				@Override
				public void run() {
					ServiceShortcutActivity.this.finish();
				}
			}, openVpnProfile, false);
			openVPNIntegrationHandler.bind();
		}
		if (openVPNIntegrationHandler == null) {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == OpenVPNIntegrationHandler.PERMISSION_REQUEST) {
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
	protected void onDestroy() {
		super.onDestroy();
		if (openVPNIntegrationHandler != null) {
			openVPNIntegrationHandler.unbind();
		}
	}
}
