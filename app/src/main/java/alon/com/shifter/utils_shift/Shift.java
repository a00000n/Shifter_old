package alon.com.shifter.utils_shift;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

import alon.com.shifter.utils.Util;

/**
 * Created by Alon on 9/30/2016.
 */

public class Shift implements Serializable {

    private ShiftInfo[] mShifts = {new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo()};

    private Comment[] mComments = {new Comment(), new Comment(), new Comment(), new Comment(), new Comment(), new Comment(), new Comment()};

    public ShiftInfo[] getInfoComplete() {
        return mShifts;
    }

    public void setShifts(ShiftInfo[] info) {
        mShifts = info;
    }

    public void setShifts(boolean[] selected) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 7; j++)
                mShifts[j].set(i, selected[i + j * 4]);
    }

    public void setShiftEnabled(int day, int shift, boolean enabled) {
        mShifts[day].set(shift, enabled);
    }

    public Comment[] getComments() {
        return mComments;
    }

    public void setComments(Comment[] comments) {
        mComments = comments;
    }

    public static Shift constructFromJSON(Context context, String shiftJSON) {
        try {
            Shift shift = new Shift();
            Util util = Util.getInstance(context);
            JSONObject json = new JSONObject(shiftJSON);
            for (int i = 1; i <= 7; i++) {
                JSONObject shiftsEnabled = json.getJSONObject(util.getDayString(context, i));
                for (int j = 0; j < 4; j++)
                    shift.setShiftEnabled(i - 1, j, shiftsEnabled.getString(util.getShiftTitle(context, j)).equals("(1)"));
            }
            return shift;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Shift && Arrays.equals(((Shift) other).mShifts, mShifts);
    }
}
