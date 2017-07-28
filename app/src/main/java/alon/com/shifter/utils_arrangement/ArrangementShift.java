package alon.com.shifter.utils_arrangement;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static alon.com.shifter.utils_arrangement.Arrangement.MORNING;
import static alon.com.shifter.utils_arrangement.Arrangement.NIGHT;

/**
 * Created by Alon on 2/9/2017.
 */
public class ArrangementShift implements Serializable {

    public static final String TAG = "ArrangementShift";
    private int shift = -1;

    private HashMap<String, String> employeesHourMap = new HashMap<>();

    public ArrangementShift(int shiftIndex) {
        if (shiftIndex < MORNING || shiftIndex > NIGHT)
            throw new IndexOutOfBoundsException();
        shift = shiftIndex;
    }

    public ArrangementShift insertNewEmployeeToShift(String uid, String hours) {
        if (employeesHourMap.get(uid) != null)
            Log.i(TAG, "insertNewEmployeeToShift: user " + uid + " already has a shift, setting from: " + employeesHourMap.get(uid) + " to: " + hours);
        employeesHourMap.put(uid, hours);
        return this;
    }

    public ArrangementShift removeEmployeeFromShift(String uid) {
        if (employeesHourMap.get(uid) == null) {
            Log.i(TAG, "removeEmployeeFromShift: user " + uid + " isn't registered for this shift.");
            return this;
        }
        employeesHourMap.remove(uid);
        return this;
    }

    @Override
    public String toString() {
        String display = "";
        for (Map.Entry<String, String> entry : employeesHourMap.entrySet()) {
            display += "\t\t " + entry.getKey() + " -> " + entry.getValue() + "\n";
        }
        return display;
    }

    public boolean isAssigned(String uid) {
        return employeesHourMap.get(uid) != null && !employeesHourMap.get(uid).isEmpty();
    }

    public HashMap<String, String> getAllEmployees() {
        return employeesHourMap;
    }
}
