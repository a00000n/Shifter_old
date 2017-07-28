package alon.com.shifter.dialog_fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.TaskResult;
import alon.com.shifter.utils.Util;

/**
 * Created by Alon on 11/19/2016.
 */

public class UserItemDialogFragment extends DialogFragment {

    private Spinner mJobType;

    private BaseUser mUser;

    private TaskResult deleteOrDont;

    private Context mCon;

    private String mTitle;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.dialog_fragment_user_info, container, false);

        ((TextView) mView.findViewById(R.id.dialog_title)).setText(mTitle);

        TextView mName = (TextView) mView.findViewById(R.id.DF_UI_name);
        TextView mEmail = (TextView) mView.findViewById(R.id.DF_UI_email);
        TextView mPhoneNumber = (TextView) mView.findViewById(R.id.DF_UI_phone);
        mJobType = (Spinner) mView.findViewById(R.id.DF_UI_job_type);
        final Spinner mRating = (Spinner) mView.findViewById(R.id.DF_UI_rating);
        Button mDelete = (Button) mView.findViewById(R.id.DF_UI_delete_user);
        Button mBack = (Button) mView.findViewById(R.id.DF_UI_back);

        String TAG = "UserItemDialogFragment";
        if (mUser == null)
            Log.i(TAG, "onCreateView: >>NO BASEUSER SET!!!<<");
        else if (mCon == null)
            Log.i(TAG, "onCreateView: >>NO CONTEXT SET!!!<<");
        else {
            String name = "\t" + mUser.getName();
            String email = "\t" + mUser.getEmail();
            String phone = "\t" + mUser.getPhoneNum();
            mName.setText(name);
            mEmail.setText(email);
            mPhoneNumber.setText(phone);
            ArrayAdapter<String> mJobTypes = new ArrayAdapter<>(mCon, android.R.layout.simple_list_item_1,
                    Util.getInstance(mCon)
                            .readPref(mCon, Consts.Pref_Keys.GENERAL_INFO_JOB_TYPES, Consts.Strings.NULL).toString().split("~"));
            ArrayAdapter<Integer> mRatings = new ArrayAdapter<>(mCon, android.R.layout.simple_list_item_1, new Integer[]{0, 1, 2, 3, 4, 5});
            mJobType.setAdapter(mJobTypes);
            mRating.setAdapter(mRatings);
            if (mUser.getJobType() != -1)
                mJobType.setSelection(mUser.getJobType());
            if (mUser.getRating() > 0 && mUser.getRating() <= 5)
                mRating.setSelection(mUser.getRating());
            else
                mRating.setSelection(2);
        }

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mJobType.getSelectedItemPosition() != mUser.getJobType())
                    mUser.setJobType(mJobType.getSelectedItemPosition());
                if (mRating.getSelectedItemPosition() != mUser.getRating())
                    mUser.setRating(mRating.getSelectedItemPosition());
                dismiss();
            }
        });

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mCon)
                        .setTitle(R.string.are_you_sure_eng)
                        .setMessage(mCon.getString(R.string.mgr_accmgr_delete_acc_sure))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (deleteOrDont != null)
                                    deleteOrDont.onSucceed();
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
        return mView;
    }

    public void setUser(BaseUser mUser) {
        this.mUser = mUser;
    }

    public void setTask(TaskResult deleteOrDont) {
        this.deleteOrDont = deleteOrDont;
    }

    public void setCon(Context mCon) {
        this.mCon = mCon;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
