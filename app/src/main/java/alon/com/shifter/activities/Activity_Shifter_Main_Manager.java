package alon.com.shifter.activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.utils.FirebaseUtil;
import alon.com.shifter.wrappers.FinishableTaskWrapper;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_DIALOG_FRAGMENT_ID;

public class Activity_Shifter_Main_Manager extends BaseActivity {

    private String mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = "Shifter_Main_Manager";
        getUtil(this);

        FinishableTaskWrapper task = new FinishableTaskWrapper() {
            @Override
            public void onFinish() {
                ((DialogFragment) getWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID)).dismiss();
                if ((boolean) mUtil.readPref(Activity_Shifter_Main_Manager.this, Pref_Keys.MGR_SEC_SCHEDULE_SET, false)) {
                    setContentView(R.layout.activity_shifter_manager);
                    setupUI();
                } else
                    mUtil.changeScreen(Activity_Shifter_Main_Manager.this, Activity_Select_Shifts_For_Week.class);
            }
        };
        task.setWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID, mUtil.generateStandbyDialog(this));
        addGateOpenListener(Fc_Keys.SHIFT_SCHEDULE_SETTINGS_PULLED, task);
    }

    @Override
    protected void setupUI() {
        if (getIntent().getExtras() != null)
            mSettings = getIntent().getExtras().getString(Strings.FILE_SPEC_SETTINGS_OBJECT, Strings.NULL);
        if (mSettings != null)
            mUtil.writeObject(this, Strings.FILE_SPEC_SETTINGS_OBJECT, mSettings);

        Button shiftSetter = (Button) findViewById(R.id.MGR_arraignment);
        Button userManager = (Button) findViewById(R.id.MGR_accmgr);
        Button phonebook = (Button) findViewById(R.id.MGR_phone_book);
        Button mgrSettings = (Button) findViewById(R.id.MGR_settings);

        Button disconnect = (Button) findViewById(R.id.MGR_logout);

        TextView helloMsgView = (TextView) findViewById(R.id.MGR_hello_msg);

        shiftSetter.setOnClickListener(this);
        userManager.setOnClickListener(this);
        mgrSettings.setOnClickListener(this);
        phonebook.setOnClickListener(this);

        disconnect.setOnClickListener(this);

        String helloMsg = getString(R.string.ui_hello_msg) + FirebaseUtil.getUser().getName() + ".";
        helloMsgView.setText(helloMsg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.MGR_arraignment:
                mUtil.changeScreen(this, Activity_Shifter_Manager_Arrangement.class);
                break;
            case R.id.MGR_accmgr:
                mUtil.changeScreen(this, Activity_Shifter_Manager_AccMgr.class);
                break;
            case R.id.MGR_settings:
                mUtil.changeScreen(this, Activity_Shifter_Manager_Settings.class);
                break;
            case R.id.MGR_phone_book:
                mUtil.changeScreen(this, Activity_Shifter_User_Phonebook.class);
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
