package alon.com.shifter.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.DelayedTaskExecutor;
import alon.com.shifter.base_classes.FinishableTask;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.dialog_fragments.ProgressDialogFragment;
import alon.com.shifter.utils.FirebaseUtil;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;
import static alon.com.shifter.utils.FlowController.setGate;

public class Activity_Login extends BaseActivity {

    private CheckBox mExistingAcc;

    private EditText mName,//
            mWorkplaceCode, mEmail,//
            mPrimaryPass, mSecPass;

    private TextView mIncompPass;

    private Button mProceed;

    private boolean passTooWeak = false;

    private ProgressDialogFragment mLoginDialog;

    private CheckBox mPermaLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUtil(this);

        if ((long) mUtil.readPref(this, Pref_Keys.LOGOUT_TIME_LONG, -1L) >= System.currentTimeMillis())
            FirebaseUtil.getFirebaseAuth().signOut();
        else {
            new AsyncTask<Object, Void, Void>() {

                @Override
                protected Void doInBackground(Object... params) {
                    try {
                        setGate(Consts.Fc_Keys.LOGIN_OR_REGISTER_FINISHED, true);
                        Linker linker = Linker.getLinker((Activity) params[0], Consts.Linker_Keys.TYPE_INFO_FETCHER);
                        Log.i(TAG, "doInBackground: Started linker for background info gathering");
                        linker.addParam(Consts.Linker_Keys.KEY_IP_FETCH_ADDR, "http://bot.whatismyipaddress.com/");
                        linker.execute();
                    } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute(this);
            if (FirebaseUtil.getUser().isManager())
                mUtil.changeScreen(this, Activity_Shifter_Main_Manager.class);
            else
                mUtil.changeScreen(this, Activity_Shifter_Main_User.class);
        }
        TAG = "Shifter_Login";

        FinishableTask mTask = new FinishableTask() {
            @Override
            public void onFinish() {
                DelayedTaskExecutor.getInstance().purge();
                init();
            }
        };
        addGateOpenListener(Fc_Keys.LOGIN_FAILED, mTask);

        if ((boolean) mUtil.readPref(this, Pref_Keys.LOG_PERMA_LOGIN, false)) {
            generateDialog();
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Linker linker = Linker.getLinker(Activity_Login.this, Linker_Keys.TYPE_LOGIN);
                        linker.addParam(Linker_Keys.KEY_LOGIN_DIALOG, mLoginDialog);
                        linker.addParam(Linker_Keys.KEY_LOGIN_EMAIL, mUtil.readPref(Activity_Login.this, Pref_Keys.LOG_PERMA_LOGIN_EMAIL, Strings.NULL));
                        linker.addParam(Linker_Keys.KEY_LOGIN_PASS, mUtil.readPref(Activity_Login.this, Pref_Keys.LOG_PERMA_LOGIN_PASS, Strings.NULL));
                        linker.execute();
                    } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        mUtil.writePref(Activity_Login.this, Pref_Keys.LOG_PERMA_LOGIN, false);
                        mUtil.writePref(Activity_Login.this, Pref_Keys.LOG_PERMA_LOGIN_EMAIL, Strings.NULL);
                        mUtil.writePref(Activity_Login.this, Pref_Keys.LOG_PERMA_LOGIN_PASS, Strings.NULL);
                    }
                    return null;
                }
            }.execute();
        } else
            init();
    }

    private void generateDialog() {
        mLoginDialog = new ProgressDialogFragment();
        mLoginDialog.setTitle(getString(R.string.logging_in));
        mLoginDialog.setMessage(getString(R.string.please_wait));
        mLoginDialog.setCancelable(false);
        mLoginDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mLoginDialog.show(getFragmentManager(), DialogFragment_Keys.CONNECTION_KEY);
    }

    private void init() {
        setContentView(R.layout.activity_login);
        setupUI();
    }

    @Override
    protected void setupUI() {
        TextView mExistingAccTV, mPermaLoginTV;

        mExistingAcc = (CheckBox) findViewById(R.id.LOG_existing_acc);
        mExistingAccTV = (TextView) findViewById(R.id.LOG_existing_acc_tv);

        mName = (EditText) findViewById(R.id.LOG_name);
        mWorkplaceCode = (EditText) findViewById(R.id.LOG_workplace_code);
        mEmail = (EditText) findViewById(R.id.LOG_email);
        mPrimaryPass = (EditText) findViewById(R.id.LOG_password_main);
        mSecPass = (EditText) findViewById(R.id.LOG_password_secodary);

        mIncompPass = (TextView) findViewById(R.id.LOG_incomp_pass_tv);

        mPermaLogin = (CheckBox) findViewById(R.id.LOG_perma_login);
        mPermaLoginTV = (TextView) findViewById(R.id.LOG_perma_login_tv);

        mProceed = (Button) findViewById(R.id.LOG_proceed);

        mProceed.setOnClickListener(this);
        mExistingAccTV.setOnClickListener(this);

        mExistingAcc.setOnCheckedChangeListener(this);

        mPrimaryPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mExistingAcc.isChecked()) {
                    boolean passMatches = mPrimaryPass.getText().toString().equals(mSecPass.getText().toString());
                    mIncompPass.setVisibility(passMatches ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                passTooWeak = s.toString().length() < 6;
            }
        });

        mSecPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mExistingAcc.isChecked()) {
                    boolean passMatches = mPrimaryPass.getText().toString().equals(mSecPass.getText().toString());
                    mIncompPass.setVisibility(passMatches ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mPermaLogin.setOnCheckedChangeListener(this);

        mPermaLoginTV.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.LOG_existing_acc) {
            int visibility = isChecked ? View.GONE : View.VISIBLE;
            mName.setVisibility(visibility);
            mWorkplaceCode.setVisibility(visibility);
            mSecPass.setVisibility(visibility);
            mProceed.setText(isChecked ? getString(R.string.login_login) : getString(R.string.register_register));
        } else if (buttonView.getId() == R.id.LOG_perma_login)
            if (isChecked)
                Toast.makeText(this, R.string.perma_login_warning, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.LOG_existing_acc_tv)
            mExistingAcc.setChecked(!mExistingAcc.isChecked());
        else if (v.getId() == R.id.LOG_perma_login_tv)
            mPermaLogin.setChecked(!mPermaLogin.isChecked());
        else if (v.getId() == R.id.LOG_proceed) {
            if (mIncompPass.getVisibility() == View.VISIBLE) {
                mIncompPass.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
                return;
            }
            if (passTooWeak) {
                mPrimaryPass.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
                Toast.makeText(this, getString(R.string.prob_weak_pass), Toast.LENGTH_LONG).show();
                return;
            }
            View[] incompViews = new View[]{null, null, null, null, null};
            int count = 0;
            boolean exists = mExistingAcc.isChecked();
            if (!exists) {
                if (mName.getText().toString().isEmpty())
                    incompViews[count++] = mName;
                if (mWorkplaceCode.getText().toString().isEmpty())
                    incompViews[count++] = mWorkplaceCode;
                if (mSecPass.getText().toString().isEmpty())
                    incompViews[count++] = mSecPass;
            }
            if (mEmail.getText().toString().isEmpty())
                incompViews[count++] = mEmail;
            if (mPrimaryPass.getText().toString().isEmpty())
                incompViews[count++] = mPrimaryPass;
            if (count == 0) {
                new RegistrationTask()
                        .execute(mWorkplaceCode.getText().toString(),
                                mName.getText().toString(), mEmail.getText().toString(),
                                mPrimaryPass.getText().toString(), mExistingAcc.isChecked(), mPermaLogin.isChecked());
            } else
                for (View view : incompViews)
                    if (view != null)
                        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        }
    }

    private class RegistrationTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            generateDialog();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Object... params) {
            String workPlaceCode = params[0].toString();
            String name = params[1].toString();
            String email = params[2].toString();
            String password = params[3].toString();
            boolean exists = (boolean) params[4];
            boolean perma = (boolean) params[5];

            Linker linker;
            try {
                if (exists)
                    linker = Linker.getLinker(Activity_Login.this, Linker_Keys.TYPE_LOGIN);
                else {
                    linker = Linker.getLinker(Activity_Login.this, Linker_Keys.TYPE_REGISTER);

                    linker.addParam(Linker_Keys.KEY_LOGIN_WORKPLACE_CODE, workPlaceCode);
                    linker.addParam(Linker_Keys.KEY_LOGIN_PERSONAL_NAME, name);
                    linker.addParam(Linker_Keys.KEY_LOGIN_PHONE, mUtil.readPref(Activity_Login.this, Pref_Keys.USR_NUMBER, Strings.NULL));
                }
                linker.addParam(Linker_Keys.KEY_LOGIN_DIALOG, mLoginDialog);
                linker.addParam(Linker_Keys.KEY_LOGIN_EMAIL, email);
                linker.addParam(Linker_Keys.KEY_LOGIN_PASS, password);
                if (perma)
                    linker.addParam(Linker_Keys.KEY_LOGIN_REMEMBER_ME, true);
                linker.execute();
            } catch (Linker.ProductionLineException e) {
                mLoginDialog.dismiss();
                Toast.makeText(Activity_Login.this, R.string.register_err_unspecefied_err, Toast.LENGTH_SHORT).show();
            } catch (Linker.InsufficientParametersException e) {
                e.printStackTrace();
            }


            return null;
        }

    }

}