package alon.com.shifter.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.FinishableTaskWithParams;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.dialog_fragments.ProgressDialogFragment;
import alon.com.shifter.utils.FirebaseUtil;
import alon.com.shifter.utils.Util;
import alon.com.shifter.utils_arrangement.Arrangement;
import alon.com.shifter.utils_shift.ShiftHours;
import alon.com.shifter.wrappers.AsyncWrapper;
import alon.com.shifter.wrappers.FinishableTaskWithParamsWrapper;
import alon.com.shifter.wrappers.OnCompleteWrapper;

import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_ACTIVITY_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_ARRANGEMENT_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_DIALOG_FRAGMENT_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_PROGRESS_BAR_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_SCROLL_VIEW_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_SHIFT_HOURS_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_TEXTVIEW_ID;

public class Activity_Shifter_Manager_Arrangement_Display extends BaseActivity {

    private ArrayList<BaseUser> allUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifter_manager_arrangement_display);

        TAG = "Shifter_Arrangement_Display";
        getUtil(this);

        setupUI();
    }

    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void setupUI() {
        TextView mInfo = (TextView) findViewById(R.id.MGR_ARRGMENT_DISPLAY_info);
        Button mPublish = (Button) findViewById(R.id.MGR_ARRGMENT_DISPLAY_submit);
        Button mCancel = (Button) findViewById(R.id.MGR_ARRGMENT_DISPLAY_cancel);
        ProgressBar mLoading = (ProgressBar) findViewById(R.id.MGR_ARRGMENT_DISPLAY_loading);
        ScrollView mScroller = (ScrollView) findViewById(R.id.MGR_ARRGMENT_DISPLAY_info_sheet);

        mPublish.setOnClickListener(this);
        mCancel.setOnClickListener(this);

        if (getIntent() != null && getIntent().getExtras() != null) {
            if (getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_OBJECT) != null && getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST) != null) {
                Arrangement arrg = (Arrangement) getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_OBJECT);
                allUsers = (ArrayList<BaseUser>) getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST);
                ShiftHours hours = (ShiftHours) mUtil.readObject(this, Strings.FILE_SHIFT_HOURS_OBJECT);
                FinishableTaskWithParamsWrapper finishableTask = new FinishableTaskWithParamsWrapper() {
                    @Override
                    public void onFinish() {
                        byte[] shiftHoursBytes = (byte[]) getParamsFromTask().get(Param_Keys.KEY_SHIFT_HOURS_BYTE_ARRAY);
                        try {
                            ShiftHours hours;
                            Activity act = (Activity) getWrapperParam(WRAPPER_ACTIVITY_ID);
                            if (shiftHoursBytes == null) {
                                hours = (ShiftHours) getWrapperParam(WRAPPER_SHIFT_HOURS_ID);
                            } else {
                                hours = (ShiftHours) Util.deseralizeObject(shiftHoursBytes);
                                Util.getInstance(act).writeObject(act, Strings.FILE_SHIFT_HOURS_OBJECT, hours);
                                if (hours == null)
                                    throw new RuntimeException("No hours set");
                            }
                            Arrangement arrg = (Arrangement) getWrapperParam(WRAPPER_ARRANGEMENT_ID);
                            View scroller = (View) getWrapperParam(WRAPPER_SCROLL_VIEW_ID);
                            View progressBar = (View) getWrapperParam(WRAPPER_PROGRESS_BAR_ID);
                            scroller.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < 7; i++) {
                                builder.append(mUtil.getDayString(act, i + 1)).append("\n");
                                for (int j = 0; j < 4; j++) {
                                    builder.append("\t").append(mUtil.getShiftTitle(act, j)).append("\n");
                                    for (Map.Entry<String, String> entry : arrg.getDay(i).getShift(j).getAllEmployees().entrySet()) {
                                        BaseUser user = findUser(entry.getKey());
                                        String time = entry.getValue();
                                        if (user == null) {
                                            Log.i(TAG, "setupUI: A user is written but not found as someone who applied at all.");
                                            continue;
                                        } else if (time.equals(Strings.MGR_ARRGMENT_DEFAULT_TIME)) {
                                            time = hours.getHour(j);
                                        } else {
                                            String[] timeParts = time.split("~");
                                            time = timeParts[0] + "-" + timeParts[1];
                                        }
                                        builder.append("\t\t- ").append(user.getName()).append(" ").append(time).append("\n");
                                    }
                                }
                            }
                            ((TextView) getWrapperParam(WRAPPER_TEXTVIEW_ID)).setText(builder.toString());
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                };
                finishableTask.setWrapperParam(WRAPPER_TEXTVIEW_ID, mInfo)
                        .setWrapperParam(WRAPPER_ACTIVITY_ID, this)
                        .setWrapperParam(WRAPPER_ARRANGEMENT_ID, arrg)
                        .setWrapperParam(WRAPPER_SHIFT_HOURS_ID, hours)
                        .setWrapperParam(WRAPPER_PROGRESS_BAR_ID, mLoading)
                        .setWrapperParam(WRAPPER_SCROLL_VIEW_ID, mScroller);
                if (hours == null) {
                    OnCompleteWrapper<byte[]> wrapper = new OnCompleteWrapper<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            FinishableTaskWithParams finishableTask = (FinishableTaskWithParams) getWrapperParam(WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID);
                            finishableTask.addParamToTask(Param_Keys.KEY_SHIFT_HOURS_BYTE_ARRAY, task.getResult());
                            finishableTask.onFinish();
                        }
                    };
                    wrapper.setWrapperParam(WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID, finishableTask);
                    FirebaseUtil.getStorage(Linker.generateStorageFileNameForShiftHours()).getBytes((long) 1024 * 1024).addOnCompleteListener(this, wrapper);
                    return;
                } else
                    finishableTask.onFinish();

                return;
            }
        }
        mInfo.setText(getString(R.string.mgr_arrgment_nothing_defined));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClick(View v) {
        Arrangement arrg = null;
        ArrayList<BaseUser> users = null;
        if (getIntent() != null &&
                getIntent().getExtras() != null &&
                getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_OBJECT) != null &&
                getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST) != null) {
            arrg = (Arrangement) getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_OBJECT);
            users = (ArrayList<BaseUser>) getIntent().getExtras().getSerializable(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST);
        }
        switch (v.getId()) {
            case R.id.MGR_ARRGMENT_DISPLAY_cancel:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Strings.KEY_ARRANGEMENT_OBJECT, arrg);
                bundle.putSerializable(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST, users);
                mUtil.changeScreen(this, Activity_Shifter_Manager_Arrangement.class, bundle);
                break;
            case R.id.MGR_ARRGMENT_DISPLAY_submit:
                ProgressDialogFragment frag = new ProgressDialogFragment();
                frag.setTitle(getString(R.string.dialog_uploading_data));
                frag.setMessage(getString(R.string.mgr_arrgment_uploading));
                frag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                frag.show(getFragmentManager(), DialogFragment_Keys.UPLOADING_KEY);
                AsyncWrapper wrapper = new AsyncWrapper() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Linker linker = Linker.getLinker((Activity) getWrapperParam(WRAPPER_CONTEXT_ID), Linker_Keys.TYPE_UPLOAD_FILE_SHIFT_ARRGMENT);
                            linker.addParam(Linker_Keys.KEY_ARRANGEMENT_OBJECT, getWrapperParam(WRAPPER_ARRANGEMENT_ID));
                            linker.addParam(Linker_Keys.KEY_ARRGMENT_DIALOG, getWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID));
                            linker.execute();
                        } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                wrapper.setWrapperParam(WRAPPER_ARRANGEMENT_ID, arrg).setWrapperParam(WRAPPER_DIALOG_FRAGMENT_ID, frag);
                wrapper.execute();
                break;
        }
    }

    private BaseUser findUser(String uid) {
        for (BaseUser user : allUsers) {
            if (user.getUID().equals(uid))
                return user;
        }
        return null;
    }
}
