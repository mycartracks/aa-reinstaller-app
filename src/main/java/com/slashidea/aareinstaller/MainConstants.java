package com.slashidea.aareinstaller;

import java.util.Calendar;

public class MainConstants {

	public static final String SETTINGS_NAME = "com.slashidea.aareinstaller";

	private static long defaultScheduleInTime;

	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 22);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);

		defaultScheduleInTime = calendar.getTimeInMillis();
	}

	// Default values
	public static final String DEFAULT_UPDATE_APP_PACKAGE = "com.project.example";
	public static final String DEFAULT_UPDATE_APP_URL = "http://app.slashidea.com/example-app.apk";
	public static final String DEFAULT_UPDATE_APP_VERSION_URL = "http://app.slashidea.com/version.txt";

	public static final long DEFAULT_SCHEDULE_IN_TIME = defaultScheduleInTime;
	public static final String DEFAULT_SCHEDULE_IN_DAY = "true;true;true;true;true;true;true";
	public static final int DEFAULT_SCHEDULE_IN_TIME_AND_DAY_MAX_RETRY = 3;

	// Preferences
	public static final String PREFERENCE_UPDATE_APP_PACKAGE = "preference_update_app_package";
	public static final String PREFERENCE_UPDATE_APP_URL = "preference_update_app_url";
	public static final String PREFERENCE_UPDATE_APP_VERSION_URL = "preference_update_app_version_url";
	public static final String PREFERENCE_UPDATE_APP_LAST_TIME = "preference_update_app_last_time";

	public static final String PREFERENCE_SCHEDULE_IN_TIME_PICKER = "preference_schedule_in_time_picker";
	public static final String PREFERENCE_SCHEDULE_IN_DAY_PICKER = "preference_schedule_in_day_picker";

	public static final String PREFERENCE_SCHEDULE_IN_TIME = "preference_schedule_in_time";
	public static final String PREFERENCE_SCHEDULE_IN_DAY = "preference_schedule_in_day";
	public static final String PREFERENCE_SCHEDULE_IN_TIME_AND_DAY_MAX_RETRY = "preference_schedule_in_time_and_day_max_retry";
	public static final String PREFERENCE_SCHEDULE_IN_TIME_AND_DAY_MAX_RETRY_ACTUAL_COUNT = "preference_schedule_in_time_and_day_max_retry_actual_count";
	public static final String PREFERENCE_SCHEDULE_IN_TIME_AND_DAY = "preference_schedule_in_time_and_day";

	// Broadcasts
	public static final int SCHEDULE_IN_TIME_AND_DAY_REQUEST_CODE = 10001;
	public static final int SCHEDULE_IN_TIME_AND_DAY_RETRY_REQUEST_CODE = 10002;

	public static final String CHECK_NEW_VERSION_BROADCAST = "com.slashidea.aareinstaller.CHECK_NEW_VERSION_BROADCAST";
	public static final String START_STOP_SCHEDULE_BROADCAST = "com.slashidea.aareinstaller.START_STOP_SCHEDULE_BROADCAST";
	public static final String ON_BOOT_SCHEDULE_BROADCAST = "android.intent.action.BOOT_COMPLETED";
	public static final String PACKAGE_REPLACED_SCHEDULE_BROADCAST = "android.intent.action.PACKAGE_REPLACED";

	// Notifications
	public static final int MAIN_NOTIFICATION_ID = 20001;

}
