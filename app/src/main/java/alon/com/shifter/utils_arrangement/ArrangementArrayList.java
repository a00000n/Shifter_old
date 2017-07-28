package alon.com.shifter.utils_arrangement;

import java.util.ArrayList;

/**
 * Created by Alon on 2/9/2017.
 */

public class ArrangementArrayList extends ArrayList<ArrangementUser> {

    private Arrangement arrangement;

    public ArrangementArrayList(Arrangement arrang) {
        super();
        arrangement = arrang;
    }

    public void setTime(int pos, String time, int currentDay) {
        ArrangementUser user = get(pos);
        user.setTime(time);

        int currentShift = determineShift(user);
        currentDay--;

        if (currentShift == -1)
            throw new RuntimeException();
        arrangement.getDay(currentDay).getShift(currentShift).insertNewEmployeeToShift(user.getUser().getUID(), time);
    }

    public void setListed(int pos, boolean listed, int currentDay) {
        ArrangementUser user = get(pos);
        user.setListed(listed);

        int currentShift = determineShift(user);
        currentDay--;

        if (currentShift == -1)
            throw new RuntimeException();
        if (listed)
            arrangement.getDay(currentDay).getShift(currentShift).insertNewEmployeeToShift(user.getUser().getUID(), user.getTime());
        else
            arrangement.getDay(currentDay).getShift(currentShift).removeEmployeeFromShift(user.getUser().getUID());
    }

    private int determineShift(ArrangementUser checkedUser) {
        int counter = -1; //start from -1 since first entry is alway title row.
        for (ArrangementUser user : this) {
            if (user.getIsTitleRow())
                counter++;
            else if (user.equals(checkedUser))
                return counter;
        }
        return -1;
    }

    public void cancelAllListedStatusWithoutUpdate() {
        for (ArrangementUser user : this)
            user.setListed(false);

    }

    public boolean isListed(int pos, int currentDay) {
        ArrangementUser user = get(pos);

        int currentShift = determineShift(user);
        currentDay--;

        return arrangement.getDay(currentDay).getShift(currentShift).isAssigned(user.getUser().getUID());
    }

    public void setArrangement(Arrangement arrg) {
        arrangement = arrg;
    }
}
