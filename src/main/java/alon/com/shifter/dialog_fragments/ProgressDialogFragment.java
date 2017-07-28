package alon.com.shifter.dialog_fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import alon.com.shifter.R;

public class ProgressDialogFragment extends DialogFragment {

    private String mTitle;
    private String mMessage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.dialog_fragment_progress, container, true);

        ((TextView) mView.findViewById(R.id.dialog_title)).setText(mTitle);
        ((TextView) mView.findViewById(R.id.DF_CONN_message)).setText(mMessage);

        return mView;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setMessage(String msg) {
        mMessage = msg;
        if (getView() != null)
            ((TextView) getView().findViewById(R.id.DF_CONN_message)).setText(msg);
    }
}
