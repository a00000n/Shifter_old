package alon.com.shifter.utils_shift;

import java.io.Serializable;
import java.util.Arrays;

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

    public boolean getForBool(int time) {
        return enabled[time];
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

    @Override
    public boolean equals(Object other){
        return other instanceof ShiftInfo && Arrays.equals(((ShiftInfo) other).enabled, enabled);
    }
}
