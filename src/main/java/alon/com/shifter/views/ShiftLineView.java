package alon.com.shifter.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.security.InvalidParameterException;

import alon.com.shifter.R;

public class ShiftLineView extends LinearLayout {

    private final int[] BTN_IDS = {R.id.V_SL_sun_tgl, R.id.V_SL_mon_tgl, R.id.V_SL_tue_tgl, R.id.V_SL_wed_tgl, R.id.V_SL_thu_tgl, R.id.V_SL_fri_tgl, R.id.V_SL_sat_tgl};
    private String[] mShiftComments = new String[7];
    private ToggleButton[] mShifts;

    public ShiftLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ShiftLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context mCon, AttributeSet attrs) {
        TypedArray arr = mCon.obtainStyledAttributes(attrs, R.styleable.ShiftLineView, 0, 0);
        try {
            int type = arr.getInt(R.styleable.ShiftLineView_shiftType, -1);
            if (type == -1)
                throw new InvalidParameterException("no custom:shiftType setGate.");

            if (type == 1)
                inflate(mCon, R.layout.view_shift_line_titles, this);
            else
                inflate(mCon, R.layout.view_shift_line, this);
            mShifts = new ToggleButton[7];
            ((TextView) findViewById(R.id.V_SL_title_TV))
                    .setText(mCon.getString((type == 1 ? R.string.shift_title_morn : (type == 2 ? R.string.shift_title_afr_non :
                            (type == 3 ? R.string.shift_title_evening : R.string.shift_title_night)))));
            for (int i = 0; i < BTN_IDS.length; i++) {
                mShifts[i] = (ToggleButton) findViewById(BTN_IDS[i]);
                mShifts[i].setTag(i + "-" + hashCode());
            }
        } finally {
            arr.recycle();
        }
    }

    public boolean[] getShifts() {
        boolean[] result = {false, false, false, false, false, false, false};
        for (int i = 0; i < mShifts.length; i++)
            result[i] = mShifts[i].isChecked();
        return result;
    }

    public String getShiftCommnent(int shift) {
        return mShiftComments[shift];
    }

    public void setShiftComment(int shift, String comment) {
        mShiftComments[shift] = comment;
    }

    public void setEnabled(int index, boolean flag) {
        mShifts[index].setEnabled(flag);
    }

    public void setToggleState(int index, boolean flag) {
        mShifts[index].setChecked(flag);
    }

    public void setLongClickListener(int i, OnLongClickListener listener) {
        mShifts[i].setOnLongClickListener(listener);
    }

    public void setOnCheckedChangeListener(int i, CompoundButton.OnCheckedChangeListener listener) {
        mShifts[i].setOnCheckedChangeListener(listener);
    }
}
