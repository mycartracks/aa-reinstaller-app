package com.slashidea.aareinstaller.receiver;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.googlecode.androidannotations.annotations.EReceiver;
import com.slashidea.aareinstaller.MainConstants;
import com.slashidea.aareinstaller.support.Utils;

/**
 * @author klobusnikp
 * 
 */
@EReceiver
public class StartStopScheduleBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = StartStopScheduleBroadcastReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "StartStopScheduleBroadcastReceiver.onReceive");

		String action = intent.getAction();

		if (action != null) {

			if (action.equals(MainConstants.START_STOP_SCHEDULE_BROADCAST)) {
				Log.d(TAG, "received START_STOP_SCHEDULE_BROADCAST!");
				startUpdateOrStopInTime(context);

			} else if (action.equals(MainConstants.ON_BOOT_SCHEDULE_BROADCAST)) {
				Log.d(TAG, "received ON_BOOT_SCHEDULE_BROADCAST || PACKAGE_REPLACED_SCHEDULE_BROADCAST!");
				startUpdateOrStopInTime(context);
			}
		}
	}

	private void startUpdateOrStopInTime(Context context) {
		Log.d(TAG, "startUpdateOrStopInTime()!");

		final SharedPreferences sharedPreferences = context.getSharedPreferences(MainConstants.SETTINGS_NAME, Context.MODE_PRIVATE);

		Long scheduleTime = Utils.getScheduleInTime(sharedPreferences);

		boolean scheduleInTimeEnabled = Utils.isScheduleInTimeEnabled(sharedPreferences);

		Intent intent = new Intent(context, CheckNewVersionBroadcastReceiver_.class);
		intent.setAction(MainConstants.CHECK_NEW_VERSION_BROADCAST);

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MainConstants.SCHEDULE_IN_TIME_AND_DAY_REQUEST_CODE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (scheduleInTimeEnabled == true) {
			// start or update actual alarm
			Log.d(TAG, "start sync scheduler!");

			Calendar scheduleTimeCalendar = Calendar.getInstance();
			scheduleTimeCalendar.setTimeInMillis(scheduleTime);

			Log.d(TAG, "scheduleTimeCalendar.getTime(): " + scheduleTimeCalendar.getTime());

			Calendar calendar = Calendar.getInstance();

			if (scheduleTimeCalendar.getTimeInMillis() < calendar.getTimeInMillis()) {
				calendar.add(Calendar.DATE, 1);
			}

			calendar.set(Calendar.HOUR_OF_DAY, scheduleTimeCalendar.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, scheduleTimeCalendar.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, 0);

			Log.d(TAG, "calendar.getTime(): " + calendar.getTime());

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			// cancel if already running
			alarmManager.cancel(pendingIntent);

			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

			Utils.setScheduleInTimeMaxRetryActualCount(0, sharedPreferences);

		} else {
			// cancel actual alarm
			Log.d(TAG, "stop sync scheduler!");

			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);

		}

	}

}
