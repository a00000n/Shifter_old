package alon.com.shifter.activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.dialog_fragments.ErrorDialogFragment;
import alon.com.shifter.utils_shift.SpecSettings;
import alon.com.shifter.utils.SpecSettingsValidator;
import alon.com.shifter.views.SpecSettingView;
import alon.com.shifter.wrappers.AsyncWrapper;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;
import static alon.com.shifter.wrappers.WrapperBase.SPEC_SETTINGS_ID;
import static alon.com.shifter.wrappers.WrapperBase.STRING_SHIFT_ID;
import static alon.com.shifter.wrappers.WrapperBase.STRING_SPEC_SETTINGS_ID;

public class Activity_Special_Settings extends BaseActivity {

    private LinearLayout mLayout;
    private Button mDone;

    private boolean mChangeText = false;

    private SpecSettings mSpecSettingsObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_settings);

        getUtil(this);
        TAG = "Shifter_SpecialSettings";

        if (getIntent() != null && getIntent().getExtras() != null) {
            try {
                mSpecSettingsObj = (SpecSettings) getIntent().getExtras().getSerializable(Strings.FILE_SPEC_SETTINGS_OBJECT);
            } catch (ClassCastException ex) {
                mChangeText = getIntent().getExtras().getBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON);
                setupUI();
                Log.i(TAG, "onCreate: Failed finding spec settings object file.");
                return;
            }
            mChangeText = getIntent().getExtras().getBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON);
        }
        if (mSpecSettingsObj == null)
            mSpecSettingsObj = (SpecSettings) mUtil.readObject(this, Strings.FILE_SPEC_SETTINGS_OBJECT);

        setupUI();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void setupUI() {
        String mCompleteSpecSettingInfo = mUtil.readPref(this, Pref_Keys.MGR_SEC_COMPLETE_SPEC_SETTINGS, Strings.NULL).toString();
        String[] mSpecSettings = mCompleteSpecSettingInfo.split(";");

        mLayout = (LinearLayout) findViewById(R.id.SS_options);
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        String rests = mUtil.readPref(this, Pref_Keys.MGR_SEC_SPEC_SETTINGS_RESTRICTIONS, Strings.NULL).toString();
        String[] restrictions = rests.split(";");
        for (int i = 1; i < mSpecSettings.length; i++) {
            String setting = mSpecSettings[0].split("~")[i - 1];
            SpecSettingView mView = constSettingView(setting, mSpecSettings[i], restrictions[i - 1].split("~")[1]);
            if (mSpecSettingsObj != null)
                if (mSpecSettingsObj.containsHeader(setting)) {
                    mView.setHeaderChecked(true);
                    for (String str : mSpecSettingsObj.getChildrenArrayList(setting))
                        mView.setChildChecked(str, true);
                }
            mLayout.addView(mView, mParams);
        }
        mDone = (Button) findViewById(R.id.SS_done);
        if (mChangeText)
            mDone.setText(getString(R.string.back));
        mDone.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mDone.getId()) {
            ArrayList<String> mItems = new ArrayList<>();
            for (int i = 0; i < mLayout.getChildCount(); i++) {
                String item = "";
                View mView = mLayout.getChildAt(i);
                if (mView instanceof SpecSettingView)
                    if (((SpecSettingView) mView).isActive())
                        if (((SpecSettingView) mView).isAnyChildChecked()) {
                            item += ((SpecSettingView) mLayout.getChildAt(i)).getHeader() + "=";
                            item += ((SpecSettingView) mLayout.getChildAt(i)).getChildrenSelected() + "~";
                        }
                int childAmount = item.lastIndexOf("~");
                if (!item.isEmpty())
                    mItems.add(item.substring(0, childAmount == -1 ? item.lastIndexOf("=") : childAmount));
            }

            SpecSettings settings = SpecSettings.generateFromList(mItems);
            String specSettingsString = mUtil.readPref(this, Pref_Keys.MGR_SEC_COMPLETE_SPEC_SETTINGS, Strings.NULL).toString();
            String shifts = mUtil.readPref(this, Pref_Keys.USR_SHIFT_SCHEDULE, Strings.NULL).toString();

            if (specSettingsString.equals(Strings.NULL) || shifts.equals(Strings.NULL)) {
                alon.com.shifter.wrappers.FinishableTaskWrapper wrapper = new alon.com.shifter.wrappers.FinishableTaskWrapper() {
                    int count = 0;

                    @Override
                    public void onFinish() {
                        count++;
                        if (count == 2) {
                            finishOnClick((SpecSettings) getWrapperParam(SPEC_SETTINGS_ID), (String) getWrapperParam(STRING_SPEC_SETTINGS_ID), (String) getWrapperParam(STRING_SHIFT_ID));
                        }
                    }
                };
                wrapper.setWrapperParam(SPEC_SETTINGS_ID, settings);
                wrapper.setWrapperParam(STRING_SPEC_SETTINGS_ID, specSettingsString);
                wrapper.setWrapperParam(STRING_SHIFT_ID, shifts);

                addGateOpenListener(Fc_Keys.SHIFT_SCHEDULE_SETTINGS_PULLED, wrapper);
                addGateOpenListener(Fc_Keys.SPEC_SETTINGS_EXPANDABLE_INFO_PULLED, wrapper);
            } else {
                finishOnClick(settings, specSettingsString, shifts);
            }
        }
    }

    private void finishOnClick(SpecSettings specSettings, String specSettingsString, String shifts) {
        boolean[] isValid = SpecSettingsValidator.isValid(specSettings, specSettingsString, shifts, this);

        String err = "";

        for (int i = 0; i < 3; i++)
            if (!isValid[i])
                switch (i) {
                    case 0:
                        err += getString(R.string.err_spec_settings_certain_day) + "\n";
                        break;
                    case 1:
                        err += getString(R.string.err_spec_settings_certain_shift) + "\n";
                        break;
                    case 2:
                        err += getString(R.string.err_spec_settings_min_shifts) + "\n";
                        break;
                }

        if (!err.isEmpty()) {
            ErrorDialogFragment dialog = new ErrorDialogFragment();
            dialog.setTitle(getString(R.string.dialog_failed_validation_spec_settings));
            dialog.setMsg(err);
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            dialog.show(getFragmentManager(), DialogFragment_Keys.SPEC_SETTINGS_VALIDATION_ERROR);
            return;
        }
        AsyncWrapper wrapper = new AsyncWrapper() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Linker linker = Linker.getLinker(Activity_Special_Settings.this, Linker_Keys.TYPE_UPLOAD_SPEC_SETTINGS);
                    linker.addParam(Linker_Keys.KEY_SPEC_SETTINGS, getWrapperParam(SPEC_SETTINGS_ID));
                    linker.execute();
                } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        wrapper.setWrapperParam(SPEC_SETTINGS_ID, specSettings);
        wrapper.execute();

        mUtil.writeObject(this, Strings.FILE_SPEC_SETTINGS_OBJECT, specSettings);
        if (mChangeText)
            mUtil.changeScreen(this, Activity_Shifter_Manager_Settings.class);
        else
            mUtil.changeScreen(this, Activity_Shift_Hour_Setting.class);

    }

    private SpecSettingView constSettingView(String header, String children, String rest) {
        SpecSettingView mSpecView = new SpecSettingView(this);
        mSpecView.setHeader(header);
        mSpecView.setChildren(children);
        mSpecView.setRestrictions(rest.split("=")[0], Integer.parseInt(rest.split("=")[1]));
        mSpecView.genViews();
        return mSpecView;
    }
}
