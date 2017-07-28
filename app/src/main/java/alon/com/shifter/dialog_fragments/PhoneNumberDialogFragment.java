package alon.com.shifter.dialog_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import alon.com.shifter.R;
import alon.com.shifter.wrappers.OnClickWrapper;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

/**
 * Created by Alon on 2/2/2017.
 */

public class PhoneNumberDialogFragment extends BaseDialogFragment {


    private View.OnClickListener mSubmitListener;
    private View.OnClickListener mCancelListener;

    private Button mSubmit;
    private EditText mNumber;


    private String mInfoText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_phonenumber, container, false);

        ((TextView) view.findViewById(R.id.DF_BASE_title)).setText(mTitle);

        if (mInfoText != null && !mInfoText.isEmpty())
            ((TextView) view.findViewById(R.id.DF_PN_msg)).setText(mInfoText);

        if (mCancelListener != null)
            view.findViewById(R.id.DF_PN_cancel).setOnClickListener(mCancelListener);

        mSubmit = (Button) view.findViewById(R.id.DF_PN_submit);
        if (mSubmitListener != null)
            mSubmit.setOnClickListener(mSubmitListener);

        mNumber = (EditText) view.findViewById(R.id.DF_PN_number);
        mNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == IME_ACTION_DONE)
                    if (mSubmit.hasOnClickListeners()) {
                        mSubmit.callOnClick();
                        return true;
                    } else
                        throw new RuntimeException("No on click listener set for Submit in PhoneNumberDialogFragment.");
                return false;
            }
        });
        return view;
    }

    public String getNumber() {
        return mNumber.getText().toString();
    }

    public void setSubmitListener(View.OnClickListener listener) {
        mSubmitListener = listener;
    }

    public void setInfo(String info) {
        this.mInfoText = info;
    }

    public void setCancelListener(OnClickWrapper listener) {
        mCancelListener = listener;
    }
}
