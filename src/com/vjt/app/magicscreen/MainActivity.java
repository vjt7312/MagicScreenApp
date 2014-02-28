package com.vjt.app.magicscreen;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnCheckedChangeListener {

	private static final String TAG = "MainActivity";

	ToggleButton mOnOffButton;
	// EditText mURL;
	SeekBar mSeekbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mOnOffButton = (ToggleButton) findViewById(R.id.running_state_toogle_button);
		mOnOffButton.setOnCheckedChangeListener(this);

		mSeekbar = (SeekBar) findViewById(R.id.seekBar);

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = settings.edit();
		mSeekbar.setProgress(settings.getInt("frequency", 1));

		if (settings.getString("onoff", getString(R.string.onoff_default))
				.equals("on")) {
			mSeekbar.setEnabled(false);
			mOnOffButton.setChecked(true);
			editor.putString("onoff", "on");
			editor.commit();
			startServer();
		} else {
			mSeekbar.setEnabled(true);
			mOnOffButton.setChecked(false);
			editor.putString("onoff", "off");
			editor.commit();
			stopServer();
		}
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

			editor.putInt("frenqucy", mSeekbar.getProgress());
			editor.putString("onoff", "on");
			editor.commit();

			mSeekbar.setEnabled(false);
			startServer();
		} else {
			final SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			final SharedPreferences.Editor editor = settings.edit();

			editor.putString("onoff", "off");
			editor.commit();

			mSeekbar.setEnabled(true);
			stopServer();
		}

	}

}
