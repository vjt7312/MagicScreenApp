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
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnCheckedChangeListener {

	private static final String TAG = "MainActivity";

	ToggleButton mOnOffButton;
	SeekBar mSeekbar1;
	SeekBar mSeekbar2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mOnOffButton = (ToggleButton) findViewById(R.id.running_state_toogle_button);
		mOnOffButton.setOnCheckedChangeListener(this);

		mSeekbar1 = (SeekBar) findViewById(R.id.seekBar1);
		mSeekbar2 = (SeekBar) findViewById(R.id.seekBar2);

		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = settings.edit();
		mSeekbar1.setProgress(settings.getInt("frequency", 1));
		mSeekbar2.setProgress(settings.getInt("sensitivity", 1));

		if (settings.getString("onoff", getString(R.string.onoff_default))
				.equals("on")) {
			mSeekbar1.setEnabled(false);
			mSeekbar2.setEnabled(false);
			mOnOffButton.setChecked(true);
			editor.putString("onoff", "on");
			editor.commit();
			startServer();
		} else {
			mSeekbar1.setEnabled(true);
			mSeekbar2.setEnabled(true);
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

			editor.putInt("frequency", mSeekbar1.getProgress());
			editor.putInt("sensitivity", mSeekbar2.getProgress());
			editor.putString("onoff", "on");
			editor.commit();

			mSeekbar1.setEnabled(false);
			mSeekbar2.setEnabled(false);
			startServer();
		} else {
			final SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			final SharedPreferences.Editor editor = settings.edit();

			editor.putString("onoff", "off");
			editor.commit();

			mSeekbar1.setEnabled(true);
			mSeekbar2.setEnabled(true);
			stopServer();
		}

	}

}
