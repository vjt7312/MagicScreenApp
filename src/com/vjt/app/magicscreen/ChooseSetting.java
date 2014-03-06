package com.vjt.app.magicscreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.vjt.app.magicscreen.ApplicationAdapter.ViewHolder;

public class ChooseSetting extends Activity {

	private static final String TAG = "ChooseSetting";

	private ListView lv;
	private TextView tv;
	private PackageManager packageManager;
	private List<ApplicationInfo> applist;
	private ApplicationAdapter listadaptor;
	private int checkNum;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d(TAG, "onCreate");
		packageManager = getPackageManager();

		setContentView(R.layout.choose_setting);
		lv = (ListView) findViewById(R.id.list);
		tv = (TextView) findViewById(R.id.count);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ViewHolder holder = (ViewHolder) arg1.getTag();
				holder.cb.toggle();
				ApplicationAdapter.getIsSelected().put(arg2,
						holder.cb.isChecked());
				if (holder.cb.isChecked() == true) {
					checkNum++;
				} else {
					checkNum--;
				}
				refreshCount();
			}
		});

		new LoadApplications().execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtil.d(TAG, "onDestroy");

		listadaptor.saveData();
	}

	private List<ApplicationInfo> checkForLaunchIntent(
			List<ApplicationInfo> list) {
		ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
		for (ApplicationInfo info : list) {
			try {
				if (null != packageManager
						.getLaunchIntentForPackage(info.packageName)) {
					applist.add(info);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return applist;
	}

	private class LoadApplications extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress = null;

		@Override
		protected Void doInBackground(Void... params) {
			applist = checkForLaunchIntent(packageManager
					.getInstalledApplications(PackageManager.GET_META_DATA));
			Collections.sort(applist,
					new ApplicationInfo.DisplayNameComparator(packageManager));
			listadaptor = new ApplicationAdapter(ChooseSetting.this,
					R.layout.snippet_list_row, applist);

			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Void result) {
			lv.setAdapter(listadaptor);
			lv.setFastScrollEnabled(true);
			lv.setFastScrollAlwaysVisible(true);
			checkNum = listadaptor.getChecked();
			refreshCount();
			progress.dismiss();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(ChooseSetting.this, null,
					getString(R.string.action_load));
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting_main, menu);
		return true;
	}

	private void doMarkAll() {
		for (int i = 0; i < applist.size(); i++) {
			ApplicationAdapter.getIsSelected().put(i, true);
		}
		checkNum = applist.size();
		dataChanged();
	}

	private void doUnMarkAll() {
		for (int i = 0; i < applist.size(); i++) {
			ApplicationAdapter.getIsSelected().put(i, false);
		}
		checkNum = 0;
		dataChanged();
	}

	private void dataChanged() {
		listadaptor.notifyDataSetChanged();
		refreshCount();
	}

	private void refreshCount() {
		tv.setText(getString(R.string.items) + checkNum);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mark_all:
			doMarkAll();
			break;
		case R.id.unmark_all:
			doUnMarkAll();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
