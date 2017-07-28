package alon.com.shifter.utils_arrangement;

import java.io.Serializable;

import static alon.com.shifter.utils_arrangement.Arrangement.MORNING;

/**
 * Created by Alon on 2/9/2017.
 */

public class ArrangementDay  implements Serializable {

    private int day = -1;

    private ArrangementShift[] shifts = new ArrangementShift[4];

    public ArrangementDay(int dayIndex) {
        day = dayIndex;
        for (int i = 0; i < shifts.length; i++)
            shifts[i] = new ArrangementShift(i + MORNING);

    }

    public int getDay() {
        return day;
    }

    public ArrangementShift getShift(int shiftIndex) {
        if (shiftIndex < 0 || shiftIndex > 3)
            throw new IndexOutOfBoundsException();
        return shifts[shiftIndex];
    }

    public void setShift(int shiftIndex, ArrangementShift shift) {
        if (shiftIndex < 0 || shiftIndex > 3)
            throw new IndexOutOfBoundsException();
        if (shift == null)
            throw new NullPointerException();
        shifts[shiftIndex] = shift;
    }

    @Override
    public String toString() {
        String display = "";
        display += "\tMorning: \n" + shifts[0].toString() + "\n" +
                "\tAfter Noon: \n" + shifts[1].toString() + "\n" +
                "\tEvening: \n" + shifts[2].toString() + "\n" +
                "\tNight: \n" + shifts[3].toString() + "\n";
        return display;
    }
}
