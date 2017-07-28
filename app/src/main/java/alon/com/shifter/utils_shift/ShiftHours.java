package alon.com.shifter.utils_shift;

import java.io.Serializable;

/**
 * Created by Alon on 11/12/2016.
 */

public class ShiftHours implements Serializable {

    private String[] mHours = new String[4];

    public void setHour(int index, String hour) {
        if (index < 0 || index > 3)
            throw new IndexOutOfBoundsException();
        mHours[index] = hour;
    }

    public String getHour(int index) {
        return mHours[index];
    }

}
