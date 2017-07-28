package alon.com.shifter.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.FinishableTask;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.dialog_fragments.ProgressDialogFragment;
import alon.com.shifter.dialog_fragments.TwoButtonDialogFragment;
import alon.com.shifter.utils.Util;
import alon.com.shifter.utils_shift.Shift;
import alon.com.shifter.utils_shift.ShiftInfo;
import alon.com.shifter.views.ShiftSubmissionView;
import alon.com.shifter.wrappers.AsyncWrapper;
import alon.com.shifter.wrappers.FinishableTaskWrapper;
import alon.com.shifter.wrappers.OnClickWrapper;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;
import static alon.com.shifter.utils.FlowController.getIsGateOpen;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_CONTEXT_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_FINISHABLE_TASK_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_UTIL_ID;

public class Activity_Select_Shifts_For_Week extends BaseActivity {


    private ShiftSubmissionView mShifts;

    private Shift mShift;

    private boolean mChangeText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getExtras() != null) {
            mShift = (Shift) getIntent().getExtras().getSerializable(Strings.FILE_SHIFT_OBJECT);
            mChangeText = getIntent().getExtras().getBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON);

        }

        setContentView(R.layout.activity_first_shift_setup);

        TAG = "Shifter_ShiftSelectionMgr";

        getUtil(this);

        if (mShift == null)
            mShift = (Shift) mUtil.readObject(this, Strings.FILE_SHIFT_OBJECT);

        boolean allGatesOpen = getIsGateOpen(Fc_Keys.SPEC_SETTINGS_TYPES_PULLED) && getIsGateOpen(Fc_Keys.SPEC_SETTING_RESTS_PULLED) && getIsGateOpen(Fc_Keys.SPEC_SETTINGS_EXPANDABLE_INFO_PULLED);
        if (allGatesOpen)
            setupUI();
        else {
            final ProgressDialogFragment mDialog = mUtil.generateStandbyDialog(this);
            FinishableTask mTask = new FinishableTask() {
                int gateOpenCount = 0;

                @Override
                public void onFinish() {
                    gateOpenCount++;
                    if (gateOpenCount == 3) {
                        mDialog.dismiss();
                        setupUI();
                    }
                }
            };
            addGateOpenListener(Fc_Keys.SPEC_SETTINGS_TYPES_PULLED, mTask);
            addGateOpenListener(Fc_Keys.SPEC_SETTING_RESTS_PULLED, mTask);
            addGateOpenListener(Fc_Keys.SPEC_SETTINGS_EXPANDABLE_INFO_PULLED, mTask);
        }
    }

    @Override
    protected void setupUI() {
        mShifts = (ShiftSubmissionView) findViewById(R.id.FSS_shiftSubmissionView);
        if (mShift != null)
            mShifts.constructViewFromShift(mShift);
        FinishableTaskWrapper acceptedShiftSettingsWrapper = new FinishableTaskWrapper() {
            @Override
            public void onFinish() {
                Context context = (Context) getWrapperParam(WRAPPER_CONTEXT_ID);
                Util util = (Util) getWrapperParam(WRAPPER_UTIL_ID);
                JSONObject mJson = new JSONObject();
                ShiftInfo[] mInfo = mShifts.getSelectedShifts().getInfoComplete();
                try {
                    for (int i = 0; i < mInfo.length; i++) {
                        String day = util.getDayString(context, i + 1);
                        JSONObject mSubDay = new JSONObject();
                        for (int j = 0; j < 4; j++)
                            mSubDay.put(util.getShiftTitle(context, j), mInfo[i].getFor(j));
                        mJson.put(day, mSubDay);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                util.writePref(context, Pref_Keys.USR_SHIFT_SCHEDULE, mJson.toString());
                TwoButtonDialogFragment frag = new TwoButtonDialogFragment();
                frag.setTitle(getString(R.string.dialog_uploading_data));
                frag.setMessage(getString(R.string.mgr_uploading_data_add_spec_settings));
                frag.setButtonLeft(getString(R.string.title_special_settings));
                frag.setButtonRight(getString(R.string.title_shift_hours));
                frag.setCancelable(false);
                OnClickWrapper wrapperLeft = new OnClickWrapper() {
                    @Override
                    public void onClick(View v) {
                        Util util = (Util) getWrapperParam(WRAPPER_UTIL_ID);
                        util.writePref(Activity_Select_Shifts_For_Week.this, Pref_Keys.MGR_SEC_SCHEDULE_SET, true);
                        Bundle mExtras = new Bundle();
                        mExtras.putBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON, false);
                        util.changeScreen(Activity_Select_Shifts_For_Week.this, Activity_Special_Settings.class, mExtras);
                        Activity_Select_Shifts_For_Week.this.finish();
                        ((DialogFragment) getWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID)).dismiss();
                    }
                };
                OnClickWrapper wrapperRight = new OnClickWrapper() {
                    @Override
                    public void onClick(View v) {
                        Util util = (Util) getWrapperParam(WRAPPER_UTIL_ID);
                        util.writePref(Activity_Select_Shifts_For_Week.this, Pref_Keys.MGR_SEC_SCHEDULE_SET, true);
                        Bundle mExtras = new Bundle();
                        mExtras.putBoolean(Strings.KEY_SHOULD_CHANGE_BACK_BUTTON, false);
                        util.changeScreen(Activity_Select_Shifts_For_Week.this, Activity_Shift_Hour_Setting.class, mExtras);
                        Activity_Select_Shifts_For_Week.this.finish();
                        ((DialogFragment) getWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID)).dismiss();
                    }
                };

                wrapperRight.setWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID, frag);
                wrapperLeft.setWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID, frag);

                frag.setLeftListener(wrapperLeft);
                frag.setRightListener(wrapperRight);
                frag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                frag.show(getFragmentManager(), DialogFragment_Keys.TWO_BUTTON_DIALOG);
                AsyncWrapper wrapper = new AsyncWrapper() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Linker linker = Linker.getLinker(Activity_Select_Shifts_For_Week.this, Linker_Keys.TYPE_UPLOAD_SHIFT_SCHEDULE);
                            linker.addParam(Linker_Keys.KEY_SHIFT_UPLOAD_SHIFT_OBJECT, getWrapperParam(WRAPPER_SHIFT_ID));
                            linker.addParam(Linker_Keys.KEY_SHIFT_UPLOAD_DIALOG, getWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID));
                            linker.execute();
                        } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                wrapper.setWrapperParam(WRAPPER_SHIFT_ID, mShifts.getSelectedShifts()).setWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID, frag);
                wrapper.execute();
            }
        };
        acceptedShiftSettingsWrapper.setWrapperParam(WRAPPER_UTIL_ID, mUtil).setWrapperParam(WRAPPER_CONTEXT_ID, this);

        Button mDone = (Button) findViewById(R.id.FSS_done);
        if (mChangeText)
            mDone.setText(getString(R.string.back));
        OnClickWrapper wrapper = new OnClickWrapper() {
            @Override
            public void onClick(View v) {
                FinishableTaskWrapper mDoneSelectingShifts = new FinishableTaskWrapper() {
                    @Override
                    public void onFinish() {
                        if (!mChangeText) {
                            TwoButtonDialogFragment frag = new TwoButtonDialogFragment();
                            frag.setTitle(getString(R.string.are_you_sure));
                            frag.setMessage(getString(R.string.mgr_set_shifts));
                            frag.setButtonLeft(getString(R.string.cancel));
                            frag.setButtonRight(getString(R.string.done));
                            OnClickWrapper wrapperLeft = new OnClickWrapper() {
                                @Override
                                public void onClick(View v) {
                                    ((DialogFragment) getWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID)).dismiss();
                                }
                            };
                            OnClickWrapper wrapperRight = new OnClickWrapper() {
                                @Override
                                public void onClick(View v) {
                                    ((DialogFragment) getWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID)).dismiss();
                                    ((FinishableTask) getWrapperParam(WRAPPER_FINISHABLE_TASK_ID)).onFinish();
                                }
                            };
                            wrapperRight.setWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID, frag);
                            wrapperLeft.setWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID, frag);

                            wrapperRight.setWrapperParam(WRAPPER_FINISHABLE_TASK_ID, getWrapperParam(WRAPPER_FINISHABLE_TASK_ID));

                            frag.setLeftListener(wrapperLeft);
                            frag.setRightListener(wrapperRight);
                            frag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                            frag.show(getFragmentManager(), DialogFragment_Keys.TWO_BUTTON_DIALOG);
                        } else
                            mUtil.changeScreen(Activity_Select_Shifts_For_Week.this, Activity_Shifter_Manager_Settings.class);

                    }
                };
                mDoneSelectingShifts.setWrapperParam(WRAPPER_FINISHABLE_TASK_ID, getWrapperParam(WRAPPER_FINISHABLE_TASK_ID));

                if (getIsGateOpen(Fc_Keys.LOGIN_OR_REGISTER_FINISHED))
                    mDoneSelectingShifts.onFinish();
                else {
                    addGateOpenListener(Fc_Keys.LOGIN_OR_REGISTER_FINISHED, mDoneSelectingShifts);
                }
            }
        };
        wrapper.setWrapperParam(WRAPPER_FINISHABLE_TASK_ID, acceptedShiftSettingsWrapper);
        mDone.setOnClickListener(wrapper);
    }

//    private abstract class AsyncTaskWrapper extends AsyncTask<Void, Void, Void> {
//
//        DialogFragment mDialog;
//        Shift mShift;
//
//        AsyncTaskWrapper(DialogFragment dialog, Shift shift) {
//            mDialog = dialog;
//            mShift = shift;
//        }
//    }
}
