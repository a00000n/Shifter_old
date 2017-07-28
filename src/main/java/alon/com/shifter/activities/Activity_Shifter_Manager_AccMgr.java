package alon.com.shifter.activities;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.FinishableTask;
import alon.com.shifter.base_classes.FinishableTaskWithParams;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.base_classes.TaskResult;
import alon.com.shifter.dialog_fragments.ProgressDialogFragment;
import alon.com.shifter.dialog_fragments.UserItemDialogFragment;
import alon.com.shifter.utils.FirebaseUtil;

import static alon.com.shifter.utils.FlowController.addGateOpenListener;
import static alon.com.shifter.utils.FlowController.getIsGateOpen;

public class Activity_Shifter_Manager_AccMgr extends BaseActivity {


    private final ArrayList<User> mUsersList = new ArrayList<>();
    private final ArrayList<String> mApprovedUIDList = new ArrayList<>();
    private ProgressBar mLoading;
    private ListView mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shifter_accmgr);

        getUtil(this);
        TAG = "Shifter_AccMgr";

        boolean gatesOpen = getIsGateOpen(Fc_Keys.USER_LIST_PULLED);
        if (gatesOpen)
            setupUI();
        else {
            final ProgressDialogFragment mDialog = mUtil.generateStandbyDialog(this);
            FinishableTask mTask = new FinishableTask() {
                @Override
                public void onFinish() {
                    mDialog.dismiss();
                    setupUI();
                }
            };
            addGateOpenListener(Fc_Keys.USER_LIST_PULLED, mTask);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setupUI() {
        mUsers = (ListView) findViewById(R.id.MGR_ACCMGR_users);
        mLoading = (ProgressBar) findViewById(R.id.MGR_ACCMGR_loading);


        Button mBack = (Button) findViewById(R.id.MGR_ACCMGR_back);

        mBack.setOnClickListener(this);

        TextView mNoUsers = (TextView) findViewById(R.id.MGR_ACCMGR_no_users);

        final ArrayList<String> mUsersUIDList = new ArrayList<>((HashSet<String>) mUtil.readPref(this, Pref_Keys.MGR_SEC_USER_LIST, new HashSet<>()));
        mApprovedUIDList.addAll(mUsersUIDList);
        ArrayList<String> mRequestsUIDList = new ArrayList<>((HashSet<String>) mUtil.readPref(this, Pref_Keys.MGR_SEC_USER_RQS, new HashSet<>()));

        final UserListAdapter[] mAdapter = {null};
        FinishableTask mTask = new FinishableTask() {
            @Override
            public void onFinish() {
                mAdapter[0] = new UserListAdapter(Activity_Shifter_Manager_AccMgr.this, mUsersList);

                mLoading.setVisibility(View.GONE);
                mUsers.setVisibility(View.VISIBLE);

                if (mAdapter[0] != null && !mUsersList.isEmpty()) {
                    mUsers.setAdapter(mAdapter[0]);
                    mUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            if (mUsersList.get(position).isApproved()) {
                                TaskResult mTask = new TaskResult() {
                                    @Override
                                    public void onFail() {
                                        //Do nothing.
                                    }

                                    @Override
                                    public void onSucceed() {
                                        new DeleteUserTask().execute(mUsersList.get(position).getUser());
                                        mUsersList.remove(position);
                                        mAdapter[0].notifyDataSetChanged();
                                    }
                                };
                                UserItemDialogFragment mDialog = new UserItemDialogFragment();
                                mDialog.setTask(mTask);
                                mDialog.setUser(mUsersList.get(position).getUser());
                                mDialog.setCon(Activity_Shifter_Manager_AccMgr.this);
                                mDialog.setTitle(getString(R.string.mgr_accmgr_title));
                                mDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                                mDialog.show(getFragmentManager(), "");
                            } else
                                Activity_Shifter_Manager_AccMgr.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(Activity_Shifter_Manager_AccMgr.this, R.string.mgr_accmgr_not_approved_yet, Toast.LENGTH_SHORT).show();
                                    }
                                });
                        }
                    });
                    mUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            Activity_Shifter_Manager_AccMgr.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Activity_Shifter_Manager_AccMgr.this, R.string.mgr_accmgr_user_item_long_click, Toast.LENGTH_SHORT).show();
                                }
                            });
                            return true;
                        }
                    });
                }
            }
        };

        if (mUsersUIDList.isEmpty() && mRequestsUIDList.isEmpty()) {
            mLoading.setVisibility(View.GONE);
            mNoUsers.setVisibility(View.VISIBLE);
        } else {
            for (String uid : mRequestsUIDList)
                if (!mUsersUIDList.contains(uid))
                    mUsersUIDList.add(uid);
            generateUserList(mUsersUIDList, mTask);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.MGR_ACCMGR_back) {
            new BackgroundDataUpload().execute(mUsersList);
            mUtil.changeScreen(this, Activity_Shifter_Main_Manager.class);
        }
    }

    private void generateUserList(final ArrayList<String> list, final FinishableTask task) {
        if (list.size() > 0) {
            String uid = list.get(0);
            list.remove(0);
            FinishableTask mTask = new FinishableTask() {
                @Override
                public void onFinish() {
                    generateUserList(list, task);
                }
            };
            mUsersList.add(new User(uid, mApprovedUIDList.contains(uid), mTask));
        } else
            task.onFinish();
    }

    private class UserListAdapter extends ArrayAdapter<User> {

        private Context mCon;

        private ArrayList mUsers;

        UserListAdapter(Context context, ArrayList<User> users) {
            super(context, R.layout.listview_mgr_user_item, users);
            mCon = context;
            mUsers = users;
        }

        @NonNull
        @Override
        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            ViewContainer mContainer;
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater) mCon.getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.listview_mgr_user_item, null);
                TextView name = (TextView) convertView.findViewById(R.id.LV_MGR_UMNGT_title);
                Button accept = (Button) convertView.findViewById(R.id.LV_MGR_UMNGT_accept);
                Button decline = (Button) convertView.findViewById(R.id.LV_MGR_UMNGT_decline);

                mContainer = new ViewContainer();

                mContainer.mName = name;
                mContainer.mAccept = accept;
                mContainer.mDecline = decline;

                convertView.setTag(mContainer);
            } else
                mContainer = (ViewContainer) convertView.getTag();

            final User mUser = (User) mUsers.get(position);
            mContainer.mName.setText(mUser.getUser().getName());
            if (!mUser.isApproved()) {
                mContainer.mAccept.setVisibility(View.VISIBLE);
                mContainer.mDecline.setVisibility(View.VISIBLE);

                mContainer.mAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseUtil.acceptedUser(mUser.getUser());
                        mUser.setApproved(true);
                        UserListAdapter.this.notifyDataSetChanged();
                    }
                });
                mContainer.mDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseUtil.declineUser(mUser.getUser());
                        mUsers.remove(mUser);
                        mUsersList.remove(mUser);
                        UserListAdapter.this.notifyDataSetChanged();
                    }
                });
            } else {
                mContainer.mAccept.setVisibility(View.GONE);
                mContainer.mDecline.setVisibility(View.GONE);
            }
            return convertView;
        }

        class ViewContainer {
            TextView mName;
            Button mAccept;
            Button mDecline;
        }
    }

    private class User {

        private BaseUser mUser;

        private boolean mApproved;

        public User(final String uid, final boolean approved, final FinishableTask task) {
            final FinishableTaskWithParams mTask = new FinishableTaskWithParams() {
                @Override
                public void onFinish() {
                    if (getParamsFromTask().containsKey(Param_Keys.KEY_BASE_USER_OBJECT)) {
                        setUser((BaseUser) getParamsFromTask().get(Param_Keys.KEY_BASE_USER_OBJECT));
                        setApproved(approved);
                        task.onFinish();
                    } else
                        Log.i(TAG, "onFinish: No User found >> " + uid);
                }
            };
            FirebaseUtil.getUserByUID(uid, mTask);
        }

        public User(BaseUser user) {
            setUser(user);
            setApproved(false);
        }

        boolean isApproved() {
            return mApproved;
        }

        void setApproved(boolean mApproved) {
            this.mApproved = mApproved;
        }

        BaseUser getUser() {
            return mUser;
        }

        void setUser(BaseUser mUser) {
            this.mUser = mUser;
        }
    }

    private class DeleteUserTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            BaseUser mUser = (BaseUser) params[0];
            try {
                Linker linker = Linker.getLinker(Activity_Shifter_Manager_AccMgr.this, Linker_Keys.TYPE_DELETE_ACCOUNT);
                linker.addParam(Consts.Linker_Keys.KEY_DELETE_USER_BASEUSER_OBJECT, mUser);
                linker.execute();
            } catch (Linker.ProductionLineException | Linker.InsufficientParametersException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class BackgroundDataUpload extends AsyncTask<Object, Void, Void> {

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Object... params) {
            ArrayList<User> userList = (ArrayList<User>) params[0];
            ArrayList<BaseUser> bUserList = new ArrayList<>();
            for (User mUser : userList)
                bUserList.add(mUser.getUser());
            Linker linker;
            try {
                linker = Linker.getLinker(Activity_Shifter_Manager_AccMgr.this, Linker_Keys.TYPE_UPDATE_USER_ACCOUNTS);
                linker.addParam(Linker_Keys.KEY_USER_UPDATE_LIST, bUserList);
                linker.execute();
            } catch (Linker.InsufficientParametersException | Linker.ProductionLineException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
