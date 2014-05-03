package com.slashidea.aareinstaller.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.slashidea.aareinstaller.MainConstants;
import com.slashidea.aareinstaller.R;
import com.slashidea.aareinstaller.component.TimePickerDialog;
import com.slashidea.aareinstaller.support.DateHelper;
import com.slashidea.aareinstaller.support.Utils;

public class MainSettingsPreferenceActivity extends PreferenceActivity {

	private static final String TAG = MainSettingsPreferenceActivity.class.getName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Tell it where to read/write preferences
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(MainConstants.SETTINGS_NAME);
		preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);

		// Load the preferences to be displayed
		addPreferencesFromResource(R.xml.preferences);

	}

	@Override
	protected void onResume() {
		super.onResume();

		Preference scheduleInTimePickerPreference = (Preference) findPreference(MainConstants.PREFERENCE_SCHEDULE_IN_TIME_PICKER);
		scheduleInTimePickerPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference arg0) {
				Log.d(TAG, "scheduleInTimePickerPreference click!");

				final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
				Long scheduleTime = Utils.getScheduleInTime(sharedPreferences);

				TimePickerDialog dateTimePickerDialog = new TimePickerDialog(MainSettingsPreferenceActivity.this,
						new TimePickerDialog.DateTimeAcceptor() {
							public void accept(long time) {
								final String formattedDatetime = DateHelper.format(time);

								Log.d(TAG, "formattedDatetime: " + formattedDatetime);
								Utils.setScheduleInTime(time, sharedPreferences);

								// startUpdateOrStopInTime();
								final Intent intent = new Intent(MainConstants.START_STOP_SCHEDULE_BROADCAST);
								sendBroadcast(intent);
							}
						}, scheduleTime);
				dateTimePickerDialog.show();

				return true;
			}
		});

		Preference scheduleInDaysPickerPreference = (Preference) findPreference(MainConstants.PREFERENCE_SCHEDULE_IN_DAY_PICKER);
		scheduleInDaysPickerPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference arg0) {
				Log.d(TAG, "scheduleInDaysPickerPreference click!");

				final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
				String scheduleInDays = Utils.getScheduleInDays(sharedPreferences);
				String[] scheduleInDaysAsArray = Utils.getSeparatedValuesAsArray(scheduleInDays);

				final CharSequence[] itemNames = new CharSequence[7];
				itemNames[0] = getText(R.string.sunday);
				itemNames[1] = getText(R.string.monday);
				itemNames[2] = getText(R.string.tuesday);
				itemNames[3] = getText(R.string.wednesday);
				itemNames[4] = getText(R.string.thursday);
				itemNames[5] = getText(R.string.friday);
				itemNames[6] = getText(R.string.saturday);

				final boolean[] itemValues = new boolean[7];

				for (int i = 0; i < scheduleInDaysAsArray.length; i++) {
					itemValues[i] = Boolean.parseBoolean(scheduleInDaysAsArray[i]);
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(MainSettingsPreferenceActivity.this);
				builder.setTitle(R.string.pick_days);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Utils.setScheduleInDays(Utils.getSeparatedValuesFromArray(itemValues), sharedPreferences);
					}
				});
				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
					}
				});

				builder.setMultiChoiceItems(itemNames, itemValues, new DialogInterface.OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
						// Do nothing
					}
				});

				AlertDialog alert = builder.create();
				alert.show();

				return true;
			}
		});
	}

}
