package alon.com.shifter.dialog_fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import alon.com.shifter.R;

/**
 * Created by Alon on 12/26/2016.
 */

public class TwoButtonDialogFragment extends DialogFragment {


    private String mTitle;
    private String mMsg;

    private String mButtonLeft;
    private String mButtonRight;

    private View.OnClickListener mLeftListener;
    private View.OnClickListener mRightListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.dialog_fragment_two_button, container, false);

        ((TextView) mView.findViewById(R.id.dialog_title)).setText(mTitle);
        ((TextView) mView.findViewById(R.id.DF_TB_message)).setText(mMsg);

        Button left;
        Button right;

        (left = ((Button) mView.findViewById(R.id.DF_TB_btnLeft))).setText(mButtonLeft);
        (right = ((Button) mView.findViewById(R.id.DF_TB_btnRight))).setText(mButtonRight);

        if (mLeftListener != null)
            left.setOnClickListener(mLeftListener);

        if (mRightListener != null)
            right.setOnClickListener(mRightListener);
        return mView;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setMessage(String msg) {
        mMsg = msg;
    }

    public void setButtonLeft(String msg) {
        mButtonLeft = msg;
    }

    public void setButtonRight(String msg) {
        mButtonRight = msg;
    }

    public void setLeftListener(View.OnClickListener listener) {
        mLeftListener = listener;
    }

    public void setRightListener(View.OnClickListener listener) {
        mRightListener = listener;
    }
}
