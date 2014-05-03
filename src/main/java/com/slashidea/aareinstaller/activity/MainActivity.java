package com.slashidea.aareinstaller.activity;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.slashidea.aareinstaller.MainConstants;
import com.slashidea.aareinstaller.R;
import com.slashidea.aareinstaller.support.Utils;
import com.stericson.RootTools.RootTools;

@EActivity(R.layout.li_main)
@OptionsMenu(R.menu.menu_main)
public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getName();

	private SharedPreferences sharedPreferences;

	@ViewById(R.id.li_main_output)
	TextView outputTextView;

	@ViewById(R.id.li_main_activate)
	Button activateButton;

	@ViewById(R.id.li_main_deactivate)
	Button deactivateButton;

	@ViewById(R.id.activate_settings_textview)
	TextView activateSettingsTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPreferences = getApplicationContext().getSharedPreferences(MainConstants.SETTINGS_NAME, Context.MODE_PRIVATE);

	}

	@Override
	public void onResume() {
		super.onResume();

		updateActualSettingsInfo();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@AfterViews
	void afterViews() {
		Log.d(TAG, "afterViews()");

		// Check BussyBox
		if (!RootTools.isBusyboxAvailable()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(getString(R.string.no_busybox_title));

			// build view from layout
			LayoutInflater factory = LayoutInflater.from(this);
			final View dialogView = factory.inflate(R.layout.li_no_busybox_dialog, null);
			builder.setView(dialogView);

			builder.setNeutralButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}

		// Check for root on device and call su binary
		try {
			if (!RootTools.isAccessGiven()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(getString(R.string.no_root_title));

				// build view from layout
				LayoutInflater factory = LayoutInflater.from(this);
				final View dialogView = factory.inflate(R.layout.li_no_root_dialog, null);
				builder.setView(dialogView);

				builder.setNeutralButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Unable to check root permission!", e);
		}
	}

	@Click(R.id.li_main_activate)
	void activateButtonClicked() {
		showDeactivateButton();

		Utils.setScheduleInTimeEnabled(true, sharedPreferences);

		final Intent intent = new Intent(MainConstants.START_STOP_SCHEDULE_BROADCAST);
		sendBroadcast(intent);
	}

	@Click(R.id.li_main_deactivate)
	void deactivateButtonClicked() {
		showActivateButton();

		Utils.setScheduleInTimeEnabled(false, sharedPreferences);

		final Intent intent = new Intent(MainConstants.START_STOP_SCHEDULE_BROADCAST);
		sendBroadcast(intent);
	}

	@OptionsItem(R.id.menu_settings)
	void menuSettingsSelected() {
		Intent mainSettingsIntent = new Intent(this, MainSettingsPreferenceActivity.class);
		startActivity(mainSettingsIntent);
	}

	private void updateActualSettingsInfo() {
		if (sharedPreferences != null) {
			// Activate/Deactivate button
			boolean isScheduleEnabled = Utils.isScheduleInTimeEnabled(sharedPreferences);

			if (isScheduleEnabled == true) {
				showDeactivateButton();

			} else {
				showActivateButton();

			}

			// Activate settings label
			String updateAppPackage = Utils.getUpdateAppPackage(sharedPreferences);

			int currentAppVersion = -1;

			try {
				currentAppVersion = Utils.getAppVersion(updateAppPackage, this);

			} catch (NameNotFoundException e) {
				Log.e(TAG, "Unable to find app, probbably not installed!", e);
			}

			String updateAppUrl = Utils.getUpdateAppUrl(sharedPreferences);
			String updateAppVersionUrl = Utils.getUpdateAppVersionUrl(sharedPreferences);
			long scheduleInTime = Utils.getScheduleInTime(sharedPreferences);
			String scheduleInDays = Utils.getScheduleInDays(sharedPreferences);
			long lastUpdateTime = Utils.getUpdateAppLastTime(sharedPreferences);

			SimpleDateFormat scheduleInTimeDateFormat = new SimpleDateFormat("HH:mm");
			SimpleDateFormat lastUpdateTimeDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

			activateSettingsTextView.setText(Html.fromHtml(getString(R.string.actual_settings_info,
					(updateAppPackage != null ? updateAppPackage : getString(R.string.not_set)),
					(currentAppVersion != -1 ? currentAppVersion : getString(R.string.not_installed)), (updateAppUrl != null ? updateAppUrl
							: getString(R.string.not_set)), (updateAppVersionUrl != null ? updateAppVersionUrl
							: getString(R.string.not_set)),

					getDaysFromScheduleInDay(scheduleInDays),
					(scheduleInTime != -1 ? scheduleInTimeDateFormat.format(new Date(scheduleInTime)) : getString(R.string.not_set)),

					(lastUpdateTime != -1 ? lastUpdateTimeDateFormat.format(new Date(lastUpdateTime)) : getString(R.string.never)))));

		}
	}

	private void showDeactivateButton() {
		deactivateButton.setEnabled(true);
		deactivateButton.setVisibility(View.VISIBLE);

		activateButton.setEnabled(false);
		activateButton.setVisibility(View.GONE);
	}

	private void showActivateButton() {
		activateButton.setEnabled(true);
		activateButton.setVisibility(View.VISIBLE);

		deactivateButton.setEnabled(false);
		deactivateButton.setVisibility(View.GONE);
	}

	private String getDaysFromScheduleInDay(String scheduleInDays) {
		String days = new String();
		String[] scheduleInDaysAsArray = Utils.getSeparatedValuesAsArray(scheduleInDays);

		DateFormatSymbols symbols = new DateFormatSymbols();
		String[] dayNames = symbols.getWeekdays();

		Log.d(TAG, "scheduleInDays: " + scheduleInDays);

		boolean hasValue = false;

		for (int i = 0; i < scheduleInDaysAsArray.length; i++) {
			Log.d(TAG, "scheduleInDaysAsArray[i]: " + scheduleInDaysAsArray[i] + " dayNames[i + 1]: " + dayNames[i + 1]);
			Log.d(TAG, "Boolean.parseBoolean(scheduleInDaysAsArray[i]): " + Boolean.parseBoolean(scheduleInDaysAsArray[i]));
			if (Boolean.parseBoolean(scheduleInDaysAsArray[i]) == true) {

				if (hasValue == true) {
					days += ", " + dayNames[i + 1];
				} else {
					hasValue = true;
					days += dayNames[i + 1];
				}
			}
		}

		return days;
	}

}
