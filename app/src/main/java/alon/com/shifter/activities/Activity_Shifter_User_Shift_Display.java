package alon.com.shifter.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.FinishableTaskWithParams;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.base_classes.SwipeGestureDetector;
import alon.com.shifter.utils.FirebaseUtil;
import alon.com.shifter.utils.Util;
import alon.com.shifter.utils_arrangement.Arrangement;
import alon.com.shifter.wrappers.FinishableTaskWithParamsWrapper;
import alon.com.shifter.wrappers.OnCompleteWrapper;

public class Activity_Shifter_User_Shift_Display extends BaseActivity {

    private Button mNextDay;
    private Button mPreviousDay;
    private ProgressBar mLoading;
    private ScrollView mScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifter_user_shift_display);

        TAG = "Shifter_User_Shift_Display";
        getUtil(this);

        setupUI();
    }

    @Override
    protected void setupUI() {
        RelativeLayout container = (RelativeLayout) findViewById(R.id.USR_SHIFT_DISPLAY_container);
        SwipeGestureDetector detector = new SwipeGestureDetector(container, 150);
        detector.setListener(new SwipeGestureDetector.OnSwipeListener() {
            @Override
            public void onSwipe(View touchedView, SwipeGestureDetector.SwipeType swipeType) {
                if (SwipeGestureDetector.hierarchyCheck(touchedView, R.id.MGR_ARRGMENT_container)) {
                    if (swipeType.equals(SwipeGestureDetector.SwipeType.RIGHT_TO_LEFT))
                        mNextDay.callOnClick();
                    else if (swipeType.equals(SwipeGestureDetector.SwipeType.LEFT_TO_RIGHT))
                        mPreviousDay.callOnClick();
                }
            }
        });

        mPreviousDay = (Button) findViewById(R.id.USR_SHIFT_DISPLAY_day_one_day_back);
        mNextDay = (Button) findViewById(R.id.USR_SHIFT_DISPLAY_day_one_day_forward);

        mPreviousDay.setOnClickListener(this);
        mNextDay.setOnClickListener(this);

        mLoading = (ProgressBar) findViewById(R.id.USR_SHIFT_DISPLAY_loading_shifts);
        mScroller = (ScrollView) findViewById(R.id.USR_SHIFT_dISPLAY_shifts_scroll_view);

        OnCompleteWrapper<byte[]> wrapper = new OnCompleteWrapper<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                FinishableTaskWithParams finishableTask = (FinishableTaskWithParams) getWrapperParam(WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID);
                try {
                    Arrangement fArrg = (Arrangement) Util.deseralizeObject(task.getResult());
                    finishableTask.addParamToTask(Param_Keys.KEY_ARRANGEMENT_OBJECT, fArrg);
                    finishableTask.onFinish();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        FinishableTaskWithParamsWrapper finishableTask = new FinishableTaskWithParamsWrapper() {
            @Override
            public void onFinish() {
                Arrangement arrg = (Arrangement) getParamsFromTask().get(Param_Keys.KEY_ARRANGEMENT_OBJECT);
                Activity act = (Activity) getWrapperParam(WRAPPER_ACTIVITY_ID);
                Util util = (Util) getWrapperParam(WRAPPER_UTIL_ID);
                String shifts = arrg.getAllShiftsForMe(FirebaseUtil.getUser().getUID());
                String[] parts = shifts.split("~");
                if (parts.length != 7)
                    throw new RuntimeException();
//                Set<String> allUsersSet = arrg.getAllUserUIDs();
//
//                FinishableTaskWithParamsWrapper wrapper = new FinishableTaskWithParamsWrapper() {
//                    @Override
//                    public void onFinish() {
//                        Activity act = (Activity) getWrapperParam(WRAPPER_ACTIVITY_ID);
//                        Arrangement arrg = (Arrangement) getWrapperParam(WRAPPER_ARRANGEMENT_ID);
//
//                        StringBuilder builder = new StringBuilder();
//                        for (int i = 0; i < 7; i++) {
//                            builder.append(mUtil.getDayString(act, i + 1)).append("\n");
//                            for (int j = 0; j < 4; j++) {
//                                builder.append("\t").append(mUtil.getShiftTitle(act, j)).append("\n");
//                                for (Map.Entry<String, String> entry : arrg.getDay(i).getShift(j).getAllEmployees().entrySet()) {
//                                    BaseUser user = findUser(entry.getKey());
//                                    String time = entry.getValue();
//                                    if (user == null) {
//                                        Log.i(TAG, "setupUI: A user is written but not found as someone who applied at all.");
//                                        continue;
//                                    } else if (time.equals(Strings.MGR_ARRGMENT_DEFAULT_TIME)) {
//                                        time = hours.getHour(j);
//                                    } else {
//                                        String[] timeParts = time.split("~");
//                                        time = timeParts[0] + "-" + timeParts[1];
//                                    }
//                                    builder.append("\t\t- ").append(user.getName()).append(" ").append(time).append("\n");
//                                }
//                            }
//                        }
//                    }
//                };
//
//                FirebaseUtil.getUsersByUID();

                mLoading.setVisibility(View.GONE);
                mScroller.setVisibility(View.VISIBLE);
            }
        };
        FirebaseUtil.getStorage(Linker.determineClosestSunday(0)).getBytes(1024 * 1024).addOnCompleteListener(this, wrapper);


    }
}
