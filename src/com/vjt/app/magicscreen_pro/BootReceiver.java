package com.vjt.app.magicscreen_pro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	private static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			// Log.d(TAG, "Boot completed");
			context.startService(new Intent(context, ScreenService.class));
		}
	}

}
