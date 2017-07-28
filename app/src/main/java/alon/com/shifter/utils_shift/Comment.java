package alon.com.shifter.utils_shift;

import java.io.Serializable;

/**
 * Created by Alon on 11/22/2016.
 */

public class Comment implements Serializable {

    private String[] mComments = new String[4];

    public String[] getComments() {
        return mComments;
    }

    public void setComment(int index, String comment) {
        mComments[index] = comment;
    }

    public String getComment(int index) {
        return mComments[index];
    }
}
