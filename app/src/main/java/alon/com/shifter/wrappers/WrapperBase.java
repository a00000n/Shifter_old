package alon.com.shifter.wrappers;

/**
 * Created by Alon on 1/8/2017.
 */

public interface WrapperBase {

    //Objects ==================================
    String WRAPPER_ACTIVITY_ID = "JYv6bLBhFBQaF6b6cSpG";
    String WRAPPER_HASHMAP_ID = "bjWpygSq4r8Zefqebalk";
    String WRAPPER_ARRAY_LIST_ID = "7z4AtJQ3jAavt7hsF2HJ";
    String WRAPPER_UTIL_ID = "t9Jjwp79w3AmYTx1hDcp";
    String WRAPPER_CONTEXT_ID = "9wDnNfbHvrBd6Wa8NjpU";
    String WRAPPER_VIEW_CONTAINER_ID = "ZZOOAUjovq2MQNGYHq1T";
    String WRAPPER_FINISHABLE_TASK_ID = "NeJNAlne4FMpYeVjMceV";
    String WRAPPER_SHIFT_ID = "BrsQtqO7mDWWcWkqTLYZ";
    String WRAPPER_ARRANGEMENT_ID = "73LzrhbLCXAJiaTOmzev";
    String WRAPPER_FINISHABLE_TASK_WITH_PARAMS_ID = "Ljm5lmAsr6zc2v5Jj13O";
    String WRAPPER_SPEC_SETTINGS_ID = "gZ72XdtAXPEfDGukGztq";
    String WRAPPER_SHIFT_HOURS_ID = "1rqtpKqIQ4EewmmxkJzH";
    //Layouts ==================================
    String WRAPPER_DIALOG_FRAGMENT_PHONENUMBER_ID = "TBKRNhDdDHE0obrVdrK5";
    String WRAPPER_DIALOG_FRAGMENT_ID = "J1LFW8F8O0ZyC08VjGQB";
    String WRAPPER_PROGRESS_BAR_ID = "giW9q8MA8DNIoztRmuRk";
    String WRAPPER_SCROLL_VIEW_ID = "Dda62ekEfHjZmkG41Q79";

    //Views ====================================
    String WRAPPER_TEXTVIEW_ID = "c49dKl7vmodOnyqUMPXu";
    //Lists ====================================
    String WRAPPER_WRAPPER_OBJECT_MULTIPLE_ID = "jJe6Ois32B0eGQTBNUoU";
    String WRAPPER_DIALOG_FRAGMENT_MULTIPLE_ID = "jCExr92OPJpuMI11TilL";
    //Primitives ===============================
    String WRAPPER_CONVERTED_VIEW_POSITION_ID = "dhb5nV19jewVRqrUgy4o";
    String WRAPPER_STRING_SPEC_SETTINGS_ID = "C4tyz4hxjtrIRRNlGyHN";
    String WRAPPER_STRING_SHIFT_ID = "ndO9hffu36fGxjVjj6R8";
    String WRAPPER_STRING_SHIFT_SUBMISSION_CAP = "X7RVHT7e1Jcm7M1Jb8HO";
    String WRAPPER_STRING_IP_SITE = "Rp70vh3GWxNAcfyGX0vB";
    //Firebase ==================================
    String WRAPPER_FB_BOOLEAN_HAS_SCHEDULE_SET = "yjoAVf2UQN24JHoXsgea";
    String WRAPPER_FB_BOOLEAN_HAS_SUBMISSION_CAP = "uM6vOZicwUhbMKfrl14s";
    String WRAPPER_FB_BOOLEAN_HAS_SPEC_SETTINGS_INFO = "FqlQ9IY1AIsUjwuDtyXL";
    String WRAPPER_FB_STRING_SHIFT_INFO = "bApJDVqtFSeXXYNiUyyv";
    String WRAPPER_FB_STRING_SPEC_SETTINGS = "huzPjURAazMVaKgmkReq";
    String WRAPPER_FB_BOOLEAN_HAS_USER_LIST = "7icTzsfrl7m5QUSTfTTz";
    String WRAPPER_FB_SET_USER_LIST = "S3ZPy8OlqWG7Cri7U2N8";

    WrapperBase setWrapperParam(String key, Object value);

    Object getWrapperParam(String key);
}
