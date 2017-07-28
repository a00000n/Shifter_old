package alon.com.shifter.activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.base_classes.SwipeGestureDetector;
import alon.com.shifter.utils.Util;
import alon.com.shifter.utils_arrangement.Arrangement;
import alon.com.shifter.utils_arrangement.ArrangementArrayList;
import alon.com.shifter.utils_arrangement.ArrangementUser;
import alon.com.shifter.utils_arrangement.ArrangementUserAdapter;
import alon.com.shifter.utils_database.DatabaseUtil;
import alon.com.shifter.wrappers.AsyncWrapper;
import alon.com.shifter.wrappers.FinishableTaskWithParamsWrapper;
import alon.com.shifter.wrappers.RunnableWrapper;

import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_ACTIVITY_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_UTIL_ID;


public class Activity_Shifter_Manager_Arrangement extends BaseActivity {

    private final ArrayList<BaseUser> mAllUsersThatSubmittedShifts = new ArrayList<>();

    private ListView mUserList;
    private ProgressBar mLoading;
    private Button mNextDay;
    private Button mPreviousDay;

    private ArrangementArrayList mUserArray;
    private Arrangement mArrangement;

    private Handler mUpdateUsers;
    private int mSelectedDay = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shifter_arrangement);

        TAG = "Shifter_arrangement";
        getUtil(this);

        setupUI();


    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setupUI() {

        mUpdateUsers = new Handler();
        mArrangement = new Arrangement();
        mUserArray = new ArrangementArrayList(mArrangement);

        mUserList = (ListView) findViewById(R.id.ARRGMENT_user_list);
        mLoading = (ProgressBar) findViewById(R.id.ARRGMENT_loading_user_list);
        mNextDay = (Button) findViewById(R.id.ARRGMENT_day_one_day_forward);
        mPreviousDay = (Button) findViewById(R.id.ARRGMENT_day_one_day_back);

        SwipeGestureDetector detector = new SwipeGestureDetector(findViewById(R.id.MGR_ARRGMENT_container), 150);
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

        findViewById(R.id.MGR_ARRGMENT_container).setOnTouchListener(detector);
        mUserList.setOnTouchListener(detector);

        findViewById(R.id.ARRGMENT_back).setOnClickListener(this);
        findViewById(R.id.ARRGMENT_submit).setOnClickListener(this);
        mNextDay.setOnClickListener(this);
        mPreviousDay.setOnClickListener(this);

        ArrangementUserAdapter mAdapter = new ArrangementUserAdapter(this, mUserArray, mSelectedDay);
        mUserList.setAdapter(mAdapter);

        ((TextView) findViewById(R.id.ARRGMENT_day_title)).setText(mUtil.getDayString(this, mSelectedDay));

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras.containsKey(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST) && extras.containsKey(Strings.KEY_ARRANGEMENT_OBJECT)) {
                mArrangement = (Arrangement) extras.getSerializable(Strings.KEY_ARRANGEMENT_OBJECT);
                mAllUsersThatSubmittedShifts.clear();
                mAllUsersThatSubmittedShifts.addAll((ArrayList<BaseUser>) extras.getSerializable(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST));
                mUserArray.setArrangement(mArrangement);
                mDoneUpdatingUsers.setWrapperParam(WRAPPER_UTIL_ID, mUtil).setWrapperParam(Param_Keys.KEY_BASE_USER_OBJECT_LIST, mAllUsersThatSubmittedShifts);
                mDoneUpdatingUsers.onFinish();
                return;
            }
        }

        AsyncWrapper wrapper = new AsyncWrapper() {

            @Override
            protected Void doInBackground(Void... params) {

                Activity act = (Activity) getWrapperParam(WRAPPER_ACTIVITY_ID);
                Linker linker;
                try {
                    mDoneUpdatingUsers.setWrapperParam(WRAPPER_UTIL_ID, getWrapperParam(WRAPPER_UTIL_ID));
                    linker = Linker.getLinker(act, Linker_Keys.TYPE_GET_SUBMITED_SHIFTS);
                    linker.addParam(Linker_Keys.KEY_ARRGMENT_DATE_FOR_THE_SHIFT_PULL, Linker.determineClosestSunday());
                    linker.setOnFinish(mDoneUpdatingUsers);
                    linker.execute();
                } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        wrapper.setWrapperParam(WRAPPER_ACTIVITY_ID, this).setWrapperParam(WRAPPER_UTIL_ID, mUtil);
        wrapper.execute();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ARRGMENT_day_one_day_back:
                if (mSelectedDay == 1)
                    mSelectedDay = 7;
                else
                    mSelectedDay--;
                break;
            case R.id.ARRGMENT_day_one_day_forward:
                if (mSelectedDay == 7)
                    mSelectedDay = 1;
                else
                    mSelectedDay++;
                break;
            case R.id.ARRGMENT_back:
                mUtil.changeScreen(this, Activity_Shifter_Main_Manager.class);
                return;
            case R.id.ARRGMENT_submit:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Strings.KEY_ARRANGEMENT_OBJECT, mArrangement);
                bundle.putSerializable(Strings.KEY_ARRANGEMENT_USER_ARRAYLIST, mAllUsersThatSubmittedShifts);
                mUtil.changeScreen(this, Activity_Shifter_Manager_Arrangement_Display.class, bundle);
                return;
        }
        mDoneUpdatingUsers.setWrapperParam(WRAPPER_UTIL_ID, mUtil).setWrapperParam(Param_Keys.KEY_BASE_USER_OBJECT_LIST, mAllUsersThatSubmittedShifts);
        mDoneUpdatingUsers.onFinish();
        ((TextView) findViewById(R.id.ARRGMENT_day_title)).setText(mUtil.getDayString(this, mSelectedDay));
    }

    private FinishableTaskWithParamsWrapper mDoneUpdatingUsers = new FinishableTaskWithParamsWrapper() {
        @Override
        @SuppressWarnings("unchecked")
        public void onFinish() {
            ArrayList<BaseUser> userList = (ArrayList<BaseUser>) getParamsFromTask().get(Param_Keys.KEY_BASE_USER_OBJECT_LIST);
            if (mAllUsersThatSubmittedShifts.size() == 0)
                mAllUsersThatSubmittedShifts.addAll(userList);
            ((ArrangementUserAdapter) mUserList.getAdapter()).setDay(mSelectedDay);
            RunnableWrapper runnableWrapper = new RunnableWrapper() {

                @Override
                public void run() {
                    if (mAllUsersThatSubmittedShifts.size() == 0)
                        mAllUsersThatSubmittedShifts.addAll((Collection<? extends BaseUser>) getParamsFromTask().get(Param_Keys.KEY_BASE_USER_OBJECT_LIST));
                    Cursor userC = DatabaseUtil.getAllUsersRequestForDate(Linker.determineClosestSunday());
                    HashMap<String, String> userMorningSubmissions = new HashMap<>();
                    HashMap<String, String> userAfterNoonSubmissions = new HashMap<>();
                    HashMap<String, String> userEveningSubmissions = new HashMap<>();
                    HashMap<String, String> userNightSubmissions = new HashMap<>();
                    while (userC.moveToNext()) {
                        //0 - ID, 1 - Name, 2 - Date, 3 - Sun, 4 - Mon, 5 - Tue, 6 - Wen, 7 - Thu, 8 - Fri, 9 - Sat. reduce 3 to get wanted column.
                        String userName = userC.getString(0);
                        String doesWantDay = userC.getString(mSelectedDay);
                        String[] daysAndComments = doesWantDay.split(":");
                        userMorningSubmissions.put(userName, daysAndComments[0]);
                        userAfterNoonSubmissions.put(userName, daysAndComments[1]);
                        userEveningSubmissions.put(userName, daysAndComments[2]);
                        userNightSubmissions.put(userName, daysAndComments[3]);
                    }
                    HashMap<String, BaseUser> userBaseUserMap = new HashMap<>();
                    mUserArray.clear();
                    for (int i = 0; i < 4; i++) {
                        HashMap<String, String> userSubmissionMap = new HashMap<>();
                        ArrangementUser seperationUser = new ArrangementUser(null);
                        seperationUser.setTitleRow(true);
                        seperationUser.setTitlePageName(((Util) getWrapperParam(WRAPPER_UTIL_ID))
                                .getShiftTitle(Activity_Shifter_Manager_Arrangement.this, i));
                        mUserArray.add(seperationUser);
                        switch (i) {
                            case 0:
                                userSubmissionMap = userMorningSubmissions;
                                break;
                            case 1:
                                userSubmissionMap = userAfterNoonSubmissions;
                                break;
                            case 2:
                                userSubmissionMap = userEveningSubmissions;
                                break;
                            case 3:
                                userSubmissionMap = userNightSubmissions;
                                break;
                        }

                        for (Map.Entry<String, String> entry : userSubmissionMap.entrySet())
                            if (entry.getValue().equals("1"))
                                if (userBaseUserMap.containsKey(entry.getKey()))
                                    mUserArray.add(new ArrangementUser(userBaseUserMap.get(entry.getKey())));
                                else {
                                    userBaseUserMap.put(entry.getKey(), getBaseUserObject(entry.getKey()));
                                    mUserArray.add(new ArrangementUser(userBaseUserMap.get(entry.getKey())));
                                }
                    }
                    if (mUserList.getVisibility() == View.GONE) {
                        mLoading.setVisibility(View.GONE);
                        mUserList.setVisibility(View.VISIBLE);
                    }
                    ((ArrangementUserAdapter) mUserList.getAdapter()).notifyDataSetChanged();
                }

                private BaseUser getBaseUserObject(String UID) {
                    ArrayList<BaseUser> userList = (ArrayList<BaseUser>) getWrapperParam(WRAPPER_ARRAY_LIST_ID);
                    for (BaseUser baseUser : userList)
                        if (baseUser.getName().equals(UID))
                            return baseUser;
                    return null;
                }
            };
            runnableWrapper.setWrapperParam(WRAPPER_ARRAY_LIST_ID, userList == null ? mAllUsersThatSubmittedShifts : userList).setWrapperParam(WRAPPER_UTIL_ID, getWrapperParam(WRAPPER_UTIL_ID));
            mUpdateUsers.post(runnableWrapper);
        }
    };

}
