package alon.com.shifter.dialog_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import alon.com.shifter.R;

/**
 * Created by Alon on 2/11/2017.
 */

public class ConnectionProgressDialogFragment extends BaseDialogFragment {

    private View.OnClickListener mCancelListener;
    private String mMsg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_progress_connection, container, false);


        ((TextView) view.findViewById(R.id.DF_BASE_title)).setText(mTitle);
        ((TextView) view.findViewById(R.id.DF_CONN_message)).setText(mMsg);
        view.findViewById(R.id.DF_CONN_cancel).setOnClickListener(mCancelListener);

        return view;
    }

    public void setMessage(String msg) {
        mMsg = msg;
    }

    public void setOnCancelListener(View.OnClickListener listener) {
        mCancelListener = listener;
    }
}
