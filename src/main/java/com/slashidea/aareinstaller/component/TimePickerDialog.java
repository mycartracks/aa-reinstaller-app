package com.slashidea.aareinstaller.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TimePicker;

import com.slashidea.aareinstaller.R;
import com.slashidea.aareinstaller.support.DateHelper;

public class TimePickerDialog extends AlertDialog {

	private static final String TAG = "DateTimePickerDialog";

	private static final String STATE_DATETIME_KEY = "DateTimePickerDialog.datetime";

	private static final String TIME_FORMAT_24 = "24";

	private TimePicker timePicker;

	public TimePickerDialog(Context context, DateTimeAcceptor datetimeAcceptor) {
		this(context, datetimeAcceptor, null, System.currentTimeMillis(), null);
	}

	public TimePickerDialog(Context context, DateTimeAcceptor datetimeAcceptor, String title) {
		this(context, datetimeAcceptor, title, System.currentTimeMillis(), null);
	}

	public TimePickerDialog(Context context, DateTimeAcceptor datetimeAcceptor, long datetime) {
		this(context, datetimeAcceptor, null, datetime, null);
	}

	public TimePickerDialog(Context context, DateTimeAcceptor datetimeAcceptor, String title, long datetime) {
		this(context, datetimeAcceptor, title, datetime, null);
	}

	public TimePickerDialog(Context context, final DateTimeAcceptor datetimeAcceptor, String title, long datetime, final Runnable canceller) {
		super(context);

		final LayoutInflater factory = LayoutInflater.from(context);

		final FrameLayout pickersContainer = (FrameLayout) factory.inflate(R.layout.li_time_picker, null);

		timePicker = (TimePicker) pickersContainer.findViewById(R.id.li_time_picker_time_picker);

		resetDatetime(datetime);

		setIcon(0);
		setTitle(title == null ? context.getText(R.string.pick_time) : title);

		setView(pickersContainer);

		setButton(BUTTON_POSITIVE, context.getText(R.string.ok), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				log("positiveBtnListener: entered");
				datetimeAcceptor.accept(getValidTime());
			}
		});

		setButton(BUTTON_NEGATIVE, context.getText(R.string.cancel), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				log("negativeBtnListener: entered");
				if (canceller != null)
					canceller.run();
			}
		});

		setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				log("cancelListener: entered");
				if (canceller != null)
					canceller.run();
			}
		});

		setCancelable(true);
	}

	@Override
	public Bundle onSaveInstanceState() {
		Bundle outState = super.onSaveInstanceState();
		outState.putLong(STATE_DATETIME_KEY, getValidTime());
		return outState;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		resetDatetime(savedInstanceState.getLong(STATE_DATETIME_KEY));
	}

	public void resetDatetime(long datetime) {
		// honor user settings as to time format
		final String timeFormat = Settings.System.getString(getContext().getContentResolver(), Settings.System.TIME_12_24);
		timePicker.setIs24HourView(TIME_FORMAT_24.equals(timeFormat));

		DateHelper.initTimePicker(timePicker, datetime);
	}

	private long getTime() {
		return DateHelper.getTime(timePicker);
	}

	private long getValidTime() {
		return DateHelper.getValidTime(timePicker);
	}

	private void log(String msg) {
		Log.d(TAG, msg);
	}

	public interface DateTimeAcceptor {
		public void accept(long datetime);
	}
}
