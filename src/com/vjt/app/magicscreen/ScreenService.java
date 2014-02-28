package com.vjt.app.magicscreen;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class ScreenService extends Service implements SensorEventListener {

	private static final String TAG = "ScreenService";

	public static final String ACTION_STARTED = "com.vjt.app.magicscreen.STARTED";
	public static final String ACTION_STOPPED = "com.vjt.app.magicscreen.STOPPED";
	public static final String ACTION_ONLINE = "com.vjt.app.magicscreen.ONLINE";
	public static final String ACTION_OFFLINE = "com.vjt.app.magicscreen.OFFLINE";
	public static final String ACTION_BAD = "com.vjt.app.magicscreen.BAD";

	public static final String ACTION_SCREEN_ON = "screen_on";
	public static final String ACTION_SCREEN_OFF = "screen_off";

	private static final int STATUS_NONE = 0;
	private static final int STATUS_ON = 1;
	private static final int STATUS_OFF = 2;

	private final int NOTIFICATIONID = 7656;

	private static int serviceStatus = STATUS_NONE;

	private static int mFrequency;
	private static float mSensitivity;
	private WakeLock mWakeLock;
	private SensorManager mSensorManager;

	private final IBinder binder = new InternetServiceBinder();

	public class InternetServiceBinder extends Binder {

		/**
		 * Gets the service.
		 * 
		 * @return the service
		 */
		public InternetServiceBinder getService() {
			return InternetServiceBinder.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	private void setupNotification(Context context, int status) {
		String ns = Context.NOTIFICATION_SERVICE;
		int icon, status_label;

		NotificationManager nm = (NotificationManager) context
				.getSystemService(ns);

		switch (status) {
		case ScreenService.STATUS_ON:
			icon = R.drawable.online;
			status_label = R.string.status_online_label;
			break;
		case ScreenService.STATUS_OFF:
			icon = R.drawable.offline;
			status_label = R.string.status_offline_label;
			break;
		default:
			icon = R.drawable.offline;
			status_label = R.string.status_offline_label;
			break;

		}

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pIntent = PendingIntent
				.getActivity(context, 0, intent, 0);

		Notification noti;
		if (Build.VERSION.SDK_INT >= 16) {
			noti = new Notification.Builder(context)
					.setContentTitle(
							context.getString(R.string.status_title_label))
					.setContentIntent(pIntent)
					.setContentText(context.getString(status_label))
					.setSmallIcon(icon).setAutoCancel(false)
					.setPriority(Notification.PRIORITY_HIGH).build();
		} else {
			long when = System.currentTimeMillis();
			CharSequence contentTitle = context
					.getString(R.string.status_title_label);
			CharSequence text = context.getString(status_label);
			CharSequence contentText = context.getString(R.string.app_name);

			noti = new Notification(icon, text, when);
			noti.setLatestEventInfo(this, contentTitle, contentText, pIntent);
		}
		noti.flags = Notification.FLAG_NO_CLEAR;
		nm.notify(NOTIFICATIONID, noti);
		startForeground(NOTIFICATIONID, noti);

	}

	private void clearNotification(Context context) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nm = (NotificationManager) context
				.getSystemService(ns);
		nm.cancelAll();
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;

			Intent serviceIntent = new Intent(context, ScreenService.class);

			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				LogUtil.d(TAG, "Receive Screen on");
				serviceIntent.setAction(ACTION_SCREEN_ON);
				startService(serviceIntent);
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				LogUtil.d(TAG, "Receive Screen off");
				serviceIntent.setAction(ACTION_SCREEN_OFF);
				startService(serviceIntent);
			}

		}
	};

	@Override
	public void onCreate() {
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(receiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		cancelWatchdog();

		if (intent.getAction() == null
				|| intent.getAction().equals(ACTION_SCREEN_ON)) {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);

			int f = (settings.getInt("frenqucy", 1));
			switch (f) {
			case 0:
				mFrequency = Integer
						.parseInt(getString(R.string.interval_0_default));
				break;
			case 1:
				mFrequency = Integer
						.parseInt(getString(R.string.interval_1_default));
				break;
			case 2:
				mFrequency = Integer
						.parseInt(getString(R.string.interval_2_default));
				break;
			}

			int s = (settings.getInt("sensitivity", 1));
			switch (s) {
			case 0:
				mSensitivity = Float
						.parseFloat(getString(R.string.sensitivity_0_default));
				break;
			case 1:
				mSensitivity = Float
						.parseFloat(getString(R.string.sensitivity_1_default));
				break;
			case 2:
				mSensitivity = Float
						.parseFloat(getString(R.string.sensitivity_2_default));
				break;
			}
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
		} else if (intent.getAction().equals(ACTION_STOPPED)) {
			stopSelf(startId);
			return START_NOT_STICKY;
		} else if (intent.getAction().equals(ACTION_SCREEN_OFF)) {
			resetStatus();
			return START_REDELIVER_INTENT;
		}

		sendBroadcast(new Intent(ACTION_STARTED));
		return START_REDELIVER_INTENT;
	}

	PendingIntent createAlarmIntent() {
		Intent i = new Intent();
		i.setClass(this, ScreenService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		return pi;
	}

	private void cancelWatchdog() {
		PendingIntent pi = createAlarmIntent();
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(pi);
	}

	private void setWatchdog(int delay) {
		PendingIntent pi = createAlarmIntent();
		long timeNow = SystemClock.elapsedRealtime();

		long nextCheckTime = timeNow + delay;
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime, pi);
	}

	private void resetStatus() {
		serviceStatus = STATUS_NONE;
		cancelWatchdog();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sendBroadcast(new Intent(ACTION_STOPPED));

		resetStatus();
		unregisterReceiver(receiver);
		clearNotification(this);
		stopForeground(true);
	}

	private void acquireWakeLock() {
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "com.vjt.app.magicscreen");
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			int oldServiceStatus = serviceStatus;

			LogUtil.d(TAG, "x = " + x);
			LogUtil.d(TAG, "y = " + y);
			LogUtil.d(TAG, "z = " + z);

			if (Math.abs(y) > mSensitivity) {
				serviceStatus = STATUS_ON;
				if (oldServiceStatus != serviceStatus) {
					acquireWakeLock();
					releaseWakeLock();
					acquireWakeLock();
					setupNotification(this, STATUS_ON);
				}
			} else {
				serviceStatus = STATUS_OFF;
				if (oldServiceStatus != serviceStatus) {
					releaseWakeLock();
					setupNotification(this, STATUS_OFF);
				}
			}
		}
		LogUtil.d(TAG, "serviceStatus = " + serviceStatus);
		mSensorManager.unregisterListener(this);
		setWatchdog(mFrequency * 1000);
	}

}
