package alon.com.shifter.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;

public class Activity_Shifter_Manager_Settings extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mgr_settings);

        TAG = "Shifter_Manager_Settings";
        getUtil(this);

        setupUI();
    }

    @Override
    protected void setupUI() {
        Button mBack = (Button) findViewById(R.id.MGR_S_back);
        Button mShiftSetting = (Button) findViewById(R.id.MGR_S_shift_settings);
        Button mSpecSettings = (Button) findViewById(R.id.MGR_S_spec_settings);
        Button mShiftHours = (Button) findViewById(R.id.MGR_S_shift_hours);

        mShiftSetting.setOnClickListener(this);
        mSpecSettings.setOnClickListener(this);
        mShiftHours.setOnClickListener(this);
        mBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Bundle extras = new Bundle();
        switch (v.getId()) {
            case R.id.MGR_S_back:
                mUtil.changeScreen(this, Activity_Shifter_Main_Manager.class, extras);
                break;
            case R.id.MGR_S_shift_settings:
                extras.putSerializable(Strings.FILE_SHIFT_OBJECT, mUtil.readObject(this, Strings.FILE_SHIFT_OBJECT));
                extras.putBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON, true);
                mUtil.changeScreen(this, Activity_Select_Shifts_For_Week.class, extras);
                break;
            case R.id.MGR_S_spec_settings:
                extras.putSerializable(Strings.FILE_SPEC_SETTINGS_OBJECT, mUtil.readObject(this, Strings.FILE_SPEC_SETTINGS_OBJECT));
                extras.putBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON, true);
                mUtil.changeScreen(this, Activity_Special_Settings.class, extras);
                break;
            case R.id.MGR_S_shift_hours:
                extras.putSerializable(Strings.FILE_SHIFT_HOURS_OBJECT, mUtil.readObject(this, Strings.FILE_SHIFT_HOURS_OBJECT));
                extras.putBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON, true);
                mUtil.changeScreen(this, Activity_Shift_Hour_Setting.class);
                break;
        }
    }
}
