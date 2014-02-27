package com.vjt.app.magicscreen;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnCheckedChangeListener {

	private static final String TAG = "MainActivity";

	ToggleButton mOnOffButton;
	// EditText mURL;
	EditText mInterval;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mOnOffButton = (ToggleButton) findViewById(R.id.running_state_toogle_button);
		mOnOffButton.setOnCheckedChangeListener(this);

		mInterval = (EditText) findViewById(R.id.interval);
		// mURL = (EditText) findViewById(R.id.url);

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = settings.edit();
		mInterval.setText(settings.getString("interval",
				getString(R.string.interval_default)));

		// mURL.setText(settings.getString("url",
		// getString(R.string.url_default)));

		if (settings.getString("onoff", getString(R.string.onoff_default))
				.equals("on")) {
			// mURL.setEnabled(false);
			mInterval.setEnabled(false);
			mOnOffButton.setChecked(true);
			editor.putString("onoff", "on");
			editor.commit();
			startServer();
		} else {
			// mURL.setEnabled(true);
			mInterval.setEnabled(true);
			mOnOffButton.setChecked(false);
			editor.putString("onoff", "off");
			editor.commit();
			stopServer();
		}

		mInterval.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String newInterval = mInterval.getText().toString();
				int interval = 0;
				boolean clear = false;
				try {
					interval = Integer.parseInt(newInterval);
				} catch (Exception e) {
					clear = true;
				}
				if (interval <= 1 || 65535 < interval || clear) {
					Toast.makeText(MainActivity.this,
							R.string.interval_validation_error,
							Toast.LENGTH_LONG).show();
					return;
				}
				editor.putString("interval", mInterval.getText().toString());
				editor.commit();
			}
		});

		// mURL.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// String url = mURL.getText().toString();
		//
		// if (URLUtil.isValidUrl(url)) {
		// Toast.makeText(MainActivity.this,
		// R.string.url_validation_error, Toast.LENGTH_LONG)
		// .show();
		// return;
		// }
		// editor.putString("url", mURL.getText().toString());
		// editor.commit();
		// }
		// });
	}

	private void startServer() {
		Intent serverService = new Intent(this, ScreenService.class);
		startService(serverService);
	}

	private void stopServer() {
		Intent serverService = new Intent(this, ScreenService.class);
		serverService.setAction(ScreenService.ACTION_STOPPED);
		startService(serverService);
	}

	// pro
	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ScreenService.ACTION_STARTED);
		filter.addAction(ScreenService.ACTION_STOPPED);
		filter.addAction(ScreenService.ACTION_OFFLINE);
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if (arg1) {
			final SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			final SharedPreferences.Editor editor = settings.edit();

			// editor.putString("url", mURL.getText().toString());
			editor.putString("interval", mInterval.getText().toString());
			editor.putString("onoff", "on");
			editor.commit();

			mInterval.setEnabled(false);
			// mURL.setEnabled(false);
			startServer();
		} else {
			final SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			final SharedPreferences.Editor editor = settings.edit();

			editor.putString("onoff", "off");
			editor.commit();

			mInterval.setEnabled(true);
			// mURL.setEnabled(true);
			stopServer();
		}

	}

}
