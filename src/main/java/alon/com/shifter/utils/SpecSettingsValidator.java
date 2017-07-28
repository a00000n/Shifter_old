package alon.com.shifter.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import alon.com.shifter.utils_shift.SpecSettings;

/**
 * Created by Alon on 12/28/2016.
 */

public class SpecSettingsValidator {


    private static final int[] SS_sizes = {7, 4, 6, 2};
    private static HashMap<Integer, Boolean[]> limitMap;
    private static boolean[] mShiftAllowed = new boolean[28];

    public static boolean[] isValid(SpecSettings specSettings, String specSettingsString, String shifts, Context con) {
        //Instantiation
        boolean[] result = new boolean[]{true, true, true};
        limitMap = new HashMap<>();

        generateLimits(specSettings, specSettingsString);
        generateAllowedShifts(shifts, con);
        if (limitMap.containsKey(0)) { // Must submit day
            Boolean[] flags = limitMap.get(0);
            for (int i = 0; i < SS_sizes[0]; i++) {
                boolean isDayNeeded = flags[i];
                boolean isDayActive = getIsDayActive(i);
                if (isDayNeeded && !isDayActive) {
                    result[0] = false;
                    break;
                }
            }
        }
        if (limitMap.containsKey(1)) { // certain shift once per week.
            Boolean[] flags = limitMap.get(1);
            for (int i = 0; i < SS_sizes[1]; i++) {
                boolean isShiftNeeded = flags[i];
                boolean isShiftActive = getIsShiftActive(i);
                if (isShiftNeeded && !isShiftActive) {
                    result[1] = false;
                    break;
                }
            }
        }
        if (limitMap.containsKey(2)) { // Min shifts.
            Boolean[] flags = limitMap.get(2);
            for (int i = 0; i < SS_sizes[2]; i++) {
                if (flags[i]) {
                    int allowedShifts = getAllowedShifts();
                    if (allowedShifts <= (i + 1)) {
                        result[2] = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static void generateLimits(SpecSettings specSettings, String specSettingsString) {
        String[] specSettingsStrParts = specSettingsString.split(";");
        String[] headers = specSettingsStrParts[0].split("~");
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (specSettings.containsHeader(header)) {
                ArrayList<String> children = specSettings.getChildrenArrayList(headers[i]);
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

    private static void generateAllowedShifts(String JSON, Context con) {
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

    private static boolean getIsDayActive(int day) {
        for (int i = day * 4; i < day * 4 + 4; i++)
            if (mShiftAllowed[i])
                return true;
        return false;
    }

    private static boolean getIsShiftActive(int shift) {
        for (int i = shift; i < 28; i += 4)
            if (mShiftAllowed[i])
                return true;
        return false;
    }

    private static int getAllowedShifts() {
        int count = 0;
        for (boolean flag : mShiftAllowed)
            if (flag)
                count++;
        return count;
    }
}
