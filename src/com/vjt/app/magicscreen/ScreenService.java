package com.vjt.app.magicscreen;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class ScreenService extends Service {

	private static final String TAG = "InternetService";

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
	private static final int STATUS_BAD = 3;

	private final int NOTIFICATIONID = 7656;

	private static int serviceStatus = STATUS_NONE;

	private static int mInterval;
	private static String mURL;

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
		case ScreenService.STATUS_BAD:
			icon = R.drawable.bad;
			status_label = R.string.status_bad_label;
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

	public class NetTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			InetAddress addr = null;
			try {
				synchronized (this) {
					addr = InetAddress.getByName(params[0]);
				}
			} catch (UnknownHostException e) {
				return null;
			} catch (Exception e) {
				return null;
			}
			return addr.getHostAddress();
		}

		@Override
		protected void onPostExecute(String netAddress) {
			if (netAddress == null) {
				if (serviceStatus != STATUS_OFF)
					setupNotification(ScreenService.this,
							ScreenService.STATUS_OFF);
				serviceStatus = STATUS_OFF;
				sendBroadcast(new Intent(ACTION_OFFLINE));
				LogUtil.d(TAG, "Offline !!!");
			} else {
				if (serviceStatus != STATUS_ON)
					setupNotification(ScreenService.this,
							ScreenService.STATUS_ON);
				serviceStatus = STATUS_ON;
				LogUtil.d(TAG, netAddress);
			}
			setWatchdog(mInterval * 1000);
		}
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

			mInterval = Integer.valueOf(settings.getString("interval",
					getString(R.string.interval_default)));
			mURL = settings.getString("url", getString(R.string.url_default));
			doCheck();
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
	}

	private void doCheck() {

		try {

			new NetTask().execute(mURL);

		} catch (Exception e) {
			if (serviceStatus != STATUS_OFF)
				setupNotification(ScreenService.this, ScreenService.STATUS_OFF);
			serviceStatus = STATUS_OFF;
			sendBroadcast(new Intent(ACTION_OFFLINE));
			LogUtil.d(TAG, "Offline !!!");
		}
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

}
