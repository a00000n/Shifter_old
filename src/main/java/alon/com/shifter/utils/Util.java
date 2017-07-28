package alon.com.shifter.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import alon.com.shifter.R;
import alon.com.shifter.activities.Activity_Login;
import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.FinishableTask;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.base_classes.TaskResult;
import alon.com.shifter.dialog_fragments.ProgressDialogFragment;
import alon.com.shifter.utils_shift.SpecSettings;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;

/**
 * The Util class is used to run everything from email string verification, to write files.
 */
public final class Util implements Consts {
    /**
     * The instance of this class, since it's a Singleton.
     */
    private static Util instance;
    /**
     * The {@link SharedPreferences} name.
     */
    private final String PREF_NAME = "shifter_manager_pref_0X000000f";
    /**
     * The only write mode allowed on this application.
     */
    private final int WRITE_MODE = Context.MODE_PRIVATE;
    private final String TAG = "SHIFTER_util";


    /**
     * The constructor of this class.
     * Since this class is a singleton all functions in the constructor only need to run once for example;
     * Getting the user's phone number (if needed).
     *
     * @param con - the context that ran the util class, since we control the flow in the application we can say with certainty,
     *            that only {@link Activity_Login} will be used to run the constructor.
     */
    private Util(final Context con) {
        if (con instanceof Activity_Login) {
            getPhoneNumber(con);
//
//            if (FirebaseUtil.getFirebaseAuth().getCurrentUser() != null)
//                new BackgroundInfoFetcher().execute((Activity) mCon);

            FinishableTask mTask = new FinishableTask() {
                int gateOpenCount = 0;

                @Override
                public void onFinish() {
                    gateOpenCount++;
                    if (gateOpenCount == 2) new IPUploadTask().execute(con);
                }
            };
            addGateOpenListener(Fc_Keys.LOGIN_OR_REGISTER_FINISHED, mTask);
            addGateOpenListener(Fc_Keys.USER_IP_PULLED, mTask);
        }
    }


    /**
     * Getting the instance of the util class.
     *
     * @param con - the context of the class.
     * @return {@link #instance}.
     */
    public static Util getInstance(Context con) {
        return instance == null ? instance = new Util(con) : instance;
    }

    /**
     * A method used to check if an email matches this pattern:
     * [Any legitimate string for an email]@[Any legitimate string for an email].
     *
     * @param mail       - the address.
     * @param taskResult - the result to be executed once there is a result from the validation.
     */
    public void validateEmail(String mail, TaskResult taskResult) {
        final String mailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern compiled = Pattern.compile(mailPattern);
        if (compiled.matcher(mail).matches()) {
            taskResult.onSucceed();
        } else
            taskResult.onFail();
    }

    /**
     * Read a preference from {@link SharedPreferences}.
     *
     * @param con              - the activity that is calling this function.
     * @param key              - the key, from {@link alon.com.shifter.base_classes.Consts.Pref_Keys}.
     * @param defaultReturnVal - the default return value, used to determine the function used to get the preference.
     * @return - an object of the preference, or the defaultReturnVal if no such preference exists.
     */
    @SuppressWarnings("unchecked")
    public Object readPref(Context con, String key, Object defaultReturnVal) {
        SharedPreferences mPrefs = con.getSharedPreferences(PREF_NAME, WRITE_MODE);
        Object mPrefVal = defaultReturnVal;
        if (defaultReturnVal instanceof Boolean)
            mPrefVal = mPrefs.getBoolean(key, (Boolean) defaultReturnVal);
        else if (defaultReturnVal instanceof String)
            mPrefVal = mPrefs.getString(key, (String) defaultReturnVal);
        else if (defaultReturnVal instanceof Integer)
            mPrefVal = mPrefs.getInt(key, (Integer) defaultReturnVal);
        else if (defaultReturnVal instanceof HashSet)
            mPrefVal = mPrefs.getStringSet(key, (Set<String>) defaultReturnVal);
        else if (defaultReturnVal instanceof SpecSettings) {
            try {
                mPrefVal = SpecSettings.fromString(mPrefs.getString(key, Strings.NULL));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (defaultReturnVal instanceof Long)
            mPrefVal = mPrefs.getLong(key, (Long) defaultReturnVal);
        return mPrefVal;
    }

    /**
     * Write a preference, or override one.
     *
     * @param con  - the activity that is calling this function.
     * @param data - the set of key and value (must be length % 2 == 0).
     */
    @SuppressWarnings("unchecked")
    public void writePref(Context con, Object... data) {
        if (data.length % 2 != 0)
            throw new RuntimeException("Missing either a key or a value in the writePref function for shared-prefereces");
        SharedPreferences mPrefs = con.getSharedPreferences(PREF_NAME, WRITE_MODE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        for (int i = 0; i < data.length; i += 2) {
            int next = i + 1;
            String key = data[i].toString();
            if (data[next] instanceof Boolean)
                mEditor.putBoolean(key, (Boolean) data[next]);
            else if (data[next] instanceof String)
                mEditor.putString(key, (String) data[next]);
            else if (data[next] instanceof Integer)
                mEditor.putInt(key, (Integer) data[next]);
            else if (data[next] instanceof ArrayList)
                mEditor.putStringSet(key, new HashSet<>((ArrayList<String>) data[next]));
            else if (data[next] instanceof HashSet)
                mEditor.putStringSet(key, (Set<String>) data[next]);
            else if (data[next] instanceof SpecSettings)
                mEditor.putString(key, data[next].toString());
            else
                throw new RuntimeException("Type not recognized.");
        }
        mEditor.apply();
    }

    /**
     * Write an object to a file on the phone.
     *
     * @param con      - the context calling the method.
     * @param fileName - the file name, from {@link alon.com.shifter.base_classes.Consts.Strings}.
     * @param data     - the object to be writen to the file.
     */
    public void writeObject(Context con, String fileName, Object data) {
        File mFileDir;
        mFileDir = con.getFilesDir();
        ObjectOutputStream outStream = null;
        try {
            outStream = new ObjectOutputStream(new FileOutputStream(new File(mFileDir, fileName)));
            outStream.writeObject(data);
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
            } catch (IOException ex) {
                Log.e(TAG, "Geez, you really fucked up.\n" + ex.getMessage());
            }
        }
    }

    /**
     * Reads an object from a file on the phone.
     *
     * @param con      - the activity calling this function.
     * @param fileName - the file name, from {@link alon.com.shifter.base_classes.Consts.Strings}.
     * @return - a {@link Serializable} object if a file exists, null if otherwise.
     */
    @Nullable
    public Serializable readObject(Context con, String fileName) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(con.getFilesDir(), fileName)));
            return (Serializable) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @param firstAct - the calling activity.
     * @param secAct   - the call to be loaded.
     * @see Intent#Intent(Context, Class)
     * @see Activity#startActivity(Intent)
     */
    public void changeScreen(Activity firstAct, Class secAct) {
        changeScreen(firstAct, secAct, null);
    }

    /**
     * @param firstAct - the calling activity.
     * @param secAct   - the call to be loaded.
     * @param bundle   - the bundle to add to the intent (see: {@link Intent#putExtras(Bundle)}).
     * @see Intent#Intent(Context, Class)
     * @see Activity#startActivity(Intent)
     */
    public void changeScreen(Activity firstAct, Class secAct, Bundle bundle) {
        Intent screenChange = new Intent(firstAct, secAct);
        if (bundle != null)
            screenChange.putExtras(bundle);
        firstAct.startActivity(screenChange);
        firstAct.finish();
    }


    /**
     * Get a day name in hebrew according to an int, otherwise throw an exception.
     *
     * @param con - the activity calling this function.
     * @param day - the day
     * @return - a string representing the day.
     */
    @NonNull
    public String getDayString(Context con, int day) {
        if (day == 1) return con.getString(R.string.shift_day_sunday);
        else if (day == 2) return con.getString(R.string.shift_day_monday);
        else if (day == 3) return con.getString(R.string.shift_day_tuseday);
        else if (day == 4) return con.getString(R.string.shift_day_wendsday);
        else if (day == 5) return con.getString(R.string.shift_day_thursday);
        else if (day == 6) return con.getString(R.string.shift_day_friday);
        else if (day == 7) return con.getString(R.string.shift_day_saturday);
        throw new RuntimeException("Something went wrong.");
    }

    /**
     * Get a shift name in hebrew, according to an int, otherwise throw an exception.
     *
     * @param con - the activity calling this function.
     * @param num - the shift
     * @return - a string representing the shift title.
     */
    @NonNull
    public String getShiftTitle(Context con, int num) {
        if (num == 0) return con.getString(R.string.shift_title_morn);
        if (num == 1) return con.getString(R.string.shift_title_afr_non);
        if (num == 2) return con.getString(R.string.shift_title_evening);
        if (num == 3) return con.getString(R.string.shift_title_night);
        throw new RuntimeException("Something went wrong.");
    }

    /**
     * Creates a {@link ProgressDialog} that only asks the user to wait until something finishes running.
     *
     * @param con - the activity calling this function.
     * @return - the {@link ProgressDialog}.
     */
    public ProgressDialogFragment generateStandbyDialog(Context con) {
        ProgressDialogFragment mDialog = new ProgressDialogFragment();
        mDialog.setTitle(con.getString(R.string.dialog_getting_data));
        mDialog.setMessage(con.getString(R.string.please_wait));
        mDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mDialog.show(((Activity) con).getFragmentManager(), DialogFragment_Keys.FETCHING_DATA);

        return mDialog;
    }
//
//    private class BackgroundInfoFetcher extends AsyncTask<Object, Void, Void> {
//
//        private final String ipURL = "http://bot.whatismyipaddress.com/";
//
//        @Override
//        protected Void doInBackground(Object... params) {
//            Context mCon = (Context) params[0];
//            try {
//                Linker linker = Linker.getLinker((Activity) mCon, Linker_Keys.TYPE_INFO_FETCHER);
//                linker.addParamToTask(Linker_Keys.KEY_IP_FETCH_ADDR, ipURL);
//                linker.execute();
//            } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }


    /**
     * A function that needs to run only once in the entire application existance on a phone.
     * Used to prompt the user to give us his phone number for later use.
     *
     * @param con - the activity calling the method.
     */
    private void getPhoneNumber(final Context con) {
        if (readPref(con, Pref_Keys.USR_NUMBER, Strings.NULL).equals(Strings.NULL)) {
            View dialogView = LayoutInflater.from(con).inflate(R.layout.dialog_phone_number_input, (ViewGroup) ((Activity) con).findViewById(R.id.LOG_container));
            final AlertDialog mInsertNumber;
            final EditText number = (EditText) dialogView.findViewById(R.id.D_PHI_number);
            final TextView msg = (TextView) dialogView.findViewById(R.id.D_PHI_msg);
            final AlertDialog.Builder builder = new AlertDialog.Builder(con);
            builder.setTitle(con.getString(R.string.phone_num_dialog_please_input_phone_number));
            msg.setText(con.getString(R.string.phone_num_dialog_phone_number_explained));
            builder.setPositiveButton(R.string.done, null);
            builder.setView(dialogView);
            builder.setCancelable(false);
            mInsertNumber = builder.create();

            mInsertNumber.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = mInsertNumber.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (number.getText().toString().isEmpty() || number.getText().toString().length() != 10) {
                                Toast.makeText(con, R.string.phone_num_dialog_err_not_correct_number, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            new NumberFetcher().execute(number.getText().toString(), con, mInsertNumber);
                        }
                    });
                }
            });
            mInsertNumber.show();
        }
    }

    //==============================================================

    /**
     * A class that is used to create a linker in order to handling phone number check.
     */
    private class NumberFetcher extends AsyncTask<Object, Void, Void> {

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Object... params) {
            final String number = params[0].toString();
            final Context mCon = (Context) params[1];
            final AlertDialog mInsertNumber = (AlertDialog) params[2];
            final ProgressDialog[] mVerDialog = new ProgressDialog[1];
            final FinishableTask mFinished = new FinishableTask() {
                @Override
                public void onFinish() {
                    try {
                        Linker linker = Linker.getLinker((Activity) mCon, Linker_Keys.TYPE_GET_PHONE_NUMBER);
                        linker.addParam(Linker_Keys.KEY_NUMBER_FETCHER_DIALOG, mVerDialog[0]);
                        linker.addParam(Linker_Keys.KEY_NUMBER_FETCHER_DIALOG_VER, mInsertNumber);
                        linker.addParam(Linker_Keys.KEY_NUMBER_FETCHER_TO, number);
                        linker.execute();
                    } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                        e.printStackTrace();
                    }
                }
            };
            ((Activity) mCon).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVerDialog[0] = new ProgressDialog(mCon);
                    mVerDialog[0].setTitle(R.string.phone_num_dialog_checking_phone_number);
                    mVerDialog[0].setMessage(mCon.getString(R.string.phone_num_dialog_verifing_phone_num));
                    mVerDialog[0].setCancelable(false);
                    mVerDialog[0].show();
                    mFinished.onFinish();
                }
            });

            return null;
        }
    }

    //==============================================================

    /**
     * A class used to upload the IP of a user to the database, whilst retaining the user's {@link BaseUser} object on the server without change.
     */
    private class IPUploadTask extends AsyncTask<Object, Void, Void> {


        @Override
        protected Void doInBackground(final Object... params) {
            FirebaseUtil.getUserFromDatabase(new TaskResult() {
                @Override
                public void onFail() {
                    Log.i(TAG, "onFail: failed getting user from db.");
                }

                @Override
                public void onSucceed() {
                    BaseUser mUser = FirebaseUtil.getUser();
                    mUser.setIP(readPref((Context) params[0], Pref_Keys.USR_IP, Strings.NULL).toString());
                    FirebaseUtil.getDatabase(Fb_Dirs.USERS).child(mUser.getUID())
                            .child(Fb_Keys.USER_BASE_USER_OBJECT).setValue(mUser);
                }
            });
            return null;
        }
    }
}
