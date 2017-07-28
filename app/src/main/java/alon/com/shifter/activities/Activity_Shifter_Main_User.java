package alon.com.shifter.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.FinishableTask;
import alon.com.shifter.base_classes.FinishableTaskWithParams;
import alon.com.shifter.base_classes.TaskResult;
import alon.com.shifter.utils.FirebaseUtil;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;

public class Activity_Shifter_Main_User extends BaseActivity {


    private boolean mApprovedUser;
    private boolean mDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shifter_user);

        TAG = "Shifter_MainUser_UI";
        getUtil(this);
        if (FirebaseUtil.getUser().isManager()) {
            mUtil.changeScreen(this, Activity_Shifter_Main_Manager.class);
            return;
        }

        FinishableTask loginDoneTask = new FinishableTask() {
            @Override
            public void onFinish() {
                FinishableTaskWithParams gotIsApproved = new FinishableTaskWithParams() {
                    @Override
                    public void onFinish() {
                        mApprovedUser = (boolean) getParamsFromTask().get(Param_Keys.KEY_APPROVED_STATE);
                        if (!mApprovedUser) {
                            FinishableTaskWithParams task = new FinishableTaskWithParams() {
                                @Override
                                public void onFinish() {
                                    mDeleted = (boolean) getParamsFromTask().get(Param_Keys.KEY_DELETED_ACCOUNT);
                                    setupUI();
                                }
                            };
                            FirebaseUtil.amIDeleted(task);
                        } else
                            setupUI();
                    }
                };
                FirebaseUtil.amIApproved(gotIsApproved);
            }
        };

        addGateOpenListener(Fc_Keys.LOGIN_OR_REGISTER_FINISHED, loginDoneTask, true);
    }

    @Override
    protected void setupUI() {
        LinearLayout btnContainer = (LinearLayout) findViewById(R.id.USR_btn_container);
        LinearLayout loadingContainer = (LinearLayout) findViewById(R.id.USR_loading_container);

        if (!mApprovedUser) {
            loadingContainer.setVisibility(View.GONE);
            findViewById(R.id.USR_unapproved_user).setVisibility(View.VISIBLE);
            if (mDeleted)
                ((TextView) findViewById(R.id.USR_unapproved_user)).setText(getString(R.string.user_deleted));
            else
                FirebaseUtil.startWaitForApproval(new TaskResult() {
                    @Override
                    public void onFail() {
                        Toast.makeText(Activity_Shifter_Main_User.this, getString(R.string.err_failed_getting_approval), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSucceed() {
                        FinishableTask loginDoneTask = new FinishableTask() {
                            @Override
                            public void onFinish() {
                                FinishableTaskWithParams gotIsApproved = new FinishableTaskWithParams() {
                                    @Override
                                    public void onFinish() {
                                        mApprovedUser = (boolean) getParamsFromTask().get(Param_Keys.KEY_APPROVED_STATE);
                                        if (!mApprovedUser) {
                                            FinishableTaskWithParams task = new FinishableTaskWithParams() {
                                                @Override
                                                public void onFinish() {
                                                    mDeleted = (boolean) getParamsFromTask().get(Param_Keys.KEY_DELETED_ACCOUNT);
                                                    setupUI();
                                                }
                                            };
                                            FirebaseUtil.amIDeleted(task);
                                        } else
                                            setupUI();
                                    }
                                };
                                FirebaseUtil.amIApproved(gotIsApproved);
                            }
                        };
                        loginDoneTask.onFinish();
                    }
                });
            return;
        } else {
            findViewById(R.id.USR_unapproved_user).setVisibility(View.GONE);
            loadingContainer.setVisibility(View.GONE);
            btnContainer.setVisibility(View.VISIBLE);
        }

        Button submit = (Button) findViewById(R.id.USR_submit_shifts);
        Button phonebook = (Button) findViewById(R.id.USR_worker_phone_page);
        Button shifts = (Button) findViewById(R.id.USR_shift_for_week);
        Button disconnect = (Button) findViewById(R.id.USR_disconnect);

        submit.setOnClickListener(this);
        phonebook.setOnClickListener(this);
        shifts.setOnClickListener(this);
        disconnect.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.USR_shift_for_week:

                break;
            case R.id.USR_submit_shifts:
                mUtil.changeScreen(this, Activity_Shifter_User_Submit.class);
                break;
            case R.id.USR_worker_phone_page:
                mUtil.changeScreen(this, Activity_Shifter_User_Phonebook.class);
                break;
            case R.id.USR_disconnect:
                FirebaseUtil.getFirebaseAuth().signOut();
                mUtil.changeScreen(this, Activity_Login.class);
                break;
        }
    }

}
