package alon.com.shifter.utils_shift;

import java.io.Serializable;

public class ShiftInfo implements Serializable {

    private boolean[] enabled = {false, false, false, false};

    public void set(int time, boolean info) {
        enabled[time] = info;
    }

    public String getFor(int time) {
        if (!enabled[time])
            return "(-1)";
        return "(1)";
    }

    @Override
    public String toString() {
        String str = "(";
        for (Boolean bool : enabled)
            str += bool + ",";
        str = str.substring(0, str.lastIndexOf(","));
        str += ")";
        return str;
    }

    public boolean getForBool(int time) {
        return enabled[time];
    }
}
