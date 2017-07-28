package alon.com.shifter.activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.FinishableTask;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.dialog_fragments.ErrorDialogFragment;
import alon.com.shifter.dialog_fragments.ProgressDialogFragment;
import alon.com.shifter.utils_shift.Shift;
import alon.com.shifter.utils_shift.SpecSettings;
import alon.com.shifter.views.ShiftSubmissionView;
import alon.com.shifter.wrappers.AsyncWrapper;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;
import static alon.com.shifter.utils.FlowController.getIsGateOpen;
import static alon.com.shifter.wrappers.WrapperBase.DIALOG_FRAGMENT_ID;
import static alon.com.shifter.wrappers.WrapperBase.SHIFT_ID;

public class Activity_Shifter_User_Submit extends BaseActivity {

    private ShiftSubmissionView mShiftSubmissions;
    private SpecSettings mSettings;

    private Handler mFinishHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shift_submission);

        TAG = "Shifter_user_submission";
        getUtil(this);

        if (!getIsGateOpen(Fc_Keys.SPEC_SETTINGS_SET_PULLED) || !getIsGateOpen(Fc_Keys.SHIFT_SCHEDULE_SETTINGS_PULLED) || !getIsGateOpen(Fc_Keys.SPEC_SETTINGS_TYPES_PULLED)) {
            final ProgressDialogFragment mDialog = mUtil.generateStandbyDialog(this);
            //FIXME : add wrapper
            FinishableTask mTask = new FinishableTask() {

                int count = 0;

                @Override
                public void onFinish() {
                    count++;
                    if (count == 3) {
                        setupUI();
                        mDialog.dismiss();
                    }
                }
            };
            addGateOpenListener(Fc_Keys.SPEC_SETTINGS_SET_PULLED, mTask);
            addGateOpenListener(Fc_Keys.SHIFT_SCHEDULE_SETTINGS_PULLED, mTask);
            addGateOpenListener(Fc_Keys.SPEC_SETTINGS_TYPES_PULLED, mTask);
        } else
            setupUI();
    }

    @Override
    protected void setupUI() {
        mFinishHandler = new Handler();
        mShiftSubmissions = (ShiftSubmissionView) findViewById(R.id.USR_SS_shiftSubmissionView);
        mSettings = (SpecSettings) mUtil.readPref(this, Pref_Keys.USR_SPEC_SETTINGS_OBJECT, SpecSettings.getEmpty());
        if (!mSettings.equals(SpecSettings.getEmpty()))
            mShiftSubmissions.setSpecSettings(mSettings, (String) mUtil.readPref(this, Pref_Keys.MGR_SEC_COMPLETE_SPEC_SETTINGS, Strings.NULL));
        Button mDone = (Button) findViewById(R.id.USR_SS_done);

        mDone.setOnClickListener(this);

        Log.i(TAG, "setupUI: " + mSettings.toString());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.USR_SS_done) {
            boolean[] validSubmission = mShiftSubmissions.submit();
            if (validSubmission != null) {
                boolean limitsMet = true;
                for (int i = 0; i < 4; i++)
                    if (!validSubmission[i]) {
                        limitsMet = false;
                        break;
                    }
                if (!limitsMet) {
                    ErrorDialogFragment error = new ErrorDialogFragment();
                    error.setTitle(getString(R.string.user_shift_submission_error));
                    String errMsg = generateErrorMsg(validSubmission);
                    Log.i(TAG, "onClick: " + errMsg);
                    error.setMsg(errMsg);
                    error.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    error.show(getFragmentManager(), DialogFragment_Keys.SHIFT_SUBMISSION_ERROR);
                    return;
                }
            }
            Shift submittedShiftObject = mShiftSubmissions.getSelectedShifts();
            ProgressDialogFragment frag = new ProgressDialogFragment();
            frag.setTitle(getString(R.string.dialog_uploading_data));
            frag.setMessage(getString(R.string.user_uploading_shifts));
            frag.show(getFragmentManager(), DialogFragment_Keys.UPLOADING_KEY);
            AsyncWrapper wrapper = new AsyncWrapper() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Linker linker = Linker.getLinker(Activity_Shifter_User_Submit.this, Linker_Keys.TYPE_UPLOAD_SELECTED_SHIFTS_USER);
                        linker.addParam(Linker_Keys.KEY_SHIFT_UPLOAD_SHIFT_OBJECT, getWrapperParam(SHIFT_ID));
                        linker.setOnFinish(new FinishableTask() {

                            @Override
                            public void onFinish() {
                                mFinishHandler.post(new RunnableWrapper((DialogFragment) getWrapperParam(DIALOG_FRAGMENT_ID)) {
                                    @Override
                                    public void run() {
                                        mFrag.dismiss();
                                        mUtil.changeScreen(Activity_Shifter_User_Submit.this, Activity_Shifter_Main_User.class);
                                    }
                                });
                            }
                        });
                        linker.execute();
                    } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                abstract class RunnableWrapper implements Runnable {
                    DialogFragment mFrag;

                    RunnableWrapper(DialogFragment frag) {
                        mFrag = frag;
                    }
                }

            };
            wrapper.setWrapperParam(SHIFT_ID, submittedShiftObject).setWrapperParam(DIALOG_FRAGMENT_ID, frag);
            wrapper.execute();
        }
    }

    private String generateErrorMsg(boolean[] errors) {
        String result = "";
        String[] headers = mUtil.readPref(this, Pref_Keys.USR_SPEC_SETTINGS, Strings.NULL).toString().split(";")[0].split("~");
        String[] errs = new String[]{getString(R.string.err_shift_day_msg),
                getString(R.string.err_shift_shift_msg),
                getString(R.string.err_shift_amount_msg_1),
                getString(R.string.err_shift_open_close_msg)};
        String errAmountEnd = getString(R.string.err_shift_amount_msg_2);
        for (int i = 0; i < 4; i++)
            if (!errors[i]) {
                result += errs[i] + " ";
                if (i != 3) {
                    ArrayList<String> children = mSettings.getChildrenArrayList(headers[i]);
                    if (children != null) {
                        for (String child : children)
                            result += child + ",";
                    } else
                        throw new RuntimeException("Fatal; no children to header in spec settings.");
                    result = result.substring(0, result.lastIndexOf(','));
                    if (i == 2)
                        result += " " + errAmountEnd + "\n";
                    else
                        result += "\n";
                    System.out.println(result);
                } else
                    result += "\n";
            }
        return result;
    }
}
