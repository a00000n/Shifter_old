package alon.com.shifter.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.utils_shift.ShiftHours;

public class Activity_Shift_Hour_Setting extends BaseActivity implements TimePickerDialog.OnTimeSetListener {

    private final int MORN = 1;
    private final int AFR_NON = 2;
    private final int EVENING = 3;
    private final int NIGHT = 4;

    private boolean mChangeText;

    private TimePickerDialog mFromHour;
    private TimePickerDialog mToHour;

    private TextView mMorn;
    private TextView mAfrNon;
    private TextView mEvening;
    private TextView mNight;

    private int mCurrentTime = -1;

    private String mShiftItem = "";

    private ShiftHours mShiftHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getExtras() != null) {
            mChangeText = getIntent().getExtras().getBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON);
            mShiftHours = (ShiftHours) getIntent().getExtras().getSerializable(Strings.FILE_SHIFT_HOURS_OBJECT);

        }
        setContentView(R.layout.activity_shift_hour_setting);

        getUtil(this);
        TAG = "Shifter_Hour_Settings";

        if (mShiftHours == null)
            mShiftHours = (ShiftHours) mUtil.readObject(this, Strings.FILE_SHIFT_HOURS_OBJECT);
        setupUI();
    }


    @Override
    protected void setupUI() {
        mMorn = (TextView) findViewById(R.id.MGR_SHS_morn_title);
        mAfrNon = (TextView) findViewById(R.id.MGR_SHS_afr_non_title);
        mEvening = (TextView) findViewById(R.id.MGR_SHS_evening_title);
        mNight = (TextView) findViewById(R.id.MGR_SHS_night_title);

        Button mMornHours = (Button) findViewById(R.id.MGR_SHS_morn_hour_set);
        Button mAfrNonHours = (Button) findViewById(R.id.MGR_SHS_afr_non_hour_set);
        Button mEveningHours = (Button) findViewById(R.id.MGR_SHS_evening_hour_set);
        Button mNightHours = (Button) findViewById(R.id.MGR_SHS_night_hour_set);

        Button mBack = (Button) findViewById(R.id.MGR_SHS_done);

        if (mChangeText)
            mBack.setText(getString(R.string.back));

        mMornHours.setOnClickListener(this);
        mAfrNonHours.setOnClickListener(this);
        mEveningHours.setOnClickListener(this);
        mNightHours.setOnClickListener(this);
        mBack.setOnClickListener(this);

        if (mShiftHours != null) {
            String mChangedText = getString(R.string.mgr_msg_hour_set);
            for (int i = 1; i < 5; i++)
                if (mShiftHours.getHour(i - 1) != null) {
                    switch (i) {
                        case MORN:
                            mChangedText = getString(R.string.shift_title_morn) + " " + mChangedText;
                            mMorn.setText(mChangedText);
                            break;
                        case AFR_NON:
                            mChangedText = getString(R.string.shift_title_afr_non) + " " + mChangedText;
                            mAfrNon.setText(mChangedText);
                            break;
                        case EVENING:
                            mChangedText = getString(R.string.shift_title_evening) + " " + mChangedText;
                            mEvening.setText(mChangedText);
                            break;
                        case NIGHT:
                            mChangedText = getString(R.string.shift_title_night) + " " + mChangedText;
                            mNight.setText(mChangedText);
                            break;
                    }
                    mChangedText = getString(R.string.mgr_msg_hour_set);
                }
        }

        Calendar mCal = Calendar.getInstance();
        mFromHour = new TimePickerDialog(this, this, mCal.get(Calendar.HOUR_OF_DAY), mCal.get(Calendar.MINUTE), true);
        mToHour = new TimePickerDialog(this, this, mCal.get(Calendar.HOUR_OF_DAY), mCal.get(Calendar.MINUTE), true);

        mFromHour.setTitle(R.string.dialog_shift_hours_title);
        mFromHour.setMessage(getString(R.string.dialog_shift_hours_msg_start));

        mToHour.setTitle(R.string.dialog_shift_hours_title);
        mToHour.setMessage(getString(R.string.dialog_shift_hours_msg_end));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.MGR_SHS_morn_hour_set:
                mCurrentTime = MORN;
                break;
            case R.id.MGR_SHS_afr_non_hour_set:
                mCurrentTime = AFR_NON;
                break;
            case R.id.MGR_SHS_evening_hour_set:
                mCurrentTime = EVENING;
                break;
            case R.id.MGR_SHS_night_hour_set:
                mCurrentTime = NIGHT;
                break;
            case R.id.MGR_SHS_done:
                mUtil.writeObject(this, Strings.FILE_SHIFT_HOURS_OBJECT, mShiftHours);
                if (mChangeText)
                    mUtil.changeScreen(this, Activity_Shifter_Manager_Settings.class);
                else
                    mUtil.changeScreen(this, Activity_Shifter_Main_Manager.class);
                return;
        }
        if (mShiftHours != null && mShiftHours.getHour(mCurrentTime - 1) != null && !mShiftHours.getHour(mCurrentTime - 1).isEmpty()) {
            String from = mShiftHours.getHour(mCurrentTime - 1).split("-")[0];
            int hour = Integer.parseInt(from.split(":")[0]);
            int min = Integer.parseInt(from.split(":")[1]);
            mFromHour.updateTime(hour, min);
        }
        mFromHour.show();
        mFromHour.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShiftItem = "";
                mFromHour.dismiss();
            }
        });
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String shiftTime = (hourOfDay < 10 ? Integer.toString(0) : "") + Integer.toString(hourOfDay) + ":" + (minute < 10 ? Integer.toString(0) : "") + Integer.toString(minute);
        if (mFromHour.isShowing()) {
            mShiftItem = shiftTime + "-";
            mFromHour.dismiss();
            if (mShiftHours != null && mShiftHours.getHour(mCurrentTime - 1) != null && !mShiftHours.getHour(mCurrentTime - 1).isEmpty()) {
                String from = mShiftHours.getHour(mCurrentTime - 1).split("-")[1];
                int hour = Integer.parseInt(from.split(":")[0]);
                int min = Integer.parseInt(from.split(":")[1]);
                mToHour.updateTime(hour, min);
            }
            mToHour.show();
            mToHour.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShiftItem = "";
                    mToHour.dismiss();
                }
            });
        } else if (mToHour.isShowing()) {
            mShiftItem += shiftTime;
            mToHour.dismiss();
            String mChangedText = getString(R.string.mgr_msg_hour_set);
            switch (mCurrentTime) {
                case MORN:
                    mChangedText = getString(R.string.shift_title_morn) + " " + mChangedText;
                    mMorn.setText(mChangedText);
                    break;
                case AFR_NON:
                    mChangedText = getString(R.string.shift_title_afr_non) + " " + mChangedText;
                    mAfrNon.setText(mChangedText);
                    break;
                case EVENING:
                    mChangedText = getString(R.string.shift_title_evening) + " " + mChangedText;
                    mEvening.setText(mChangedText);
                    break;
                case NIGHT:
                    mChangedText = getString(R.string.shift_title_night) + " " + mChangedText;
                    mNight.setText(mChangedText);
                    break;
            }
            if (mShiftHours == null)
                mShiftHours = new ShiftHours();
            mShiftHours.setHour(mCurrentTime - 1, mShiftItem);
        }
    }
}
