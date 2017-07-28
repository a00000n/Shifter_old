package alon.com.shifter.wrappers;

/**
 * Created by Alon on 1/8/2017.
 */

public interface WrapperBase {

    String SHIFT_ID = "BrsQtqO7mDWWcWkqTLYZ";
    String DIALOG_FRAGMENT_ID = "J1LFW8F8O0ZyC08VjGQB";
    String FINISHABLE_TASK_ID = "NeJNAlne4FMpYeVjMceV";
    String SPEC_SETTINGS_ID = "gZ72XdtAXPEfDGukGztq";
    String STRING_SPEC_SETTINGS_ID = "C4tyz4hxjtrIRRNlGyHN";
    String STRING_SHIFT_ID = "ndO9hffu36fGxjVjj6R8";
    String CHECK_BOX_ID = "APH8ZLX1VMfceRRcRjyt";
    String LAYOUT_ID = "sj2wutREi0CxZ8oXmYp7";
    String FINISHABLE_TASK_WTIH_PARAMS_ID = "Ljm5lmAsr6zc2v5Jj13O";

    WrapperBase setWrapperParam(String key, Object value);

    Object getWrapperParam(String key);
}
