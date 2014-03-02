package com.vjt.app.magicscreen;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class MainActivity extends Activity implements OnCheckedChangeListener {

	private static final String TAG = "MainActivity";

	private static final String MY_AD_UNIT_ID = "a1530ffcf0970cd";

	ToggleButton mOnOffButton;
	SeekBar mSeekbar1;
	SeekBar mSeekbar2;

	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ---------------------------------------------------------------------
		// 建立 adView
		adView = new AdView(this, AdSize.BANNER, MY_AD_UNIT_ID);

		// 查詢 LinearLayout (假設您已經提供)
		// 屬性是 android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.mainLayout);

		// 在其中加入 adView
		layout.addView(adView);

		// 請求測試廣告
		// AdRequest adRequest = new AdRequest();
		// adRequest.addTestDevice(AdRequest.TEST_EMULATOR); // 模擬工具
		// adRequest.addTestDevice("TEST_DEVICE_ID");
		// adView.loadAd(adRequest);

		// 啟用泛用請求，並隨廣告一起載入
		adView.loadAd(new AdRequest());
		// ---------------------------------------------------------------------

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

        @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent launchNewIntent = new Intent(MainActivity.this,
					ChooseSetting.class);
			startActivityForResult(launchNewIntent, 0);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}

}
