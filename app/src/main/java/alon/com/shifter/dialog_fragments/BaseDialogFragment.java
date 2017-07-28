package alon.com.shifter.dialog_fragments;

import android.app.DialogFragment;

/**
 * Created by Alon on 2/1/2017.
 */

public class BaseDialogFragment extends DialogFragment {

    String mTitle;

    public BaseDialogFragment() {
        if (isCancelable())
            setCancelable(false);
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
