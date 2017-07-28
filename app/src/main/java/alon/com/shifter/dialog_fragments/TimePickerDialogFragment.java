package alon.com.shifter.dialog_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.Consts;

public class TimePickerDialogFragment extends BaseDialogFragment {

    private TimePicker mPicker;

    private View.OnClickListener mLeftListener;
    private View.OnClickListener mRightListener;

    private String mInfoTxt;
    private String mPreviousTime = "";

    private int hour = -1;
    private int min = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_time_picker, container, false);

        ((TextView) view.findViewById(R.id.DF_BASE_title)).setText(mTitle);

        mPicker = (TimePicker) view.findViewById(R.id.DF_TP_picker);
        mPicker.setIs24HourView(true);
        if (hour != -1 && min != -1) {
            mPicker.setCurrentHour(hour);
            mPicker.setCurrentMinute(min);
        }

        TextView mInfo = (TextView) view.findViewById(R.id.DF_TP_info);
        if (mInfoTxt != null)
            mInfo.setText(mInfoTxt);

        Button mLeft = (Button) view.findViewById(R.id.DF_TP_done);
        Button mRight = (Button) view.findViewById(R.id.DF_TP_cancel);

        if (mLeftListener != null)
            mLeft.setOnClickListener(mLeftListener);
        if (mRightListener != null)
            mRight.setOnClickListener(mRightListener);

        return view;
    }

    public void setInfoTxt(String info) {
        mInfoTxt = info;
    }

    public void setOnDoneListener(View.OnClickListener listener) {
        mLeftListener = listener;
    }

    public void setOnCancelListener(View.OnClickListener listener) {
        mRightListener = listener;
    }

    public void setTime(String time, int startIs0EndIs1) {
        if (time.equals(Consts.Strings.MGR_ARRGMENT_DEFAULT_TIME))
            return;
        String[] parts = time.split("~")[startIs0EndIs1].split(":");
        hour = Integer.parseInt(parts[0]);
        min = Integer.parseInt(parts[1]);
    }

    public String getTime() {
        if (mPicker != null) {
            int hour = mPicker.getCurrentHour();
            int min = mPicker.getCurrentMinute();
            StringBuilder builder = new StringBuilder();
            if (hour < 10)
                builder.append("0");
            builder.append(hour).append(":");
            if (min < 10)
                builder.append("0");
            builder.append(min);
            return (mPreviousTime.isEmpty() ? "" : mPreviousTime + "~") + builder.toString();
        }
        return null;
    }

    public void setPreviousTime(String prev) {
        mPreviousTime = prev;
    }

    public String getPreviousTime() {
        return mPreviousTime;
    }
}
