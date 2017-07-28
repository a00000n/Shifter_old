package alon.com.shifter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import alon.com.shifter.R;

public class CustomSettingView extends RelativeLayout implements View.OnTouchListener {

    private TextView mDetails;

    private OnClickListener mOnClick;

    public CustomSettingView(Context context) {
        super(context);
        init(context);
    }

    public CustomSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomSettingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context mCon) {
        inflate(mCon, R.layout.view_custom_setting, this);

        mDetails = (TextView) findViewById(R.id.V_CS_setting);

        mDetails.setOnTouchListener(this);


    }

    @Override
    public void setOnClickListener(OnClickListener onClick) {
        mOnClick = onClick;
    }

    public void setDetails(String key, String[] value) {
        String mInfo = key;
        if (value != null && value.length > 0) {
            mInfo += " : ";
            if (value.length == 1) {
                mInfo += value[0] + "\n";
                setDetails(mInfo);
            } else {
                for (String val : value)
                    mInfo += "\n\t\t" + val;
            }
        }
        setDetails(mInfo);
    }

    public String getDetails() {
        return mDetails.getText().toString();
    }

    public void setDetails(CharSequence str) {
        mDetails.setText(str);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        String TAG = "CustomSettingView";
        final int DRAWABLE_RIGHT = 2;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= (v.getRight() - mDetails.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                if (mOnClick != null)
                    mOnClick.onClick(this);
                else
                    Log.e(TAG, "onClick: mOnClick is null.");
            }
        }
        return true;
    }

}
