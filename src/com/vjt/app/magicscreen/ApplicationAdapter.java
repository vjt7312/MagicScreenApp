package com.vjt.app.magicscreen;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationAdapter extends ArrayAdapter<ApplicationInfo> {

	private static HashMap<Integer, Boolean> isSelected;
	private List<ApplicationInfo> appsList = null;
	private Context context;
	private PackageManager packageManager;

	public ApplicationAdapter(Context context, int textViewResourceId,
			List<ApplicationInfo> appsList) {
		super(context, textViewResourceId, appsList);
		this.context = context;
		this.appsList = appsList;
		isSelected = new HashMap<Integer, Boolean>();
		packageManager = context.getPackageManager();
		initDate();
	}

	private void initDate() {
		for (int i = 0; i < appsList.size(); i++) {
			getIsSelected().put(i, false);
		}
	}

	public static HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}

	public static void setIsSelected(HashMap<Integer, Boolean> isSelected) {
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
