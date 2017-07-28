package alon.com.shifter.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.utils.FirebaseUtil;

public class Activity_Shifter_Main_Manager extends BaseActivity {

    private String mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = "Shifter_Main_Manager";
        getUtil(this);
        if ((boolean) mUtil.readPref(this, Pref_Keys.MGR_SEC_SCHEDULE_SET, false)) {
            setContentView(R.layout.activity_shifter_manager);
            setupUI();
        } else
            mUtil.changeScreen(Activity_Shifter_Main_Manager.this, Activity_Select_Shifts_For_Week.class);
    }

    @Override
    protected void setupUI() {
        if (getIntent().getExtras() != null)
            mSettings = getIntent().getExtras().getString(Strings.FILE_SPEC_SETTINGS_OBJECT, Strings.NULL);
        if (mSettings != null)
            mUtil.writeObject(this, Strings.FILE_SPEC_SETTINGS_OBJECT, mSettings);

        Button mShifts = (Button) findViewById(R.id.MGR_arraignment);
        Button mUserRqs = (Button) findViewById(R.id.MGR_accmgr);
        Button mSettings = (Button) findViewById(R.id.MGR_settings);

        Button mDisconnect = (Button) findViewById(R.id.MGR_logout);

        TextView mHelloMsg = (TextView) findViewById(R.id.MGR_hello_msg);

        mShifts.setOnClickListener(this);
        mUserRqs.setOnClickListener(this);
        mSettings.setOnClickListener(this);

        mDisconnect.setOnClickListener(this);

        String helloMsg = getString(R.string.ui_hello_msg) + FirebaseUtil.getUser().getName() + ".";
        mHelloMsg.setText(helloMsg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.MGR_arraignment:

                break;
            case R.id.MGR_accmgr:
                mUtil.changeScreen(this, Activity_Shifter_Manager_AccMgr.class);
                break;
            case R.id.MGR_settings:
                mUtil.changeScreen(this, Activity_Shifter_Manager_Settings.class);
                break;
            case R.id.MGR_logout:
                FirebaseUtil.getFirebaseAuth().signOut();
                mUtil.writePref(this, Pref_Keys.LOG_PERMA_LOGIN, false);
                mUtil.writePref(this, Pref_Keys.LOG_PERMA_LOGIN_EMAIL, Strings.NULL);
                mUtil.writePref(this, Pref_Keys.LOG_PERMA_LOGIN_PASS, Strings.NULL);
                mUtil.changeScreen(this, Activity_Login.class);
                break;
        }
    }
}
