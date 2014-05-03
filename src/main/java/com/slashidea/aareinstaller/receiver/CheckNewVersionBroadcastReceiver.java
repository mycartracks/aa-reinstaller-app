package com.slashidea.aareinstaller.receiver;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.PowerManager;
import android.util.Log;

import com.github.ignition.support.http.IgnitedHttp;
import com.github.ignition.support.http.IgnitedHttpResponse;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EReceiver;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.UiThread;
import com.slashidea.aareinstaller.MainConstants;
import com.slashidea.aareinstaller.R;
import com.slashidea.aareinstaller.R.string;
import com.slashidea.aareinstaller.activity.MainActivity_;
import com.slashidea.aareinstaller.exception.DeleteApkException;
import com.slashidea.aareinstaller.exception.DownloadApkException;
import com.slashidea.aareinstaller.exception.InstallApkException;
import com.slashidea.aareinstaller.support.Utils;

/**
 *  
 * WakeLock?
 * http://stackoverflow.com/questions/8768368/alarm-manager-dont-work-after-some-time
 * 
 * @author klobusnikp
 * 
 */
@EReceiver
public class CheckNewVersionBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = CheckNewVersionBroadcastReceiver.class.getName();

	private static final int APK_DOWNLOAD_TIMEOUT = 1 * 60 * 1000;

	@SystemService
	PowerManager powerManager;

	@SystemService
	NotificationManager notificationManager;

	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "CheckNewVersionBroadcastReceiver.onReceive");

		this.context = context;

		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wakeLock.acquire();

		String action = intent.getAction();

		if (action != null) {

			if (action.equals(MainConstants.CHECK_NEW_VERSION_BROADCAST)) {
				Log.d(TAG, "recieved CHECK_NEW_VERSION_BROADCAST!");

				SharedPreferences sharedPreferences = context.getSharedPreferences(MainConstants.SETTINGS_NAME, Context.MODE_PRIVATE);

				String scheduleInDays = Utils.getScheduleInDays(sharedPreferences);

				String[] scheduleInDaysAsArray = Utils.getSeparatedValuesAsArray(scheduleInDays);

				final boolean[] itemValues = new boolean[7];

				for (int i = 0; i < scheduleInDaysAsArray.length; i++) {
					itemValues[i] = Boolean.parseBoolean(scheduleInDaysAsArray[i]);
				}

				Calendar calendar = Calendar.getInstance();
				int day = calendar.get(Calendar.DAY_OF_WEEK);

				Log.d(TAG, "day: " + day + "itemValues[day-1]: " + itemValues[day - 1]);

				if (itemValues[day - 1] == true) {
					Log.d(TAG, "Check new version!");

					String appPackage = Utils.getUpdateAppPackage(sharedPreferences);
					String updateAppVersionUrl = Utils.getUpdateAppVersionUrl(sharedPreferences);

					try {
						int currentVersion = Utils.getAppVersion(appPackage, context);
						Log.d(TAG, "Actual app version: " + currentVersion);

						// Do in background
						checkVersionFromUrl(currentVersion, appPackage, updateAppVersionUrl, sharedPreferences);

					} catch (NameNotFoundException e) {
						Log.e(TAG, "Unable to find app, probbably not installed!", e);
						showNotification(context.getString(R.string.app_name),
								context.getString(R.string.notification_unable_find_package, appPackage));

					}

					// Set repeating interval!
					int maxRetryCount = Utils.getScheduleInTimeMaxRetry(sharedPreferences);
					int actualRetryCount = Utils.getScheduleInTimeMaxRetryActualCount(sharedPreferences);

					Log.d(TAG, "actualRetryCount: " + actualRetryCount);

					if (actualRetryCount < maxRetryCount) {

						calendar.add(Calendar.HOUR_OF_DAY, 1);
						// calendar.add(Calendar.MINUTE, 1);

						Intent retryIntent = new Intent(context, CheckNewVersionBroadcastReceiver.class);
						retryIntent.setAction(MainConstants.CHECK_NEW_VERSION_BROADCAST);

						final PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
								MainConstants.SCHEDULE_IN_TIME_AND_DAY_RETRY_REQUEST_CODE, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT);

						AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

						Utils.setScheduleInTimeMaxRetryActualCount(actualRetryCount + 1, sharedPreferences);

					}

				}
			}

		}

		wakeLock.release();

	}

	@Background
	void checkVersionFromUrl(int currentVersion, String appPackage, String updateAppVersionUrl, SharedPreferences sharedPreferences) {

		String updateAppUrl = Utils.getUpdateAppUrl(sharedPreferences);

		try {
			IgnitedHttp ignitedHttp = new IgnitedHttp();
			// On fail throws exception
			IgnitedHttpResponse ignitedHttpResponse = ignitedHttp.get(updateAppVersionUrl).retries(3).send();

			int currentWebVersion = Integer.parseInt(ignitedHttpResponse.getResponseBodyAsString().trim());

			Log.d(TAG, "Current app version on web: " + currentWebVersion);

			if (currentVersion < currentWebVersion) {
				Log.d(TAG, "New app version is out, downloading...");

				// In background
				downloadAndInstallApk(updateAppUrl, context);

				Utils.setUpdateAppLastTime(System.currentTimeMillis(), sharedPreferences);

				showNotification(context.getString(R.string.app_name),
						context.getString(R.string.notification_install_success, appPackage, currentWebVersion));

			} else {
				Log.d(TAG, "Installed app is up to date, do nothing!");
			}

		} catch (ConnectException e) {
			Log.e(TAG, "Unable to download actual app version from url!", e);
			showNotification(context.getString(R.string.app_name), context.getString(R.string.notification_unable_download_version));

		} catch (IOException e) {
			Log.e(TAG, "Unable to get response from version url!", e);
			showNotification(context.getString(R.string.app_name),
					context.getString(R.string.notification_unable_download_version, updateAppVersionUrl));

		} catch (NumberFormatException e) {
			Log.e(TAG, "Unable to parse number from version url!", e);
			showNotification(context.getString(R.string.app_name),
					context.getString(R.string.notification_unable_parse_version, updateAppVersionUrl));

		}
	}

	@Background
	void downloadAndInstallApk(String appUrl, Context context) {
		try {

			String tempDirPath = context.getCacheDir().getAbsolutePath();
			String tempFileName = "temp_" + System.currentTimeMillis() + ".apk";

			Log.d(TAG, "Deleting old APK files!");
			Utils.deleteOldApk(tempDirPath, "temp_*.apk");

			Log.d(TAG, "tempDirPath: " + tempDirPath);
			Utils.downloadApkToDir(appUrl, tempDirPath, tempFileName, APK_DOWNLOAD_TIMEOUT);

			Log.d(TAG, "tempFileName: " + tempDirPath + "/" + tempFileName);
			Utils.installApk(tempDirPath + "/" + tempFileName);

		} catch (DownloadApkException e) {
			Log.e(TAG, "Failed to download APK from URL!", e); 
			showNotification(context.getString(R.string.app_name),
					context.getString(R.string.notification_unable_downlaod_apk, e.getMessage()));

		} catch (InstallApkException e) {
			Log.e(TAG, "Failed to install new APK!", e);
			showNotification(context.getString(R.string.app_name), context.getString(R.string.notification_failed_install_apk));

		} catch (DeleteApkException e) {
			Log.e(TAG, "Failed to delete old APK file!", e);
			showNotification(context.getString(R.string.app_name), context.getString(R.string.notification_unable_delete_apk));

		}

	} 

	@UiThread
	void showNotification(String title, String text) {  
		Utils.showNotification(MainConstants.MAIN_NOTIFICATION_ID, title, title, text, MainActivity_.class, context, notificationManager);
	}  

}
