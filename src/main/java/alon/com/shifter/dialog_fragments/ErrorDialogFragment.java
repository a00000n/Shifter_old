package alon.com.shifter.dialog_fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import alon.com.shifter.R;

/**
 * Created by Alon on 12/22/2016.
 */

public class ErrorDialogFragment extends DialogFragment {

    private String mTitle;
    private String mMsg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.dialog_fragment_error_submitting, container, false);

        ((TextView) mView.findViewById(R.id.dialog_title)).setText(mTitle);

        ((TextView) mView.findViewById(R.id.DF_ERR_SUBMIT_error_msg)).setText(mMsg);

        mView.findViewById(R.id.DF_ERR_SUBMIT_okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return mView;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }
}
