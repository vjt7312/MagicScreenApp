package com.vjt.app.magicscreen;

import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {

	private static SparseBooleanArray isSelected;
	private List<ApplicationInfo> appsList;
	private Context context;
	private PackageManager packageManager;
	private int checkedCount;

	public ApplicationAdapter(Context context, int textViewResourceId,
			List<ApplicationInfo> appsList) {
		super(context, textViewResourceId, appsList);
		this.context = context;
		this.appsList = appsList;
		isSelected = new SparseBooleanArray();
		packageManager = context.getPackageManager();
		initData();
	}

	private void initData() {

		for (int i = 0; i < appsList.size(); i++) {
			getIsSelected().put(i, false);
		}

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		String app_data = settings.getString("app_data", "");
		if (app_data != null && app_data.length() > 0) {
			StringTokenizer st = new StringTokenizer(app_data, ":");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				for (int i = 0; i < appsList.size(); i++) {
					ApplicationInfo data = appsList.get(i);
					if (data.packageName.equals(token)) {
						getIsSelected().put(i, true);
						checkedCount++;
					}
				}
			}
		}
	}

	public void saveData() {
		String app_data = "";

		for (int i = 0; i < getIsSelected().size(); i++) {
			if (getIsSelected().get(i)) {
				ApplicationInfo data = appsList.get(i);
				app_data += data.packageName + ":";
			}
		}
		if (app_data != null) {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("app_data", app_data);
			editor.commit();
		}
	}

	public int getChecked() {
		return checkedCount;
	}

	public static SparseBooleanArray getIsSelected() {
		return isSelected;
	}

	public static void setIsSelected(SparseBooleanArray isSelected) {
		ApplicationAdapter.isSelected = isSelected;
	}

	@Override
	public int getCount() {
		return ((null != appsList) ? appsList.size() : 0);
	}

	@Override
	public ApplicationInfo getItem(int position) {
		return ((null != appsList) ? appsList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.snippet_list_row,
					null);
			holder.appName = (TextView) convertView.findViewById(R.id.app_name);
			holder.packageName = (TextView) convertView
					.findViewById(R.id.app_paackage);
			holder.ic = (ImageView) convertView.findViewById(R.id.app_icon);
			holder.cb = (CheckBox) convertView
					.findViewById(R.id.setting_checkbox);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ApplicationInfo data = appsList.get(position);
		if (null != data) {
			holder.appName.setText(data.loadLabel(packageManager));
			holder.packageName.setText(data.packageName);
			holder.ic.setImageDrawable(data.loadIcon(packageManager));
			holder.cb.setChecked(getIsSelected().get(position));
		}
		return convertView;
	}

	static final class ViewHolder {
		TextView appName;
		TextView packageName;
		ImageView ic;
		CheckBox cb;
	}
};
