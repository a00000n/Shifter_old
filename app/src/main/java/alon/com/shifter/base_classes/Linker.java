package alon.com.shifter.base_classes;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import alon.com.shifter.R;
import alon.com.shifter.activities.Activity_Shifter_Main_Manager;
import alon.com.shifter.activities.Activity_Shifter_Main_User;
import alon.com.shifter.dialog_fragments.ProgressDialogFragment;
import alon.com.shifter.dialog_fragments.TwoButtonDialogFragment;
import alon.com.shifter.utils.FirebaseUtil;
import alon.com.shifter.utils.Util;
import alon.com.shifter.utils_arrangement.Arrangement;
import alon.com.shifter.utils_database.DatabaseUtil;
import alon.com.shifter.utils_shift.Comment;
import alon.com.shifter.utils_shift.Shift;
import alon.com.shifter.utils_shift.ShiftHours;
import alon.com.shifter.utils_shift.ShiftInfo;
import alon.com.shifter.utils_shift.SpecSettings;
import alon.com.shifter.wrappers.FinishableTaskWithParamsWrapper;
import alon.com.shifter.wrappers.FinishableTaskWrapper;
import alon.com.shifter.wrappers.ValueEventListenerWrapper;

import static alon.com.shifter.base_classes.Consts.Fb_Dirs.MGR_SEC;
import static alon.com.shifter.base_classes.Consts.Ints.DB_ERR_ALREADY_STARTED;
import static alon.com.shifter.base_classes.Consts.Ints.DB_RESULT_OKAY;
import static alon.com.shifter.utils.FlowController.setGate;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_HASHMAP_ID;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_STRING_IP_SITE;

/**
 * The linker class is probably the most important class in the entire application.
 * The singular purpose of the linker class is to provide a factory line for objects that can execute a set of commands according {@link alon.com.shifter.base_classes.Consts.Linker_Keys}.
 * Each type of key has a unique purpose.
 */
public class Linker {

    /**
     * The factory line limit, not more than 5 linker object can exist in the memory at any single time.
     */
    private static final int limit = 5;
    /**
     * Current number of online linkers.
     */
    private static int currentNumberOfLinkers = 0;
    /**
     * Current number of free linker slots.
     */
    private static int currentFreeSlot = 0;
    /**
     * The array of linker objects.
     */
    private static Linker[] factoryLine = new Linker[limit];
    private final String TAG = "LINKER";
    /**
     * The type of {@link Linker} that is needed to be used.
     */
    private int mType = -1;

    /**
     * A list of parameters that is used when executing orders according to the type ({@link #mType}).
     */
    private HashMap<String, Object> mParams;

    /**
     * The caller to the linker class.
     */
    private Activity mCaller;

    /**
     * A {@link Util} object.
     */
    private Util mUtil;

    /**
     * A {@link DelayedTaskExecutor} object.
     */
    private DelayedTaskExecutor mDelayedTaskExecutor;

    /**
     * A task to execute once the process has finished.
     */
    private FinishableTask mTaskOnFinish;

    //==================Factory Line==================

    private Linker(Activity caller, int type) {
        mType = type;
        mParams = new HashMap<>();
        mCaller = caller;
        mUtil = Util.getInstance(mCaller);
        mDelayedTaskExecutor = DelayedTaskExecutor.getInstance();
    }

    /**
     * Get a linker object, assuming that there is an empty slot in the line.
     *
     * @param caller
     *         - The caller activity.
     * @param type
     *         - The execution type, taken from {@link alon.com.shifter.base_classes.Consts.Linker_Keys}.
     *
     * @return A linker object, assuming that there is a location empty.
     *
     * @throws ProductionLineException
     */
    public static Linker getLinker(Activity caller, int type) throws ProductionLineException {
        countProdLine();
        if (limit < currentNumberOfLinkers)
            throw new ProductionLineException();
        if (currentFreeSlot == 5)
            for (int i = 0; i < 5; i++)
                if (factoryLine[i] == null) {
                    currentFreeSlot = i;
                    break;
                }
        if (currentFreeSlot == limit)
            throw new ProductionLineException();
        return (factoryLine[currentFreeSlot++] = new Linker(caller, type));
    }

    //================================================

    public static void countProdLine() {
        int count;
        count = 0;
        int[] occupied = new int[5];
        for (int i = 0, j = 0; i < limit; i++)
            if (factoryLine[i] == null)
                count++;
            else
                occupied[j++] = i;
        //Inform us what positions are empty.
        Log.i("LINKER_PROD_LINE_INF", "getLinker:\n" +
                "Current amount of free linker slots: " + (count) +
                "\nCurrent occupied linker positions: " + Arrays.toString(occupied));
    }

    @Override
    public String toString() {
        for (int i = 0; i < limit; i++) {
            if (factoryLine[i] == this)
                return "Linker #" + Integer.toString(i + 1);
        }
        throw new RuntimeException("Unlinked Linker, not possible.");
    }

    /**
     * Finds the sunday that is the first day of the week in which the shift arrangement is given.
     *
     * @return - the string of the week, for example 29/1/17.
     */
    public static String determineClosestSunday() {
        return determineClosestSunday(1);
    }


    public static String determineClosestSunday(int weekOffset) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date currentDay = cal.getTime();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date closest = cal.getTime();
        cal.add(Calendar.DATE, 7 * weekOffset);
        Date secondClosest = cal.getTime();
        if (closest.before(currentDay) || closest.equals(currentDay))
            return df.format(secondClosest).replace("/", "-");
        else
            return df.format(closest).replace("/", "-");
    }

    /**
     * A method to delete all information inside a linker.
     * This method also calls {@link System#gc()}.
     */
    private void erase() {
        int count = 0;
        for (Linker linker : factoryLine) {
            if (linker == this) {
                Log.i(TAG, "erase: " + toString());
                factoryLine[count] = null;
                System.gc();
                currentFreeSlot = count;
                currentNumberOfLinkers--;
                break;
            }
            count++;
        }
        countProdLine();
    }

    /**
     * A function that is used to create a toast if and when a data pull fails.
     */
    private void onFailedBackgroundDataPull() {
        mCaller.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mCaller, R.string.err_failed_getting_info, Toast.LENGTH_SHORT).show();
                erase();
            }
        });
    }

    /**
     * The execute method runs a set of code according to the type of the linker {@link #mType}.
     * The linker types works as follows:<br>
     * <ul>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_LOGIN}<br>
     * 1)Checks if:<br>
     * - it has the dialog object of the login dialog.<br>
     * - it has the password of the user.<br>
     * - it has the email of the user.<br>
     * 2) assuming the above was met, it creates 3 Tasks,(1 FinishableTask, 2 TaskResult(s)).<br>
     * 3) Uses the {@link FirebaseUtil#login(String, String, Activity, TaskResult)} with one task result.<br>
     * 4) Once the user logged in it runs, it sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#LOGIN_OR_REGISTER_FINISHED} as true, and runs {@link FirebaseUtil#getUserFromDatabase(TaskResult)}, with the second TaskResult.<br>
     * 5) It obtains the {@link BaseUser} object from the {@link FirebaseUtil}, and then sets it in the database, once it's finished it calls the {@link FinishableTask}.<br>
     * 6) If the user wanted to be remembered, this uses {@link #mUtil} object,otherwise it starts the {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_INFO_FETCHER}.<br>
     * 7) Changes the users screen according to the if he's a manager or not.<br>
     * 8) Erases the linker.<br>
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_REGISTER}<br>
     * 1) Checks if:<br>
     * - it has the dialog object.<br>
     * - it has the workplace code.<br>
     * - it has the user's email.<br>
     * - it has the user's personal name.<br>
     * - it has the user's password.<br>
     * - it has the user's phone number.<br>
     * 2) Assuming the above is met, it creates 3 Tasks (1 FinisableTask, 2 TaskResult(s)).<br>
     * 3) Uses the {@link FirebaseUtil#verifyCode(String, TaskResult)}, to verify the workpalce code, passing a TaskResult as a param.<br>
     * 4) Uses the {@link Util#validateEmail(String, TaskResult)} to validate the email given is a legitimate email and not just a random combination of characters.<br>
     * 5) Uses the {@link FirebaseUtil#register(String, String, Activity, TaskResult)} to register the user on the server, passing a TaskResult as a param.<br>
     * 6) Uses the {@link BaseUser#BaseUser(String, String, String, String, String, String, boolean, String)} method to create a new BaseUser object from the info passed in, then it uses the {@link FirebaseUtil#setUser(BaseUser)} to set the user, and push the value to the server.<br>
     * 7) If the user wanted to be remembered, this uses {@link #mUtil} object,otherwise it starts the {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_INFO_FETCHER}.<br>
     * 8) Changes the users screen according to the if he's a manager or not, if this is a user, then it calls {@link FirebaseUtil#insertRequest(BaseUser)}.<br>
     * 9) Erases the linker.<br>
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_UPLOAD_SHIFT_SCHEDULE}<br>
     * 1) Checks if:<br>
     * - it has the {@link Shift} object.<br>
     * - it has the dialog object.<br>
     * 2) Converts the {@link Shift} object to a {@link JSONObject}.<br>
     * 3) Creates 1 FinishableTask.<br>
     * 4) Set the value of {@link alon.com.shifter.base_classes.Consts.Fb_Keys#MGR_SEC_SHIFT_SCHEDULE} in {@link alon.com.shifter.base_classes.Consts.Fb_Dirs#MGR_SEC} to the {@link JSONObject} we created.
     * 5) Sets the value of {@link alon.com.shifter.base_classes.Consts.Pref_Keys#MGR_SEC_SCHEDULE_SET} to true to say the shifts were set.<br>
     * 6) Write the shift object to the {@link alon.com.shifter.base_classes.Consts.Strings#FILE_SHIFT_OBJECT} using {@link #mUtil}.<br>
     * 7) Changes the gate state of {@link alon.com.shifter.base_classes.Consts.Fc_Keys#SCHDULE_INFO_UPLOADED} to true.<br>
     * 8) Erases the linker.<br>
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_GET_PHONE_NUMBER}<br>
     * 1) Checks if:<br>
     * - it has the main dialog object.<br>
     * - it has the verification dialog object [AlertDialog]<br>
     * - it has the verification number.<br>
     * - it creates a random key for the verification.<br>
     * 2) It creates a TaskResult, and a broadcast receiver that runs on SMS_RECEIVED.<br>
     * 3) It registers the receiver, and sends an sms to the number given.<br>
     * 4) When an SMS is received, it checks the number, if the number is the same as the given, and the code is the same as the code created, it runs the TaskResult.<br>
     * 5) The TaskResult adds the phone number to {@link #mUtil} preferences.<br>
     * 6) If it fails it toasts to the user, and dismisses the verification dialog.<br>
     * 7) Erases the linker.<br>
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_UPLOAD_SPEC_SETTINGS}<br>
     * 1) Checks if:<br>
     * - it has the {@link SpecSettings} object.<br>
     * 2) Sets {@link alon.com.shifter.base_classes.Consts.Fb_Keys#MGR_SEC_SPEC_SETTINGS_SET} to the {@link SpecSettings#toString()} in {@link alon.com.shifter.base_classes.Consts.Fb_Dirs#MGR_SEC}.<br>
     * 3) Erases the linker.<br>
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_DELETE_ACCOUNT}<br>
     * 1) Chceks if:<br>
     * - it has the {@link BaseUser} object.<br>
     * 2) Creates a FinishableTaskWithParams.<br>
     * 3) Sets the given {@link BaseUser}'s UID's state in {@link alon.com.shifter.base_classes.Consts.Fb_Dirs#MGR_SEC_USER_REQUESTS} to {@link alon.com.shifter.base_classes.Consts.Strings#VALUE_DELETE_ACCOUNT}.<br>
     * 4) Gets the current value in  {@link alon.com.shifter.base_classes.Consts.Fb_Keys#MGR_SEC_USERS_ACCEPTED} in {@link alon.com.shifter.base_classes.Consts.Fb_Dirs#MGR_SEC}.<br>
     * 5) Once it's found it runs the {@link FinishableTaskWithParams#onFinish()} method and reconstructs the string without the passed user's UID, and re-sets the value to {@link alon.com.shifter.base_classes.Consts.Fb_Keys#MGR_SEC_USERS_ACCEPTED}.<br>
     * 6) Erases the linker.<br>
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_UPDATE_USER_ACCOUNTS}<br>
     * 1) Checks if:<br>
     * - it has the {@link ArrayList} of the baseusers.<br>
     * 2) For each BaseUser in the list it sets the {@link alon.com.shifter.base_classes.Consts.Fb_Keys#USER_BASE_USER_OBJECT} to the BaseUser object passed.<br>
     * 3) Erases the linker.<br>
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_INFO_FETCHER}<br>
     * The most important linker type, it gets all the data from the server that is used.<br>
     * 1) Checks if:<br>
     * - it has the {@link String} of the site in which the IP address is fetched from.<br>
     * 2) Creates 9 Tasks (2 FinishableTask, 7 TaskResult).<br>
     * 3) First it gets the {@link Shift} object from the server, and compares it the the one in the {@link android.content.SharedPreferences}, if one exists.
     * If they match, it continues to the next task, otherwise it sets the pulled Shift object as the one in the preferences.
     * Then it sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#SHIFT_SCHEDULE_SETTINGS_PULLED} as true and continues to the next task.<br>
     * 4) Next it gets the {@link SpecSettings} object from the server and compares it to the on in the {@link android.content.SharedPreferences}, if one exists.
     * If they match, it continues to the next task, otherwise it sets the pulled SpecSettings object as the one in the preferences.
     * Then it sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#SPEC_SETTINGS_SET_PULLED} as true and then continues to the next task.<br>
     * 5) Next, if the user is the manager it continues to (6), if not it continues to (11).<br>
     * 6) Next it gets a {@link String} of all possible spec settings. it compares it to the {@link android.content.SharedPreferences}, if a value exists.
     * If they match, it continues to the next task, otherwise it sets the pulled string as the one in the preferences.
     * It sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#SPEC_SETTINGS_TYPES_PULLED} to true and continues to the next task..<br>
     * 7) Next it gets a {@link String} of all spec settings expansions. It compares it to the {@link android.content.SharedPreferences}, if a value exists.
     * If they match, it continues to the next task, otherwise it sets the pulled string as the one in the preferences.
     * It sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#SPEC_SETTINGS_EXPANDABLE_INFO_PULLED} to true and continues to the next task.<br>
     * 8) Next it gets a {@link String} of all spec settings restrictions. It compares it to the {@link android.content.SharedPreferences}, if a value exists.
     * If They match, it continues to the next task, otherwise it sets tee pulled string as the one in the preferences.
     * It sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#SPEC_SETTING_RESTS_PULLED} to true and continues to the next task.<br>
     * 9) Next it gets a {@link Set} of all user approval requests. It compares it to the {@link android.content.SharedPreferences}, if a value exists.
     * If they match it continues to the next task, otherwise it sets the pulled set as the one in the preferences.
     * It sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#USERS_RQS_LIST_PULLED} to true and continues to the next task.<br>
     * 10) Next it gets a {@link Set} of all users' UID. It compares it to the {@link android.content.SharedPreferences}, if a value exists.
     * If they match it continues to the next task, otherwise it sets the pulled set as the one in the preferences.
     * It sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#USER_LIST_PULLED} to true and continues to the next task.<br>
     * 11) Next it gets a {@link String} of all job types. It compares it to the {@link android.content.SharedPreferences}, if a value exists.
     * If they match it continues to the next task, otherwise it sets the pulled string as the one in the preferences.
     * It sets {@link alon.com.shifter.base_classes.Consts.Fc_Keys#JOBS_PULLED} to true and continues to the next task.<br>
     * 12) Next it gets the {@link Boolean} value of if a schedule was set, if it was, it sets a {@link android.content.SharedPreferences}
     * value of  {@link alon.com.shifter.base_classes.Consts.Pref_Keys#MGR_SEC_SCHEDULE_SET} to true so that it never checks again,
     * otherwise it prompts the user to set the info.<br>
     * 13) Erases the linker.
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_UPLOAD_SELECTED_SHIFTS_USER}<br>
     * 1) Checks if:<br>
     * - It has the {@link Shift} object.<br>
     * 2)converts the values in the object to a json type,
     * with only the selected values and comments (if a value isn't selected but has a comment, that comment is not registered to be uploaded.<br>
     * 3) Uploads the value to the server.
     * 4) Erases the linker.
     * </p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_GET_SUBMITED_SHIFTS}<br>
     * 1) Checks if:<br>
     * - It has the {@link String} representing the sunday that starts the week.<br>
     * 2) Pulls from the server all the submitted shifts for the date given.<br>
     * 3) Returns the values.
     * 4) Erases the linker.
     * <p>
     * </li>
     * <li>
     * <p>
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_UPLOAD_FILE_SHIFT_ARRGMENT}<br>
     * 1) Checks if:<br>
     * -It has the {@link Arrangement} object.<br>
     * -It has the {@link ProgressDialogFragment} object.<br>
     * 2) Converts the {@link Arrangement} object to an array of bytes using {@link Util#seralizeObject(Serializable)}.<br>
     * 3) Uploads the byte array from the previous point to the firebase-storage using {@link FirebaseUtil#getStorage(String)} with a direction from  {@link alon.com.shifter.base_classes.Consts.Fb_Dirs}.
     * 4) dismisses the dialog.<br>
     * 5) Erases the linker.<br>
     * <p>
     * </p>
     * </li>
     * </ul>
     *
     * @throws InsufficientParametersException
     *         - if the parameters needed to execute the linker doen't exists.
     */
    public void execute() throws InsufficientParametersException {
        try {
            //TODO: REMOVE ALL FINALS
            boolean containsAllRequiredParams;
            switch (mType) {
                //=====================================================================
                case Consts.Linker_Keys.TYPE_LOGIN:
                    containsAllRequiredParams = (mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_DIALOG) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_PASS) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_EMAIL));
                    if (containsAllRequiredParams) {
                        final ProgressDialogFragment mDialog = (ProgressDialogFragment) mParams.get(Consts.Linker_Keys.KEY_LOGIN_DIALOG);
                        final FinishableTask mFinishedUpdatingIP = new FinishableTask() {
                            @Override
                            public void onFinish() {
                                if (mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_REMEMBER_ME)) {
                                    if ((boolean) mParams.get(Consts.Linker_Keys.KEY_LOGIN_REMEMBER_ME)) {
                                        mUtil.writePref(mCaller, Consts.Pref_Keys.LOG_PERMA_LOGIN, true);
                                        mUtil.writePref(mCaller, Consts.Pref_Keys.LOG_PERMA_LOGIN_PASS, mParams.get(Consts.Linker_Keys.KEY_LOGIN_PASS).toString());
                                        mUtil.writePref(mCaller, Consts.Pref_Keys.LOG_PERMA_LOGIN_EMAIL, mParams.get(Consts.Linker_Keys.KEY_LOGIN_EMAIL).toString());
                                    }
                                    mUtil.writePref(mCaller, Consts.Pref_Keys.LOGOUT_TIME_LONG, System.currentTimeMillis() - 20 + (1000 * 60 * 60 * 24));
                                }
                                new BackgroundInfoFetcher().execute();
                                if (FirebaseUtil.getUser().isManager())
                                    mCaller.startActivity(new Intent(mCaller, Activity_Shifter_Main_Manager.class));
                                else
                                    mCaller.startActivity(new Intent(mCaller, Activity_Shifter_Main_User.class));
                                mCaller.finish();
                                erase();
                            }
                        };
                        final TaskResult mFinishedGettingUser = new TaskResult() {
                            @Override
                            public void onFail() {
                                mDialog.dismiss();
                                mCaller.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mCaller, R.string.err_login_failed, Toast.LENGTH_SHORT).show();
                                        setGate(Consts.Fc_Keys.LOGIN_FAILED, true);
                                    }
                                });
                            }

                            @Override
                            public void onSucceed() {
                                mDialog.dismiss();
                                BaseUser mUser = FirebaseUtil.getUser();
                                mUser.setIP((String) Util.getInstance(mCaller).readPref(mCaller, Consts.Pref_Keys.USR_IP, Consts.Strings.NULL));
                                FirebaseUtil.getDatabase(Consts.Fb_Dirs.USERS).child(mUser.getUID())
                                        .child(Consts.Fb_Keys.USER_BASE_USER_OBJECT).setValue(mUser).addOnCompleteListener(mCaller, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mFinishedUpdatingIP.onFinish();
                                    }
                                });
                            }
                        };
                        final TaskResult mLoggedIn = new TaskResult() {
                            @Override
                            public void onFail() {
                                mDialog.dismiss();
                                mCaller.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mCaller, R.string.err_no_user, Toast.LENGTH_SHORT).show();
                                        setGate(Consts.Fc_Keys.LOGIN_FAILED, true);
                                    }
                                });
                            }

                            @Override
                            public void onSucceed() {
                                FirebaseUtil.getUserFromDatabase(mFinishedGettingUser);
                            }
                        };
                        FirebaseUtil.login(mParams.get(Consts.Linker_Keys.KEY_LOGIN_EMAIL).toString(), mParams.get(Consts.Linker_Keys.KEY_LOGIN_PASS).toString(), mCaller, mLoggedIn);
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_REGISTER:
                    containsAllRequiredParams = (mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_DIALOG) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_WORKPLACE_CODE) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_EMAIL) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_PERSONAL_NAME) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_PASS) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_PHONE));
                    if (containsAllRequiredParams) {
                        final ProgressDialogFragment mDialog = (ProgressDialogFragment) mParams.get(Consts.Linker_Keys.KEY_LOGIN_DIALOG);
                        final FinishableTask mFinishedProcess = new FinishableTask() {
                            @Override
                            public void onFinish() {
                                if (mParams.containsKey(Consts.Linker_Keys.KEY_LOGIN_REMEMBER_ME)) {
                                    mUtil.writePref(mCaller, Consts.Pref_Keys.LOG_PERMA_LOGIN, true);
                                    mUtil.writePref(mCaller, Consts.Pref_Keys.LOG_PERMA_LOGIN_EMAIL, mParams.get(Consts.Linker_Keys.KEY_LOGIN_EMAIL));
                                    mUtil.writePref(mCaller, Consts.Pref_Keys.LOG_PERMA_LOGIN_PASS, mParams.get(Consts.Linker_Keys.KEY_LOGIN_PASS));
                                }
                                mUtil.writePref(mCaller, Consts.Pref_Keys.LOGOUT_TIME_LONG, System.currentTimeMillis() - 20 + (1000 * 60 * 60 * 24));
                                new BackgroundInfoFetcher().execute();
                                if (FirebaseUtil.getUser().isManager())
                                    mCaller.startActivity(new Intent(mCaller, Activity_Shifter_Main_Manager.class));
                                else {
                                    FirebaseUtil.insertRequest(FirebaseUtil.getUser());
                                    mCaller.startActivity(new Intent(mCaller, Activity_Shifter_Main_User.class));
                                }
                                if (mTaskOnFinish != null) {
                                    mTaskOnFinish.onFinish();
                                    mTaskOnFinish = null;
                                }
                                erase();
                            }
                        };
                        final TaskResult mRegistered = new TaskResult() {
                            @Override
                            public void onFail() {
                                mDialog.dismiss();
                                mCaller.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mCaller, R.string.register_err_unspecefied_err, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onSucceed() {
                                FirebaseUtil.getShorthandCode(new FinishableTask() {
                                    @Override
                                    public void onFinish() {
                                        BaseUser mUser =
                                                new BaseUser(FirebaseUtil.getUser().getUID(),
                                                        mParams.get(Consts.Linker_Keys.KEY_LOGIN_PERSONAL_NAME).toString(),
                                                        mParams.get(Consts.Linker_Keys.KEY_LOGIN_PHONE).toString(),
                                                        Util.getInstance(mCaller).readPref(mCaller, Consts.Pref_Keys.USR_IP, Consts.Strings.NULL).toString(),
                                                        FirebaseUtil.getUser().getSHCode(),
                                                        mParams.get(Consts.Linker_Keys.KEY_LOGIN_EMAIL).toString(), false,
                                                        mParams.get(Consts.Linker_Keys.KEY_LOGIN_WORKPLACE_CODE).toString());
                                        FirebaseUtil.setUser(mUser);
                                        FirebaseUtil.getDatabase(Consts.Fb_Dirs.USERS).child(mUser.getUID())
                                                .child(Consts.Fb_Keys.USER_BASE_USER_OBJECT).setValue(mUser).addOnCompleteListener(mCaller, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                mFinishedProcess.onFinish();
                                            }
                                        });
                                    }
                                });
                            }
                        };
                        final TaskResult mEmailVerified = new TaskResult() {
                            @Override
                            public void onFail() {
                                mDialog.dismiss();
                                mCaller.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mCaller, R.string.register_err_unspecefied_err, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onSucceed() {
                                mDialog.setMessage(mCaller.getString(R.string.register_dialog_adding_you));
                                FirebaseUtil.register(mParams.get(Consts.Linker_Keys.KEY_LOGIN_EMAIL).toString(), mParams.get(Consts.Linker_Keys.KEY_LOGIN_PASS).toString(), mCaller, mRegistered);
                            }
                        };
                        TaskResult mVerifiedCode = new TaskResult() {
                            @Override
                            public void onFail() {
                                mDialog.dismiss();
                                mCaller.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mCaller, R.string.register_err_failed_to_validate_wrkplace_code, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onSucceed() {
                                mDialog.setMessage(mCaller.getString(R.string.register_dialog_checking_email));
                                Util.getInstance(mCaller).validateEmail(mParams.get(Consts.Linker_Keys.KEY_LOGIN_EMAIL).toString(), mEmailVerified);
                            }
                        };
                        FirebaseUtil.verifyCode((String) mParams.get(Consts.Linker_Keys.KEY_LOGIN_WORKPLACE_CODE), mVerifiedCode);
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_UPLOAD_SHIFT_SCHEDULE:
                    containsAllRequiredParams = (mParams.containsKey(Consts.Linker_Keys.KEY_SHIFT_UPLOAD_SHIFT_OBJECT) && mParams.containsKey(Consts.Linker_Keys.KEY_SHIFT_UPLOAD_DIALOG));
                    if (containsAllRequiredParams) {
                        final Shift mShift = (Shift) mParams.get(Consts.Linker_Keys.KEY_SHIFT_UPLOAD_SHIFT_OBJECT);
                        final TwoButtonDialogFragment mDialog = (TwoButtonDialogFragment) mParams.get(Consts.Linker_Keys.KEY_SHIFT_UPLOAD_DIALOG);
                        try {
                            JSONObject mJson = new JSONObject();
                            ShiftInfo[] mInfo = mShift.getInfoComplete();
                            for (int i = 0; i < mInfo.length; i++) {
                                String day = mUtil.getDayString(mCaller, i + 1);
                                JSONObject mSubDay = new JSONObject();
                                for (int j = 0; j < 4; j++)
                                    mSubDay.put(mUtil.getShiftTitle(mCaller, j), mInfo[i].getFor(j));
                                mJson.put(day, mSubDay);
                            }
                            final FinishableTask mFinishedUploadingSpecSettings = new FinishableTask() {
                                @Override
                                public void onFinish() {
                                    FirebaseUtil.getDatabase(MGR_SEC).child(FirebaseUtil.getUser().getCode()).child(Consts.Fb_Keys.MGR_SEC_SCHEDULE_SET).setValue(true);
                                    mUtil.writePref(mCaller, Consts.Pref_Keys.MGR_SEC_SCHEDULE_SET, true);
                                    mUtil.writeObject(mCaller, Consts.Strings.FILE_SHIFT_OBJECT, mShift);
                                    setGate(Consts.Fc_Keys.SCHDULE_INFO_UPLOADED, true);
                                    if (mTaskOnFinish != null) {
                                        mTaskOnFinish.onFinish();
                                        mTaskOnFinish = null;
                                    }
                                    erase();
                                }
                            };
                            FirebaseUtil.getDatabase(MGR_SEC).child(FirebaseUtil.getUser().getCode()).child(Consts.Fb_Keys.MGR_SEC_SHIFT_SCHEDULE)
                                    .setValue(mJson.toString()).addOnCompleteListener(mCaller, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        mFinishedUploadingSpecSettings.onFinish();
                                    else {
                                        Log.e(TAG, "onComplete: failed", task.getException());
                                        mDialog.dismiss();
                                        setGate(Consts.Fc_Keys.SCHDULE_INFO_UPLOADED, false);
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_INFO_FETCHER:
                    containsAllRequiredParams = (mParams.containsKey(Consts.Linker_Keys.KEY_IP_FETCH_ADDR));
                    if (containsAllRequiredParams) {
                        //IP Update Call Block
                        FinishableTaskWrapper ipFetcher = new FinishableTaskWrapper() {
                            @Override
                            public void onFinish() {
                                String ipFetcherSite = getWrapperParam(WRAPPER_STRING_IP_SITE).toString();
                                try {
                                    URL url = new URL(ipFetcherSite);
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    InputStream stream = conn.getInputStream();
                                    byte[] buffer = new byte[1024];
                                    int aRead = stream.read(buffer);
                                    conn.disconnect();
                                    byte[] input = Arrays.copyOf(buffer, aRead);
                                    String ip = new String(input);
                                    mUtil.writePref(mCaller, Consts.Pref_Keys.USR_IP, ip);
                                    setGate(Consts.Fc_Keys.USER_IP_PULLED, true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        ipFetcher.setWrapperParam(WRAPPER_STRING_IP_SITE, mParams.get(Consts.Linker_Keys.KEY_IP_FETCH_ADDR));
                        FinishableTaskWrapper standardFBCall = new FinishableTaskWrapper() {
                            @Override
                            public void onFinish() {
                                String specRestrics;
                                String specSettingsComplete;
                                boolean hasSSRestrictions = (specRestrics = mUtil.readPref(mCaller, Consts.Pref_Keys.MGR_SEC_SPEC_SETTINGS_RESTRICTIONS, Consts.Strings.NULL).toString())
                                        != null &&
                                        !specRestrics.isEmpty() &&
                                        !specRestrics.equals(Consts.Strings.NULL);
                                boolean hasCompleteSpecSettings = (specSettingsComplete = mUtil.readPref(mCaller, Consts.Pref_Keys.USR_SPEC_SETTINGS_RESTS_AND_NAMES, Consts.Strings.NULL).toString())
                                        != null &&
                                        !specSettingsComplete.isEmpty() &&
                                        !specSettingsComplete.equals(Consts.Strings.NULL);

                            }
                        };
                        FinishableTaskWrapper managerFBCall = new FinishableTaskWrapper() {

                            @Override
                            @SuppressWarnings("unchecked")
                            public void onFinish() {
                                boolean hasScheduleSet = (boolean) mUtil.readPref(mCaller, Consts.Pref_Keys.MGR_SEC_SCHEDULE_SET, false);
                                boolean hasSubmissionCap = mUtil.readPref(mCaller, Consts.Pref_Keys.MGR_SEC_SUBMISSION_CAP, Consts.Strings.NULL)
                                        .equals(Consts.Strings.NULL);
                                boolean hasSpecialSettingsInfo = mUtil.readPref(mCaller, Consts.Pref_Keys.USR_SPEC_SETTINGS_RESTS_AND_NAMES, Consts.Strings.NULL)
                                        .equals(Consts.Strings.NULL);
                                boolean hasUserList = !((HashSet<String>) mUtil.readPref(mCaller, Consts.Pref_Keys.MGR_SEC_USER_LIST, new HashSet<String>())).isEmpty();

                                ValueEventListenerWrapper firebaseWrapper = new ValueEventListenerWrapper() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        boolean scheduleSet = (boolean) getWrapperParam(WRAPPER_FB_BOOLEAN_HAS_SCHEDULE_SET);
                                        boolean submissionCapSet = (boolean) getWrapperParam(WRAPPER_FB_BOOLEAN_HAS_SUBMISSION_CAP);
                                        boolean specSettingsSet = (boolean) getWrapperParam(WRAPPER_FB_BOOLEAN_HAS_SPEC_SETTINGS_INFO);
                                        boolean userListSet = (boolean) getWrapperParam(WRAPPER_FB_BOOLEAN_HAS_USER_LIST);
                                        SpecSettings specSettingsInfo = SpecSettings.getEmpty();
                                        String submissionCap = "";
                                        String schedule = "";
                                        Set<String> currentUsers = new HashSet<>();
                                        if (scheduleSet)
                                            schedule = getWrapperParam(WRAPPER_FB_STRING_SHIFT_INFO).toString();
                                        if (specSettingsSet)
                                            specSettingsInfo = (SpecSettings) getWrapperParam(WRAPPER_FB_STRING_SPEC_SETTINGS);
                                        if (submissionCapSet)
                                            submissionCap = getWrapperParam(WRAPPER_STRING_SHIFT_SUBMISSION_CAP).toString();
                                        if (userListSet)
                                            currentUsers = (Set<String>) getWrapperParam(WRAPPER_FB_SET_USER_LIST);

                                        boolean updateSchedule = false;

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            switch (snapshot.getKey()) {
                                                case Consts.Fb_Keys.MGR_SEC_USERS_ACCEPTED:
                                                    HashSet<String> userSet = new HashSet<>();
                                                    userSet.addAll(Arrays.asList(snapshot.getValue().toString().split("~")));
                                                    if (!userSet.equals(currentUsers)) {
                                                        mUtil.writePref(mCaller, Consts.Pref_Keys.MGR_SEC_USER_LIST, userSet);
                                                        Log.i(TAG, "onDataChange: managerCall -> Accepted Users Updated.");
                                                    } else
                                                        Log.i(TAG, "onDataChange: managerCall -> Accepted Users Not Updated.");
                                                    setGate(Consts.Fc_Keys.USER_LIST_PULLED, true);
                                                    break;
                                                case Consts.Fb_Keys.MGR_SEC_SCHEDULE_SET:
                                                    boolean isSet = (boolean) snapshot.getValue();
                                                    if (isSet != scheduleSet) {
                                                        mUtil.writePref(mCaller, Consts.Pref_Keys.MGR_SEC_SCHEDULE_SET, isSet);
                                                        Log.i(TAG, "onDataChange: managerCall -> Schedule Set Updated.");
                                                    } else
                                                        Log.i(TAG, "onDataChange: managerCall -> Schedule Set Not Updated.");
                                                    if (!isSet) {
                                                        Log.i(TAG, "onDataChange: managerCall -> Schedule needs update.");
                                                        updateSchedule = true;
                                                    }
                                                    break;
                                                case Consts.Fb_Keys.MGR_SEC_SPEC_SETTINGS_SET:
                                                    try {
                                                        SpecSettings serverSpecSettings = SpecSettings.fromString(snapshot.getValue().toString());
                                                        if (!specSettingsInfo.equals(serverSpecSettings)) {
                                                            mUtil.writePref(mCaller, Consts.Pref_Keys.USR_SPEC_SETTINGS_OBJECT, serverSpecSettings);
                                                            Log.i(TAG, "onDataChange: managerCall -> SpecSettings Object Updated.");
                                                        } else
                                                            Log.i(TAG, "onDataChange: managerCall -> SpecSettings Object Not Updated.");
                                                    } catch (JSONException e) {
                                                        Log.e(TAG, "onDataChange: managerCall -> Spec Settings from server cause error >>", e);
                                                    }
                                                    break;
                                                case Consts.Fb_Keys.MGR_SEC_SHIFT_SCHEDULE:
                                                    String scheduleInServer = snapshot.getValue().toString();
                                                    if (!schedule.equals(scheduleInServer)) {
                                                        mUtil.writePref(mCaller, Consts.Pref_Keys.USR_SHIFT_SCHEDULE, scheduleInServer);
                                                        mUtil.writePref(mCaller, Consts.Pref_Keys.MGR_SEC_SCHEDULE_SET, true);
                                                        Log.i(TAG, "onDataChange: managerCall -> Shift Schedule Updated.");
                                                    } else
                                                        Log.i(TAG, "onDataChange: managerCall -> Shift Schedule Not Updated.");
                                                    setGate(Consts.Fc_Keys.SHIFT_SCHEDULE_SETTINGS_PULLED, true);
                                                    break;
                                                case Consts.Fb_Keys.MGR_SEC_SUBMISSION_CAP:
                                                    String cap = snapshot.getValue().toString();
                                                    if (!cap.equals(submissionCap)) {
                                                        mUtil.writePref(mCaller, Consts.Pref_Keys.MGR_SEC_SUBMISSION_CAP, cap);
                                                        Log.i(TAG, "onDataChange: managerCall -> Submission Cap Updated.");
                                                    } else
                                                        Log.i(TAG, "onDataChange: managerCall -> Submission Cap Not Updated.");
                                                    setGate(Consts.Fc_Keys.SUBMISSION_CAP_PULLED, true);
                                                    break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, "onCancelled: managerCall -> Error.", databaseError.toException());
                                    }
                                };

                                firebaseWrapper
                                        .setWrapperParam(WRAPPER_FB_BOOLEAN_HAS_SCHEDULE_SET, hasScheduleSet)
                                        .setWrapperParam(WRAPPER_FB_BOOLEAN_HAS_SPEC_SETTINGS_INFO, hasSpecialSettingsInfo)
                                        .setWrapperParam(WRAPPER_FB_BOOLEAN_HAS_SUBMISSION_CAP, hasSubmissionCap)
                                        .setWrapperParam(WRAPPER_FB_BOOLEAN_HAS_USER_LIST, hasUserList);
                                if (hasScheduleSet)
                                    firebaseWrapper
                                            .setWrapperParam(WRAPPER_FB_STRING_SHIFT_INFO,
                                                    mUtil.readPref(mCaller, Consts.Pref_Keys.USR_SHIFT_SCHEDULE, Consts.Strings.NULL));
                                if (hasSubmissionCap)
                                    firebaseWrapper
                                            .setWrapperParam(WRAPPER_STRING_SHIFT_SUBMISSION_CAP,
                                                    mUtil.readPref(mCaller, Consts.Pref_Keys.MGR_SEC_SUBMISSION_CAP, Consts.Strings.NULL));
                                if (hasSpecialSettingsInfo)
                                    firebaseWrapper
                                            .setWrapperParam(WRAPPER_FB_STRING_SPEC_SETTINGS,
                                                    mUtil.readPref(mCaller, Consts.Pref_Keys.USR_SPEC_SETTINGS_OBJECT, SpecSettings.getEmpty()));
                                if (hasUserList)
                                    firebaseWrapper.setWrapperParam(WRAPPER_FB_SET_USER_LIST, mUtil.readPref(mCaller, Consts.Pref_Keys.MGR_SEC_USER_LIST, new HashSet<String>()));
                                FirebaseUtil.getDatabase(Consts.Fb_Dirs.MGR_SEC).child(FirebaseUtil.getUser().getCode()).addListenerForSingleValueEvent(firebaseWrapper);
                            }
                        };
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_GET_PHONE_NUMBER:
                    containsAllRequiredParams = (mParams.containsKey(Consts.Linker_Keys.KEY_NUMBER_FETCHER_DIALOG) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_NUMBER_FETCHER_DIALOG_VER) &&
                            mParams.containsKey(Consts.Linker_Keys.KEY_NUMBER_FETCHER_TO));
                    if (containsAllRequiredParams) {
                        final int verCode = new Random().nextInt(100000000);
                        final String action = "android.provider.Telephony.SMS_RECEIVED";

                        final ProgressDialogFragment mDialog = (ProgressDialogFragment) mParams.get(Consts.Linker_Keys.KEY_NUMBER_FETCHER_DIALOG);
                        final DialogFragment mVerDialog = (DialogFragment) mParams.get(Consts.Linker_Keys.KEY_NUMBER_FETCHER_DIALOG_VER);

                        String toNumber = mParams.get(Consts.Linker_Keys.KEY_NUMBER_FETCHER_TO).toString();
                        final String finalToNumber = toNumber;
                        final TaskResult mResult = new TaskResult() {
                            @Override
                            public void onFail() {
                                mDialog.dismiss();
                                mCaller.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mCaller, R.string.phone_num_dialog_err_not_correct_number, Toast.LENGTH_SHORT).show();
                                    }
                                });
                                erase();
                            }

                            @Override
                            public void onSucceed() {
                                mDialog.dismiss();
                                mVerDialog.dismiss();
                                mUtil.writePref(mCaller, Consts.Pref_Keys.USR_NUMBER, finalToNumber);
                                if (mTaskOnFinish != null) {
                                    mTaskOnFinish.onFinish();
                                    mTaskOnFinish = null;
                                }
                                erase();
                            }
                        };
                        BroadcastReceiver mSmsReceieved = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (intent.getAction().equals(action)) {
                                    try {
                                        Bundle extras = intent.getExtras();
                                        SmsMessage[] msgs;
                                        String smsFrom;
                                        if (extras != null) {
                                            Object[] pdus = (Object[]) extras.get("pdus");
                                            if (pdus != null) {
                                                msgs = new SmsMessage[pdus.length];
                                                for (int i = 0; i < msgs.length; i++) {
                                                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                                    smsFrom = msgs[i].getOriginatingAddress();
                                                    if (smsFrom.startsWith("+972"))
                                                        smsFrom = smsFrom.replace("+972", "0");
                                                    else
                                                        throw new RuntimeException("Unknown country code.");
                                                    String smsBody = msgs[i].getMessageBody();
                                                    if (smsFrom.equals(finalToNumber) && smsBody.equals(Integer.toString(verCode))) {
                                                        mDialog.dismiss();
                                                        context.unregisterReceiver(this);
                                                        mResult.onSucceed();
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "onReceive: failed", e);
                                        mResult.onFail();
                                    }
                                }
                            }
                        };
                        mCaller.registerReceiver(mSmsReceieved, new IntentFilter(action));
                        SmsManager smsMgr = SmsManager.getDefault();
                        smsMgr.sendTextMessage(toNumber, null, Integer.toString(verCode), null, null);
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_UPLOAD_SPEC_SETTINGS:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_SPEC_SETTINGS);
                    if (containsAllRequiredParams)
                        if (!mParams.get(Consts.Linker_Keys.KEY_SPEC_SETTINGS).equals(SpecSettings.getEmpty()))
                            FirebaseUtil.getDatabase(MGR_SEC)
                                    .child(FirebaseUtil.getUser().getCode()).child(Consts.Fb_Keys.MGR_SEC_SPEC_SETTINGS_SET)
                                    .setValue((mParams.get(Consts.Linker_Keys.KEY_SPEC_SETTINGS)).toString()).addOnCompleteListener(mCaller, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isComplete())
                                        erase();
                                }
                            });
                        else
                            throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_DELETE_ACCOUNT:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_DELETE_USER_BASEUSER_OBJECT);
                    if (containsAllRequiredParams) {
                        final BaseUser mUser = (BaseUser) mParams.get(Consts.Linker_Keys.KEY_DELETE_USER_BASEUSER_OBJECT);
                        final FinishableTaskWithParams mTask = new FinishableTaskWithParams() {
                            @Override
                            public void onFinish() {
                                String mAccepted = (String) getParamsFromTask().get(Consts.Param_Keys.KEY_ACCEPTED_USERS_STRING);
                                String newUsers = "";
                                String[] mParts = mAccepted.split(mUser.getUID());
                                boolean rightIsEmpty = mParts[1].length() > 1;
                                boolean leftIsEmpty = mParts[0].length() > 1;
                                if (rightIsEmpty && leftIsEmpty)
                                    newUsers = "";
                                else if (rightIsEmpty)
                                    newUsers += mParts[1];
                                else if (leftIsEmpty)
                                    newUsers += mParts[0];
                                else
                                    newUsers += mParts[0] + mParts[1].substring(1);
                                FirebaseUtil.getDatabase(MGR_SEC).child(FirebaseUtil.getUser().getUID()).child(Consts.Fb_Keys.MGR_SEC_USERS_ACCEPTED).setValue(newUsers);
                                if (mTaskOnFinish != null) {
                                    mTaskOnFinish.onFinish();
                                    mTaskOnFinish = null;
                                }
                                erase();
                            }
                        };
                        FirebaseUtil.getDatabase(Consts.Fb_Dirs.MGR_SEC_USER_REQUESTS).child(mUser.getUID()).setValue(Consts.Strings.VALUE_DELETE_ACCOUNT);
                        FirebaseUtil.getDatabase(MGR_SEC).child(FirebaseUtil.getUser().getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                                    if (mSnap.getKey().equals(Consts.Fb_Keys.MGR_SEC_USERS_ACCEPTED)) {
                                        String mAccepted = mSnap.getValue().toString();
                                        mTask.addParamToTask(Consts.Param_Keys.KEY_ACCEPTED_USERS_STRING, mAccepted);
                                        mTask.onFinish();
                                        return;
                                    }
                                }
                                Log.i(TAG, "onDataChange: Didn't find account to delete.");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled: deleting account failed", databaseError.toException());
                            }
                        });
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_UPDATE_USER_ACCOUNTS:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_USER_UPDATE_LIST);
                    if (containsAllRequiredParams) {
                        @SuppressWarnings("unchecked")
                        ArrayList<BaseUser> mUsers = (ArrayList<BaseUser>) mParams.get(Consts.Linker_Keys.KEY_USER_UPDATE_LIST);
                        for (BaseUser user : mUsers)
                            FirebaseUtil.getDatabase(Consts.Fb_Dirs.USERS).child(user.getUID()).child(Consts.Fb_Keys.USER_BASE_USER_OBJECT).setValue(user);
                        if (mTaskOnFinish != null) {
                            mTaskOnFinish.onFinish();
                            mTaskOnFinish = null;
                        }
                        erase();
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_UPLOAD_SELECTED_SHIFTS_USER:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_SHIFT_UPLOAD_SHIFT_OBJECT);
                    if (containsAllRequiredParams) {
                        Shift mShift = (Shift) mParams.get(Consts.Linker_Keys.KEY_SHIFT_UPLOAD_SHIFT_OBJECT);
                        JSONObject masterJSON = new JSONObject();
                        ShiftInfo[] info = mShift.getInfoComplete();
                        Comment[] comments = mShift.getComments();
                        try {
                            for (int i = 0; i < info.length; i++) {
                                JSONArray shiftRqsArr = new JSONArray();
                                for (int j = 0; j < 4; j++) {
                                    boolean requested = info[i].getForBool(j);
                                    JSONObject requestedAndValue = new JSONObject();
                                    if (requested)
                                        requestedAndValue.put(Consts.Strings.VALUE_UPLOAD_SHIFT_JSON_KEY, mUtil.getShiftTitle(mCaller, j));
                                    String comment = comments[i].getComment(j);
                                    boolean hasComment = comment != null && !comment.isEmpty();
                                    if (hasComment)
                                        requestedAndValue.put(Consts.Strings.VALUE_UPLOAD_SHIFT_COMMENT_JSON_KEY, comment);
                                    if (requested)
                                        shiftRqsArr.put(requestedAndValue);
                                }
                                masterJSON.put(mUtil.getDayString(mCaller, i + 1), shiftRqsArr);
                            }
                            FirebaseUtil.getDatabase(Consts.Fb_Dirs.SHIFT_SUBMISSIONS).child(FirebaseUtil.getUser().getCode()).child(determineClosestSunday().replace("/", "-")).child(FirebaseUtil.getUser().getUID()).setValue(masterJSON.toString());
                            if (mTaskOnFinish != null) {
                                mTaskOnFinish.onFinish();
                                mTaskOnFinish = null;
                            }
                            erase();
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_GET_SUBMITED_SHIFTS:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_ARRGMENT_DATE_FOR_THE_SHIFT_PULL);
                    if (containsAllRequiredParams) {
                        String date = (String) mParams.get(Consts.Linker_Keys.KEY_ARRGMENT_DATE_FOR_THE_SHIFT_PULL);
                        FirebaseUtil.getDatabase(Consts.Fb_Dirs.SHIFT_SUBMISSIONS).child(FirebaseUtil.getUser().getCode()).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, String> userAndRequestMap = new HashMap<>();
                                int res;
                                boolean started = (res = DatabaseUtil.start(mCaller)) == DB_RESULT_OKAY || res == DB_ERR_ALREADY_STARTED;
                                if (!started) {
                                    Log.e(TAG, "onDataChange: Database not started.");
                                    return;
                                }
                                boolean opened = DatabaseUtil.openWrite() == DB_RESULT_OKAY;
                                if (opened)
                                    for (DataSnapshot snapShot : dataSnapshot.getChildren())
                                        userAndRequestMap.put(snapShot.getKey(), (String) snapShot.getValue());
                                else {
                                    Log.e(TAG, "onDataChange: Database not opened.");
                                    return;
                                }
                                FinishableTaskWithParamsWrapper doneGettingAllBaseUserObjects = new FinishableTaskWithParamsWrapper() {
                                    @Override
                                    @SuppressWarnings("unchecked")
                                    public void onFinish() {
                                        HashMap<String, String> userAndRequestMap = (HashMap<String, String>) getWrapperParam(WRAPPER_HASHMAP_ID);

                                        ArrayList<BaseUser> userList = (ArrayList<BaseUser>) getParamsFromTask().get(Consts.Param_Keys.KEY_BASE_USER_OBJECT_LIST);
                                        ArrayList<JSONObject> requests = new ArrayList<>();

                                        try {
                                            for (BaseUser user : userList) {
                                                JSONObject parsedRequestList = new JSONObject(userAndRequestMap.get(user.getUID()));
                                                parsedRequestList.put(Consts.Strings.VALUE_UPLOAD_SHIFT_RQS_NEXT_SUNDAY, determineClosestSunday());
                                                parsedRequestList.put(Consts.Strings.VALUE_UPLOAD_SHIFT_JSON_USER_NAME_KEY, user.getName());
                                                requests.add(parsedRequestList);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        DatabaseUtil.addUserRequests(mCaller, Arrays.copyOf(requests.toArray(), requests.size(), JSONObject[].class));

                                        if (mTaskOnFinish != null)
                                            if (mTaskOnFinish instanceof FinishableTaskWithParams) {
                                                ((FinishableTaskWithParams) mTaskOnFinish).addParamToTask(Consts.Param_Keys.KEY_BASE_USER_OBJECT_LIST, userList);
                                                mTaskOnFinish.onFinish();
                                            }
                                        erase();
                                    }
                                };
                                doneGettingAllBaseUserObjects.setWrapperParam(WRAPPER_HASHMAP_ID, userAndRequestMap);

                                FirebaseUtil.getUsersByUID(userAndRequestMap.keySet(), doneGettingAllBaseUserObjects);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_UPLOAD_FILE_SHIFT_ARRGMENT:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_ARRGMENT_DIALOG) && mParams.containsKey(Consts.Linker_Keys.KEY_ARRANGEMENT_OBJECT);
                    if (containsAllRequiredParams) {
                        Arrangement arrg = (Arrangement) mParams.get(Consts.Linker_Keys.KEY_ARRANGEMENT_OBJECT);
                        try {
                            byte[] arr = Util.seralizeObject(arrg);
                            FirebaseUtil.getStorage(generateStorageFileNameForArrangement()).putBytes(arr);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            if (ex instanceof NotSerializableException)
                                Toast.makeText(mCaller, mCaller.getString(R.string.err_not_seralizeable), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(mCaller, mCaller.getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
                            throw ex;
                        } finally {
                            ProgressDialogFragment frag = (ProgressDialogFragment) mParams.get(Consts.Linker_Keys.KEY_ARRGMENT_DIALOG);
                            frag.dismiss();
                            erase();
                        }
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_UPLOAD_FILE_SHIFT_HOURS:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_SHIFT_HOURS_OBJECT);
                    if (containsAllRequiredParams) {
                        ShiftHours hours = (ShiftHours) mParams.get(Consts.Linker_Keys.KEY_SHIFT_HOURS_OBJECT);
                        try {
                            byte[] bytes = Util.seralizeObject(hours);
                            FirebaseUtil.getStorage(generateStorageFileNameForShiftHours()).putBytes(bytes);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            if (ex instanceof NotSerializableException)
                                Toast.makeText(mCaller, mCaller.getString(R.string.err_not_seralizeable), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(mCaller, mCaller.getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
                            throw ex;
                        }
                        erase();
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                case Consts.Linker_Keys.TYPE_UPLOAD_SHIFT_SUBMISSION_CAP:
                    containsAllRequiredParams = mParams.containsKey(Consts.Linker_Keys.KEY_SHIFT_SUBMISSION_CAP_STRING);
                    if (containsAllRequiredParams) {
                        FirebaseUtil
                                .getDatabase(Consts.Fb_Dirs.MGR_SEC)
                                .child(FirebaseUtil.getUser().getCode())
                                .child(Consts.Fb_Keys.MGR_SEC_SHIFT_SUBMISSION_CAP)
                                .setValue(mParams.get(Consts.Linker_Keys.KEY_SHIFT_SUBMISSION_CAP_STRING));
                        erase();
                    } else
                        throw new InsufficientParametersException();
                    break;
                //=====================================================================
                default:
                    throw new RuntimeException("Tried to execute linker with a non-recognizable type-int");
            }
        } catch (Exception e) {
            e.printStackTrace();
            erase();
        }
    }

    public static String generateStorageFileNameForShiftHours() {
        return Consts.Fb_Dirs.STORAGE_SHIFT_HOURS + "~" + FirebaseUtil.getUser().getCode();
    }

    public static String generateStorageFileNameForArrangement() {
        return Consts.Fb_Dirs.STORAGE_ARRAGNEMENT + "~" + FirebaseUtil.getUser().getCode() + "~" + determineClosestSunday();
    }


    /**
     * Adds a parameter to {@link #mParams}.
     *
     * @param key
     *         - The key of the parameter, must be of type {@link alon.com.shifter.base_classes.Consts.Linker_Keys}
     * @param param
     *         - The parameter
     *
     * @return - The {@link Linker}
     */
    public Linker addParam(String key, Object param) {
        mParams.put(key, param);
        return this;
    }

    public void setOnFinish(FinishableTask task) {
        mTaskOnFinish = task;
    }

    public static class ProductionLineException extends Exception {
        ProductionLineException() {
            super("Tried to get an object from a factory line, when the limit of objects has already been reached. Call the erase method to free a slot.");
        }
    }

    public class InsufficientParametersException extends Exception {
        InsufficientParametersException() {
            super(Linker.this.toString() + " doesn't have all required params for execute(), TYPE:: >>" + mType);
        }
    }

    public class BackgroundInfoFetcher extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                setGate(Consts.Fc_Keys.LOGIN_OR_REGISTER_FINISHED, true);
                Linker linker = getLinker(mCaller, Consts.Linker_Keys.TYPE_INFO_FETCHER);
                Log.i(TAG, "doInBackground: Started linker for background info gathering");
                linker.addParam(Consts.Linker_Keys.KEY_IP_FETCH_ADDR, "http://bot.whatismyipaddress.com/");
                linker.execute();
            } catch (ProductionLineException | InsufficientParametersException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
