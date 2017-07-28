package alon.com.shifter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.wrappers.AsyncWrapper;

import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_ACTIVITY_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_STRING_SHIFT_SUBMISSION_CAP;

/**
 * Created by Alon on 2/8/2017.
 */

public class Activity_Shifter_Manager_SubmissionCap extends BaseActivity {

    private boolean mChangeText = false;

    private TimePicker mDoneHour;
    private Spinner mDaySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifter_mgr_submissioncap);

        getUtil(this);
        TAG = "Shifter_Submission_cap";

        if (getIntent() != null && getIntent().getExtras() != null) {
            mChangeText = getIntent().getExtras().getBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON);
        }

        setupUI();
    }

    @Override
    protected void setupUI() {
        Button done = (Button) findViewById(R.id.MGR_SUB_CAP_done);
        if (mChangeText)
            done.setText(getString(R.string.back));

        mDoneHour = (TimePicker) findViewById(R.id.MGR_SUB_CAP_final_hour);
        mDaySpinner = (Spinner) findViewById(R.id.MGR_SUB_CAP_day_spinner);

        ArrayList<String> dayList = new ArrayList<>();
        for (int i = 1; i <= 7; i++)
            dayList.add(mUtil.getDayString(this, i));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dayList);
        mDaySpinner.setAdapter(adapter);

        //TODO: update to API21 mDoneHour.setMode
        mDoneHour.setIs24HourView(true);

        done.setOnClickListener(this);

        String time = (String) mUtil.readPref(this, Pref_Keys.MGR_SEC_SUBMISSION_CAP, Strings.NULL);
        if (!time.equals(Strings.NULL)) {
            String[] parts = time.split("~");
            mDaySpinner.setSelection(Integer.parseInt(parts[0]) - 1);
            String[] hoursAndMinutes = parts[1].split(":");
            //BACK_COMPAT
            mDoneHour.setCurrentHour(Integer.parseInt(hoursAndMinutes[0]));
            mDoneHour.setCurrentMinute(Integer.parseInt(hoursAndMinutes[1]));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.MGR_SUB_CAP_done) {
            String part = Integer.toString(mDaySpinner.getSelectedItemPosition() + 1) + "~" + Integer.toString(mDoneHour.getCurrentHour()) + ":" + Integer.toString(mDoneHour.getCurrentMinute());
            if (!mUtil.readPref(this, Pref_Keys.MGR_SEC_SUBMISSION_CAP, Strings.NULL).equals(part)) {
                mUtil.writePref(this, Pref_Keys.MGR_SEC_SUBMISSION_CAP, part);
                AsyncWrapper wrapper = new AsyncWrapper() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Linker linker = Linker.getLinker((Activity) getWrapperParam(WRAPPER_ACTIVITY_ID), Linker_Keys.TYPE_UPLOAD_SHIFT_SUBMISSION_CAP);
                            linker.addParam(Linker_Keys.KEY_SHIFT_SUBMISSION_CAP_STRING, getWrapperParam(WRAPPER_STRING_SHIFT_SUBMISSION_CAP).toString());
                            linker.execute();
                        } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                wrapper.setWrapperParam(WRAPPER_ACTIVITY_ID, this).setWrapperParam(WRAPPER_STRING_SHIFT_SUBMISSION_CAP, part);
                wrapper.execute();
            }
            if (mChangeText)
                mUtil.changeScreen(this, Activity_Shifter_Manager_Settings.class);
            else
                mUtil.changeScreen(this, Activity_Shifter_Main_Manager.class);
        }
    }
}
