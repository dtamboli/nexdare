/*
 *     Copyright 2016 Makoto Consulting Group, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.nexdare;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class DateTimePickerFragment extends AbstractDialogFragment {

    public static final String DIALOG_TAG = DateTimePickerFragment.class.getName();

    public static final String RESULT_DATE_TIME = "result." + DateTimePickerFragment.class.getName();
    public static final String TIME = "Time";
    public static final String DATE = "Date";
    public static final String BOTH = "BOTH";

    private static final String TAG = DateTimePickerFragment.class.getSimpleName();

    private Date mDate;
    private String mDateType;
    private String mDateOrTimeChoice;

    /**
     * Maintain a reference to the DatePicker to work around a bug in Android 5.
     * The OnWhatever() listener does not work, so whenver we need to pull the values
     * in the Pickers, we have to reference them directly. We do not, however, need
     * to save these in the Bundle because they can always be recreated if necessary
     * (on device rotation, say).
     */
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;

    /**
     * This interface is implemented by the caller, if it wants the result delivered
     * this way. Otherwise, onActivityResult() will be used.
     */
    public interface ResultHandler extends Serializable {
        void setDate(Date result);
    }

    /**
     * The ResultHandler (if used). Basically, this is so we can use this DialogFragment
     * directly from an Activity (rather than a Fragment).
     */
    private ResultHandler mResultHandler;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Pull the date out of the Fragment Arguments
        processFragmentArguments();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_date_time, null);

        // Spinner to choose either Date or Time to edit
        final Spinner dateTimeSpinner = (Spinner) view.findViewById(R.id.spinner_date_time_choice);
        mDatePicker = (DatePicker) view.findViewById(R.id.date_picker);
        mTimePicker = (TimePicker) view.findViewById(R.id.time_picker);
        // Note: the OnDateChangedListener does not work in Android 5 when using the
        /// super cool material style of calendar, which is really slick.
        mDatePicker.init(year, month, day, null);

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteOfHour = calendar.get(Calendar.MINUTE);
        //noinspection deprecation
        mTimePicker.setCurrentHour(hourOfDay);
        //noinspection deprecation
        mTimePicker.setCurrentMinute(minuteOfHour);

        configureDateTimeSpinner(dateTimeSpinner);
        // Now show the Dialog in all its glory!
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.choose_date_or_time)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String METHOD = "onClick(" + dialog + ", " + which + "): ";
                        if (getTargetFragment() == null && mResultHandler == null) {
                            Log.e(TAG, "Both Target Fragment and ResultHandler are null!");
                        } else {
                            //noinspection deprecation
                            mDate = computeDateFromComponents(
                                    mDatePicker.getYear(),
                                    mDatePicker.getMonth(),
                                    mDatePicker.getDayOfMonth(),
                                    mTimePicker.getCurrentHour(),
                                    mTimePicker.getCurrentMinute());
                            if (mResultHandler == null) {
                                Intent intent = new Intent();
                                intent.putExtra(RESULT_DATE_TIME, mDate);
                                getTargetFragment().onActivityResult(
                                        getTargetRequestCode(),
                                        Activity.RESULT_OK,
                                        intent);
                                Log.d(TAG, METHOD + "Sending result back to the caller...:" +
                                        new LocalDateTime(mDate.getTime()).toString("MM/dd/yyyy hh:mm a"));
                            } else {
                                mResultHandler.setDate(mDate);
                            }
                        }
                    }
                })
                .create();
    }

    @Override
    protected void restoreInstanceState(Bundle savedInstanceState) {
        // Nothing to do
    }

    @Override
    protected void saveInstanceState(Bundle outState) {
        // Nothing to do
    }

    private void configureDateTimeSpinner(final Spinner dateTimeSpinner) {
        List<String> choices = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Objects.equals(DATE, mDateOrTimeChoice)) {
                choices.add(computeChoice(DATE));
            } else if (Objects.equals(TIME, mDateOrTimeChoice)) {
                choices.add(computeChoice(TIME));
            } else {
                choices.add(computeChoice(TIME));
                choices.add(computeChoice(DATE));
            }
        }
        dateTimeSpinner.setAdapter(
                new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        choices));
        dateTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choice = (String) dateTimeSpinner.getAdapter().getItem(position);
                if (choice.equalsIgnoreCase(computeChoice(DATE))) {
                    // Make the DatePicker visible
                    mDatePicker.setVisibility(View.VISIBLE);
                    mTimePicker.setVisibility(View.GONE);
                } else {
                    // Make the TimePicker visible
                    mTimePicker.setVisibility(View.VISIBLE);
                    mDatePicker.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Select initial choice if neither preferred.
        if (mDateOrTimeChoice == null) {
            dateTimeSpinner.setSelection(choices.indexOf(computeChoice(TIME)));
        } else {
            dateTimeSpinner.setSelection(choices.indexOf(computeChoice(mDateOrTimeChoice)));
        }
    }

    private String computeChoice(String baseChoice) {
        return mDateType + " " + baseChoice;
    }

    @Override
    protected void processFragmentArguments() {
        mDate = (Date) getArguments().getSerializable(FragmentFactory.FRAG_ARG_DATE);
        // Sanity check
        if (mDate == null) {
            throw new RuntimeException("Fragment argument (" + FragmentFactory.FRAG_ARG_DATE + ") cannot be null!");
        }
        mDateType = (String) getArguments().getSerializable(FragmentFactory.FRAG_ARG_DATE_TYPE);
        // Sanity check
        if (mDateType == null || mDateType.isEmpty()) {
            throw new RuntimeException("Fragment argument (" + FragmentFactory.FRAG_ARG_DATE_TYPE + ") cannot be null!");
        }

        mDateOrTimeChoice = (String) getArguments().getSerializable(FragmentFactory.FRAG_ARG_DATETIME_PICKER_CHOICE);

        mResultHandler = (ResultHandler)getArguments().getSerializable(FragmentFactory.FRAG_ARG_DATETIME_PICKER_RESULT_HANDLER);
    }

    private Date computeDateFromComponents(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour) {
        final String METHOD = "computeDateFromComponents(" + year + "," + monthOfYear + "," + dayOfMonth + "," + hourOfDay + "," + minuteOfHour + ")";
        Calendar changedDateCalendar = Calendar.getInstance();
        changedDateCalendar.set(Calendar.YEAR, year);
        changedDateCalendar.set(Calendar.MONTH, monthOfYear);
        changedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        changedDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        changedDateCalendar.set(Calendar.MINUTE, minuteOfHour);
        changedDateCalendar.set(Calendar.SECOND, 0);
        changedDateCalendar.set(Calendar.MILLISECOND, 0);
        Date ret = changedDateCalendar.getTime();
        Log.d(TAG, METHOD + "Returning date: " + ret);
        return ret;
    }
}
