package alon.com.shifter.utils_shift;

import java.io.Serializable;

/**
 * Created by Alon on 9/30/2016.
 */

public class Shift implements Serializable {

    private ShiftInfo[] mShifts = {new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo(), new ShiftInfo()};

    private Comment[] mComments = {new Comment(), new Comment(), new Comment(), new Comment(), new Comment(), new Comment(), new Comment()};

    public ShiftInfo[] getInfoComplete() {
        return mShifts;
    }

    public void setShifts(boolean[] selected) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 7; j++)
                mShifts[j].set(i, selected[i + j * 4]);
    }

    public Comment[] getComments() {
        return mComments;
    }

    public void setComments(Comment[] comments) {
        mComments = comments;
    }
}
