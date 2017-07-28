package alon.com.shifter.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import alon.com.shifter.utils_shift.SpecSettings;

/**
 * Created by Alon on 12/18/2016.
 */

public class SpecSettingsInforcer {

//    private final int SS_mustSubmitDay = 0;
//    private final int SS_mustSubmitCertainShiftOncePerWeek = 1;
//    private final int SS_mustSubmitMinOfShifts = 2;
//    private final int SS_extraSettings = 3;

    private static SpecSettingsInforcer instance;
    private final int[] SS_sizes = {7, 4, 6, 2};
    private HashMap<Integer, Boolean[]> limitMap;
    private SpecSettings mSettings;

    private String mSpecSettingsString;

    private boolean[] mShiftAllowed;

    private boolean[] mShiftSelection;


    private SpecSettingsInforcer(SpecSettings settings, String specSettingsString, String shiftJSON, Context context) {
        if (settings == null || settings.equals(SpecSettings.getEmpty()))
            throw new IllegalArgumentException("SpecSettings passed to constructor can't be null or empty."); // TODO add NullCheck as static function in Util class.
        mSettings = settings;
        mSpecSettingsString = specSettingsString;

        mShiftSelection = new boolean[28];
        mShiftAllowed = new boolean[28];

        limitMap = new HashMap<>();

        if (mSpecSettingsString == null || mSpecSettingsString.isEmpty())
            throw new IllegalArgumentException("String passed to constructor can't be null or empty.");


        generateAllowedShifts(shiftJSON, context);
        generateLimits();
    }

    public static SpecSettingsInforcer getInstance(SpecSettings settings, String specSettingsString, String json, Context context) {
        return instance == null ? new SpecSettingsInforcer(settings, specSettingsString, json, context) : instance;
    }

    private void generateLimits() {
        String[] specSettingsStrParts = mSpecSettingsString.split(";");
        String[] headers = specSettingsStrParts[0].split("~");
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (mSettings.containsHeader(header)) {
                ArrayList<String> children = mSettings.getChildrenArrayList(headers[i]);
                assert children != null; // PROBLEM might be a problem later-on.

                String specSettingsChild = specSettingsStrParts[i + 1];
                String[] childParts = specSettingsChild.split("~");

                Boolean[] selected = new Boolean[SS_sizes[i]];

                for (int j = 0; j < selected.length; j++)
                    selected[j] = false;

                Log.i("SpecSettingsInforcer", "generateLimits: " + childParts[0].equals(Integer.toString(i + 1)));

                String[] newChildParts = new String[childParts.length - 1];

                System.arraycopy(childParts, 1, newChildParts, 0, childParts.length - 1);

                for (int j = 0; j < newChildParts.length; j++)
                    selected[j] = children.contains(newChildParts[j]);
                limitMap.put(i, selected);
            }
        }
    }

    private void generateAllowedShifts(String JSON, Context con) {
        Util mUtil = Util.getInstance(con);
        try {
            JSONObject mJson = new JSONObject(JSON);
            for (int i = 0; i < 7; i++) {
                String dayName = mUtil.getDayString(con, i + 1);
                try {
                    JSONObject mObj = (JSONObject) mJson.get(dayName);
                    for (int j = 0; j < 4; j++) {
                        String dayShiftName = mUtil.getShiftTitle(con, j);
                        boolean canSubmit = !(mObj.get(dayShiftName).equals("(-1)"));
                        mShiftAllowed[i * 4 + j] = canSubmit;
                    }
                } catch (ClassCastException ex) {
                    for (int j = 0; j < 4; j++)
                        mShiftAllowed[i * 4 + j] = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onUpdatedChange(int day, int shift) {
        int pos = (day * 4) + shift;
        mShiftSelection[pos] = !mShiftSelection[pos];
    }

    public boolean[] checkLimits() {
        boolean[] metDemands = {true, true, true, true, false};
        for (int i = 0; i < 4; i++) {
            Boolean[] demands = limitMap.get(i);
            if (demands != null)
                if (i == 0) {
                    for (int j = 0; j < demands.length; j++)
                        if (demands[j])
                            if (!isDaySubmitted(j)) {
                                metDemands[i] = false;
                                break;
                            }
                } else if (i == 1) {
                    for (int j = 0; j < demands.length; j++)
                        if (demands[j])
                            if (!isShitSubmited(j)) {
                                metDemands[i] = false;
                                break;
                            }
                } else if (i == 2) {
                    int min = -1;
                    for (int j = 0; j < demands.length; j++)
                        if (demands[j]) {
                            min = j;
                            break;
                        }
                    if (getAmountOfSelectedShifts() < min)
                        metDemands[i] = false;
                } else if (i == 3) {
                    boolean reminder = demands[0];
                    boolean forbidden = demands[1];
                    if (reminder) {
                        //Complex. TODO: finish.
                    }
                    if (forbidden)
                        for (int j = 0; j < 7; j++)
                            if (j != 6) {
                                int k = getLastAllowedShiftForDay(j);
                                int l = getFirstAllowedShiftForDay(j + 1);
                                if (mShiftSelection[k] && mShiftSelection[l] && Math.abs(k - l) <= 3) {
                                    metDemands[i] = false;
                                    break;
                                }
                            }

//                    for (int j =,k = 4;
//                    j<mShiftSelection.length ;
//                    j += 4, k += 4)
//                    if (mShiftSelection[j] && mShiftSelection[k]) {
//                        metDemands[i] = false;
//                        break;
//                    }
                }
        }
        return metDemands;
    }

    private int getLastAllowedShiftForDay(int day) {
        for (int i = day * 4 + 3; i >= day * 4; i--)
            if (mShiftAllowed[i])
                return i;
        return -1;
    }

    private int getFirstAllowedShiftForDay(int day) {
        for (int i = day * 4; i < day * 4 + 4; i++)
            if (mShiftAllowed[i])
                return i;
        return -1;
    }

    private boolean isDaySubmitted(int day) {
        for (int k = day * 4; k < (day + 1) * 4; k++) {
            boolean selectedShift = mShiftSelection[k];
            if (selectedShift)
                return true;
        }
        return false;
    }

    private boolean isShitSubmited(int shift) {
        for (int k = shift; k < mShiftSelection.length; k += 4) {
            boolean selectedShift = mShiftSelection[k];
            if (selectedShift)
                return true;
        }
        return false;
    }

    private int getAmountOfSelectedShifts() {
        int shiftCounter = 0;
        for (boolean shiftSelected : mShiftSelection)
            if (shiftSelected)
                shiftCounter++;
        return shiftCounter;
    }

}
