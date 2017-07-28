package alon.com.shifter.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseActivity;
import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.FinishableTaskWithParams;
import alon.com.shifter.utils.FirebaseUtil;
import alon.com.shifter.wrappers.FinishableTaskWithParamsWrapper;

import static alon.com.shifter.base_classes.Consts.Param_Keys.KEY_BASE_USER_OBJECT_LIST;
import static alon.com.shifter.base_classes.Consts.Param_Keys.KEY_BASE_USER_STRING_LIST;
import static alon.com.shifter.wrappers.WrapperBase.WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID;

public class Activity_Shifter_User_Phonebook extends BaseActivity {

    private ListView mPhoneBook;
    private ProgressBar mLoading;

    private ArrayList<BaseUser> mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_phonebook);

        getUtil(this);
        TAG = "USER_Phonebook";

        setupUI();
    }

    @Override
    protected void setupUI() {
        mUserList = new ArrayList<>();
        ArrayAdapter<BaseUser> mUserAdapter = new BaseUserAdapter(this, mUserList);

        mPhoneBook = (ListView) findViewById(R.id.USR_PB_book);
        mLoading = (ProgressBar) findViewById(R.id.USR_PB_loading);

        mPhoneBook.setAdapter(mUserAdapter);

        Button mBack = (Button) findViewById(R.id.USR_PB_back);
        mBack.setOnClickListener(this);

        generateUserPhonebook();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.USR_PB_back)
            mUtil.changeScreen(this, Activity_Shifter_Main_User.class);
    }

    private void generateUserPhonebook() {
        FinishableTaskWithParams addUserInfo = new FinishableTaskWithParams() {
            @Override
            @SuppressWarnings("unchecked")
            public void onFinish() {
                ArrayList<BaseUser> users = (ArrayList<BaseUser>) getParamsFromTask().get(KEY_BASE_USER_OBJECT_LIST);
                mLoading.setVisibility(View.GONE);
                mPhoneBook.setVisibility(View.VISIBLE);
                mUserList.addAll(users);
            }
        };
        FinishableTaskWithParamsWrapper baseUserFetcher = new FinishableTaskWithParamsWrapper() {
            @Override
            @SuppressWarnings("unchecked")
            public void onFinish() {
                ArrayList<String> userUIDS = (ArrayList<String>) getParamsFromTask().get(KEY_BASE_USER_STRING_LIST);
                Set<String> uids = new HashSet<>();
                uids.addAll(userUIDS);
                FirebaseUtil.getUsersByUID(uids, (FinishableTaskWithParams) getWrapperParam(WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID));
            }
        };
        baseUserFetcher.setWrapperParam(WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID, addUserInfo);
        FirebaseUtil.getUsersUIDsForManager(baseUserFetcher);

    }

    private class BaseUserAdapter extends ArrayAdapter<BaseUser> {

        private Context mCon;
        private ArrayList<BaseUser> mUsers;

        BaseUserAdapter(Context con, ArrayList<BaseUser> users) {
            super(con, R.layout.listview_user_phonebook, users);
            mCon = con;
            mUsers = users;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewContainer container;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mCon);
                convertView = inflater.inflate(R.layout.listview_user_phonebook, parent, false);
                TextView name = (TextView) convertView.findViewById(R.id.LV_U_PB_name);
                TextView email = (TextView) convertView.findViewById(R.id.LV_U_PB_email);
                TextView phone = (TextView) convertView.findViewById(R.id.LV_U_PB_number);
                container = new ViewContainer();
                container.mName = name;
                container.mEmail = email;
                container.mPhone = phone;
                convertView.setTag(container);
            } else
                container = (ViewContainer) convertView.getTag();
            container.mName.setText(mUsers.get(position).getName());
            container.mEmail.setText(mUsers.get(position).getEmail());
            container.mPhone.setText(mUsers.get(position).getPhoneNum());
            container.mPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setPackage("com.android.phone");
                    intent.setData(Uri.parse("tel:0533345777"));
                    Activity_Shifter_User_Phonebook.this.startActivity(Intent.createChooser(intent, "צלצל לממספר."));
                }
            });
            return convertView;
        }

        class ViewContainer {
            private TextView mName;
            private TextView mEmail;
            private TextView mPhone;
        }
    }
}
