package alon.com.shifter.utils_arrangement;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Alon on 2/9/2017.
 */

public class Arrangement implements Serializable {

    public static final int SUN = 1;
    public static final int MON = 2;
    public static final int TUE = 3;
    public static final int WED = 4;
    public static final int THU = 5;
    public static final int FRI = 6;
    public static final int SAT = 7;

    public static final int MORNING = 10;
    public static final int AFTERNOON = 11;
    public static final int EVENING = 12;
    public static final int NIGHT = 13;

    private ArrangementDay[] days = new ArrangementDay[7];

    public Arrangement() {
        for (int i = 0; i < days.length; i++) {
            days[i] = new ArrangementDay(i);
        }
    }

    public ArrangementDay getDay(int day) {
        if (day < 0 || day > 6)
            throw new IndexOutOfBoundsException();
        return days[day];
    }

    public String getAllShiftsForMe(String uid) {
        StringBuilder builder = new StringBuilder();
        for (ArrangementDay day : days) {
            for (int i = 0; i < 4; i++) {
                ArrangementShift shift = day.getShift(i);
                HashMap<String, String> map = shift.getAllEmployees();
                builder.append(map.get(uid)).append(i != 3 ? "" : ";");
            }
            builder.append("~");
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        String display = "";
        display += "Sunday: \n" + days[0].toString() + "\n" +
                "Monday: \n" + days[1].toString() + "\n" +
                "Tuesday: \n" + days[2].toString() + "\n" +
                "Wednesday: \n" + days[3].toString() + "\n" +
                "Thursday: \n" + days[4].toString() + "\n" +
                "Friday: \n" + days[5].toString() + "\n" +
                "Saturday: \n" + days[6].toString() + "\n";
        return display;
    }
}
