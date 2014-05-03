package com.slashidea.aareinstaller.support;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.slashidea.aareinstaller.MainConstants;
import com.slashidea.aareinstaller.R;
import com.slashidea.aareinstaller.exception.DeleteApkException;
import com.slashidea.aareinstaller.exception.DownloadApkException;
import com.slashidea.aareinstaller.exception.InstallApkException;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

public class Utils {

	private static final String TAG = Utils.class.getName();

	private static final String FILE_SEPARATOR = System.getProperty("file.separator", "/");

	public static void showNotification(int notificationId, String title, String tickerText, String text, Class<?> intentClass,
			Context context, NotificationManager notificationManager) {

		notificationManager.cancel(notificationId);

		Intent notificationIntent = new Intent(context, intentClass);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		Notification notification = new Notification(R.drawable.ic_main_notification, tickerText, System.currentTimeMillis());
		notification.setLatestEventInfo(context, title, text, contentIntent);
		notification.flags += Notification.FLAG_NO_CLEAR;

		notificationManager.notify(notificationId, notification);

	}

	public static int getAppVersion(String packageName, Context context) throws NameNotFoundException {
		int version = -1;

		PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
		version = packageInfo.versionCode;

		return version;
	}

	public static void deletePackage(String packageName) throws IOException, RootToolsException, TimeoutException {

		Log.d(TAG, "pm uninstall " + packageName);

		List<String> output = RootTools.sendShell(new String[] { "pm uninstall " + packageName }, 0, -1);
		Log.d(TAG, "Output of sendShell commands: " + output.toString());

	}

	public static void installApk(String apkPath) throws InstallApkException {
		try {
			Log.d(TAG, "pm install -r " + apkPath);

			List<String> output = RootTools.sendShell(new String[] { "pm install -r " + apkPath }, 0, -1);

			if (output != null && output.size() > 0) {
				Log.d(TAG, "Output of sendShell commands: " + output.toString());

				int result = Integer.parseInt(output.get(output.size() - 1));

				Log.d(TAG, "result: " + result);

				if (result != 0) {
					throw new InstallApkException("Result: " + result);
				}
			}

		} catch (Exception e) {
			throw new InstallApkException(e);
		}
	}

	public static void deleteOldApk(String tempDirPath, String tempFileName) throws DeleteApkException {
		try {
			Log.d(TAG, "rm -f " + tempDirPath + FILE_SEPARATOR + tempFileName);
			List<String> output = RootTools.sendShell(new String[] { "rm -f " + tempDirPath + FILE_SEPARATOR + tempFileName }, 0, -1);

			if (output != null && output.size() > 0) {
				Log.d(TAG, "Output of sendShell commands: " + output.toString());

				int result = Integer.parseInt(output.get(output.size() - 1));

				Log.d(TAG, "result: " + result);

				if (result != 0) {
					throw new DeleteApkException("Result: " + result);
				}
			}
		} catch (Exception e) {
			throw new DeleteApkException(e);
		}
	}

	public static void downloadApkToDir(String appUrl, String tempDirPath, String tempFileName, int timeout) throws DownloadApkException {
		try {
			Log.d(TAG, "wget -c -O " + tempDirPath + FILE_SEPARATOR + tempFileName + " -T " + timeout + " " + appUrl);
			List<String> output = RootTools.sendShell(new String[] { "wget -c -O " + tempDirPath + FILE_SEPARATOR + tempFileName + " -T "
					+ timeout + " " + appUrl }, 0, -1);

			if (output != null && output.size() > 0) {
				Log.d(TAG, "Output of sendShell commands: " + output.toString());

				int result = Integer.parseInt(output.get(output.size() - 1));

				Log.d(TAG, "result: " + result);

				if (result != 0) {
					switch (result) {
					case 1:
						throw new DownloadApkException("Generic error code");
					case 2:
						throw new DownloadApkException("Parse error for instance");
					case 3:
						throw new DownloadApkException("File I/O error");
					case 4:
						throw new DownloadApkException("Network failure");
					case 5:
						throw new DownloadApkException("SSL verification failure");
					case 6:
						throw new DownloadApkException("Username/password authentication failure");
					case 7:
						throw new DownloadApkException("Protocol errors");
					case 8:
						throw new DownloadApkException("Server issued an error response");

					}
				}
			}

		} catch (Exception e) {
			throw new DownloadApkException(e);
		}
	}

	public static String getScheduleInDays(SharedPreferences sharedPreferences) {
		String scheduleInDays = sharedPreferences
				.getString(MainConstants.PREFERENCE_SCHEDULE_IN_DAY, MainConstants.DEFAULT_SCHEDULE_IN_DAY);

		Log.d(TAG, "scheduleInDays: " + scheduleInDays);

		return scheduleInDays;
	}

	public static void setScheduleInDays(String days, SharedPreferences sharedPreferences) {
		Log.d(TAG, "setting schedule in days: " + days);

		sharedPreferences.edit().putString(MainConstants.PREFERENCE_SCHEDULE_IN_DAY, days).commit();

	}

	public static Long getScheduleInTime(SharedPreferences sharedPreferences) {
		Long scheduleInTime = sharedPreferences.getLong(MainConstants.PREFERENCE_SCHEDULE_IN_TIME, MainConstants.DEFAULT_SCHEDULE_IN_TIME);

		Log.d(TAG, "scheduleInTime: " + scheduleInTime);

		return scheduleInTime;
	}

	public static void setScheduleInTime(Long time, SharedPreferences sharedPreferences) {
		Log.d(TAG, "setting schedule in time: " + time);

		sharedPreferences.edit().putLong(MainConstants.PREFERENCE_SCHEDULE_IN_TIME, time).commit();

	}

	public static String[] getSeparatedValuesAsArray(String separatedValues) {

		if (separatedValues == null || separatedValues.length() == 0) {
			return null;
		}

		String[] separatedValuesArray = separatedValues.split(";");

		if (separatedValuesArray != null && separatedValuesArray.length > 0) {
			for (int i = 0; i < separatedValuesArray.length; i++) {
				separatedValuesArray[i] = separatedValuesArray[i].trim();
			}
		}

		return separatedValuesArray;
	}

	public static String getSeparatedValuesFromArray(boolean[] separatedValuesArray) {
		if (separatedValuesArray == null || separatedValuesArray.length == 0) {
			return null;
		}

		String[] values = new String[separatedValuesArray.length];

		for (int i = 0; i < separatedValuesArray.length; i++) {
			values[i] = String.valueOf(separatedValuesArray[i]);
		}

		return getSeparatedValuesFromArray(values);

	}

	public static String getSeparatedValuesFromArray(String[] separatedValuesArray) {
		if (separatedValuesArray == null || separatedValuesArray.length == 0) {
			return null;
		}

		StringBuffer separatedValuesStringBuffer = new StringBuffer();

		for (String value : separatedValuesArray) {
			if (separatedValuesStringBuffer.length() > 0) {
				separatedValuesStringBuffer.append(";" + value);
			} else {
				separatedValuesStringBuffer.append(value);
			}
		}

		return separatedValuesStringBuffer.toString();
	}

	public static void setScheduleInTimeMaxRetryActualCount(int actualCount, SharedPreferences sharedPreferences) {
		Log.d(TAG, "setting schedule in time actual count: " + actualCount);

		sharedPreferences.edit().putInt(MainConstants.PREFERENCE_SCHEDULE_IN_TIME_AND_DAY_MAX_RETRY_ACTUAL_COUNT, actualCount).commit();
	}

	public static boolean isScheduleInTimeEnabled(SharedPreferences sharedPreferences) {
		boolean scheduleInTimeEnabled = sharedPreferences.getBoolean(MainConstants.PREFERENCE_SCHEDULE_IN_TIME_AND_DAY, false);

		Log.d(TAG, "scheduleInTimeEnabled: " + scheduleInTimeEnabled);

		return scheduleInTimeEnabled;
	}

	public static void setScheduleInTimeEnabled(boolean scheduleInTimeAndDay, SharedPreferences sharedPreferences) {
		Log.d(TAG, "setting schedule in time and day: " + scheduleInTimeAndDay);

		sharedPreferences.edit().putBoolean(MainConstants.PREFERENCE_SCHEDULE_IN_TIME_AND_DAY, scheduleInTimeAndDay).commit();

	}

	public static int getScheduleInTimeMaxRetry(SharedPreferences sharedPreferences) {
		String maxRetry = sharedPreferences.getString(MainConstants.PREFERENCE_SCHEDULE_IN_TIME_AND_DAY_MAX_RETRY,
				String.valueOf(MainConstants.DEFAULT_SCHEDULE_IN_TIME_AND_DAY_MAX_RETRY));
		return Integer.parseInt(maxRetry);
	}

	public static int getScheduleInTimeMaxRetryActualCount(SharedPreferences sharedPreferences) {
		int actualCount = sharedPreferences.getInt(MainConstants.PREFERENCE_SCHEDULE_IN_TIME_AND_DAY_MAX_RETRY_ACTUAL_COUNT, 0);
		return actualCount;
	}

	public static String getUpdateAppUrl(SharedPreferences sharedPreferences) {
		String updateAppUrl = sharedPreferences.getString(MainConstants.PREFERENCE_UPDATE_APP_URL, MainConstants.DEFAULT_UPDATE_APP_URL);
		return updateAppUrl;
	}

	public static long getUpdateAppLastTime(SharedPreferences sharedPreferences) {
		long updateAppLastTime = sharedPreferences.getLong(MainConstants.PREFERENCE_UPDATE_APP_LAST_TIME, -1);
		return updateAppLastTime;
	}

	public static void setUpdateAppLastTime(long updateAppLastTime, SharedPreferences sharedPreferences) {
		Log.d(TAG, "setting updateAppLastTime: " + updateAppLastTime);

		sharedPreferences.edit().putLong(MainConstants.PREFERENCE_UPDATE_APP_LAST_TIME, updateAppLastTime).commit();

	}

	public static String getUpdateAppVersionUrl(SharedPreferences sharedPreferences) {
		String updateAppVersionUrl = sharedPreferences.getString(MainConstants.PREFERENCE_UPDATE_APP_VERSION_URL,
				MainConstants.DEFAULT_UPDATE_APP_VERSION_URL);
		return updateAppVersionUrl;
	}

	public static String getUpdateAppPackage(SharedPreferences sharedPreferences) {
		String updateAppPackage = sharedPreferences.getString(MainConstants.PREFERENCE_UPDATE_APP_PACKAGE,
				MainConstants.DEFAULT_UPDATE_APP_PACKAGE);
		return updateAppPackage;
	}
}
