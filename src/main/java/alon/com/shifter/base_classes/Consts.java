package alon.com.shifter.base_classes;

import android.app.FragmentManager;

/**
 * The Consts interface, short for Constants, is the base interface that almost all other classes implement, or use in one way or another.
 * This interface holds several classes, similar to the {@link alon.com.shifter.R} class.
 * The class contains the following sub classes:<br>
 * <ul>
 * <li>{@link Fb_Dirs} - A constants set of all possible Firebase Realtime Database directories.</li>
 * <li>{@link Fb_Keys} - A constants set of all possible Firebase Realtime Database keys, which are located in side directories.</li>
 * <li>{@link Pref_Keys} - A constants set of all preferences keys used in {@link android.content.SharedPreferences}.</li>
 * <li>{@link Param_Keys} - A constants set of all keys that can be used in {@link FinishableTaskWithParams}.</li>
 * <li>{@link Strings} - A constants set of strings without a specific class for them,
 * this contains, values for account deletion, certain behaviour flags, and file names.</li>
 * <li>{@link Ints} - A constants set of integers, without a specific class for them, such as job type ints, and progress flags.</li>
 * <li>{@link Fc_Keys} - A constants set of all gate names used in {@link alon.com.shifter.utils.FlowController}.</li>
 * <li>{@link Linker_Keys} - A constants set of all values that can be used in the {@link Linker} class, including parameter names, and linker type flags.</li>
 * <li>{@link DialogFragment_Keys} - A constants set of all values that are used when calling {@link android.app.DialogFragment#show(FragmentManager, String)} as the tag parameter.</li>
 * </ul>
 *
 * @since V.1.0
 */
@SuppressWarnings("ALL")
public interface Consts {
    /**
     * INSTRUCTIONS:
     * {NAME OF DIRECTORY}_{NAME OF KEY}
     */
    final class Fb_Keys {
        public static final String WORKPLACE_SH_CODE = "short-hand-code";

        public static final String MGR_SEC_SHIFT_SCHEDULE = "shift-schedule";
        public static final String MGR_SEC_SPEC_SETTINGS_EXPANDABLE = "spec-settings-expandable";
        public static final String MGR_SEC_SPEC_SETTINGS_RESTRICTIONS = "spec-settings-restrictions";
        public static final String MGR_SEC_USERS_ACCEPTED = "accepted-users";
        public static final String MGR_SEC_SCHEDULE_SET = "schedule-set";
        public static final String MGR_SEC_SPEC_SETTINGS_SET = "special-settings-object";

        public static final String GENERAL_INFO_JOB_TYPES = "job-types";

        public static final String SHIFT_SETUP_SEC_SPECIAL_SETTINGS_TYPES = "special-shift-settings";
        public static final String USER_BASE_USER_OBJECT = "base-user-seralizeable";

        public static final String USER_SHIFT_INFO = "";
    }

    /**
     * INSTRUCTIONS:
     * {NAME OF DIRECTORY}
     */
    final class Fb_Dirs {
        public static final String WORKPLACES = "workplaces";
        public static final String USERS = "users";
        public static final String MGR_SEC = "mgr-section";
        public static final String MGR_SEC_USER_REQUESTS = "mgr-section-user-requests";
        public static final String GENERAL_INFO = "general-info";
        public static final String SHIFT_SUBMISSIONS = "shift-submissions";
    }

    /**
     * INSTRUCTIONS:
     * {NAME OF PREF GENERAL PURPOSE}_{TYPE OF GENERAL PREFERENCES}_{NAME OF KEY}
     */
    final class Pref_Keys {
        public static final String USR_NUMBER = ">>phone_number<<";
        public static final String USR_IP = ">>ip_addr<<";
        public static final String USR_SHIFT_SCHEDULE = ">>shift_schedule<<";

        public static final String LOG_PERMA_LOGIN = ">>perma_login<<";
        public static final String LOG_PERMA_LOGIN_PASS = ">>perma_login_pass<<";
        public static final String LOG_PERMA_LOGIN_EMAIL = ">>perma_login_email<<";

        public static final String MGR_SEC_COMPLETE_SPEC_SETTINGS = ">>complete_shift_info<<";
        public static final String MGR_SEC_SPEC_SETTINGS_RESTRICTIONS = ">>spec_settings_restrictions<<";
        public static final String MGR_SEC_SPEC_SETTINGS_EXPANDABLE = ">>spec_Settings_expandable<<";
        public static final String USR_SPEC_SETTINGS = ">>spec_settings<<";
        public static final String USR_SPEC_SETTINGS_OBJECT = ">>spec_settings_obj<<";
        public static final String MGR_SEC_SCHEDULE_SET = ">>constructed_schedule<<";
        public static final String MGR_SEC_USER_RQS = ">>user_requests<<";
        public static final String MGR_SEC_USER_LIST = ">>user_list<<";

        public static final String GENERAL_INFO_JOB_TYPES = ">>job_types<<";
        public static final String LOGOUT_TIME_LONG = ">>logout_timestamp_milli<<";
    }

    /**
     * INSTRUCTIONS:
     * [TYPE || KEY} - Must be either Integer(int) or String.
     * Name instruction:
     * {TYPE || KEY}_{NAME OF GENERAL ACTION}_{NAME OF KEY}.
     */
    final class Linker_Keys {
        public static final int TYPE_UPLOAD_SHIFT_SCHEDULE = 0x0a6018;
        public static final int TYPE_REGISTER = 0x100c35;
        public static final int TYPE_LOGIN = 0x06e816;
        public static final int TYPE_INFO_FETCHER = 0x0077139;
        public static final int TYPE_GET_PHONE_NUMBER = 0x111938;
        public static final int TYPE_UPLOAD_SPEC_SETTINGS = 0x183627;
        public static final int TYPE_DELETE_ACCOUNT = 0x0de989;
        public static final int TYPE_UPDATE_USER_ACCOUNTS = 0x07fea4;
        public static final int TYPE_UPLOAD_SELECTED_SHIFTS_USER = 0x09b63c;

        public static final String KEY_LOGIN_PERSONAL_NAME = "name";
        public static final String KEY_LOGIN_EMAIL = "email";
        public static final String KEY_LOGIN_PASS = "password";
        public static final String KEY_LOGIN_WORKPLACE_CODE = "workpalce_code";
        public static final String KEY_LOGIN_PHONE = "phone_#";
        public static final String KEY_LOGIN_DIALOG = "progress_dialog";
        public static final String KEY_LOGIN_REMEMBER_ME = "remember_me";

        public static final String KEY_SHIFT_UPLOAD_SHIFT_OBJECT = "shift_schedule_object";
        public static final String KEY_SHIFT_UPLOAD_DIALOG = "shift_upload_dialog";

        public static final String KEY_IP_FETCH_ADDR = "get_ip";

        public static final String KEY_NUMBER_FETCHER_DIALOG = "number_fetch_dialog";
        public static final String KEY_NUMBER_FETCHER_TO = "number_fetch_to";
        public static final String KEY_NUMBER_FETCHER_DIALOG_VER = "number_fetch_ver_dialog";

        public static final String KEY_SPEC_SETTINGS = "spec_settings_object";

        public static final String KEY_DELETE_USER_BASEUSER_OBJECT = "user_delete_baseuser_object";

        public static final String KEY_USER_UPDATE_LIST = "user_update_list";
    }

    /**
     * INSTRUCTIONS:
     * {NAME OF PARAM KEY}.
     */
    final class Param_Keys {
        public static final String KEY_ACCEPTED_USERS_STRING = "63dHskAtfQhuCblqU3Gi";
        public static final String KEY_APPROVED_STATE = "a3erXOm0ER2dNlHh2IKS";
        public static final String KEY_BASE_USER_OBJECT_LIST = "s8Qwa81RI9sD0fgTYysh";
        public static final String KEY_BASE_USER_STRING_LIST = "jmmz3gnpCdPGHf7Op7LM";
        public static final String KEY_BASE_USER_OBJECT = "LEl32wRjgSdnwxelMNjh";
        public static final String KEY_DELETED_ACCOUNT = "l4XD1PxJ6zlNDQvOeLjQ";
        public static final String KEY_SHIFT_COMMENT = "poFKlWIDe3KADxnzqU3Q";
        public static final String KEY_FINISHABLE_TASK = "k2Z6K1wIkAyMiRO0AyUv";
    }

    /**
     * INSTRUCTIONS:
     * {NAME || FILE || FB_DIR}_{nothing || NAME OF KEY || NAME OF KEY}
     */
    final class Strings {
        public static final String NULL = ">>NULL<<";

        public static final String MGR_SEC_SPEC_SETTING_REST_LIMIT_AMOUNT = "jhz83S75CgVmSg7";
        public static final String MGR_SEC_SPEC_SETTING_REST_RADIO_BTN_BEHAVIOUR = "9CR25bdDu1w3wkd";

        public static final String FILE_SHIFT_OBJECT = "shift_object.obj";
        public static final String FILE_SPEC_SETTINGS_OBJECT = "spec_settings_object.obj";
        public static final String FILE_SHIFT_HOURS_OBJECT = "shift_hours.obj";

        public static final String KEY_SHOULD_CHANGE_BACK_BUTTON = "ZK9kHT5XGAgQcudZMNdC";
        public static final String KEY_IN = "vBBzH6KqhQxWovWNqUdR";
        public static final String KEY_OUT = "uTeESlcq5ma8xlLN6ZrE";

        public static final String VALUE_DELETE_ACCOUNT = "plYcwgEx6PZJ0z0o5MJyCQ4oH2uLn8fyBJ8427H8";
        public static final String VALUE_SHIFT_JSON_KEY = "json_shiftrqs_value";
        public static final String VALUE_COMMENT_JSON_KEY = "json_comment_value";
    }

    /**
     * INSTRUCTIONS:
     * {NAME OF INT}
     */
    final class Ints {
        public static final Integer PGS_STARTED_WRKPLC_CHECK = 0xf123a839;
        public static final Integer PGS_STARTED_EMAIL_CHECK = 0x132f36ab;
        public static final Integer PGS_STARTED_EMAIL_VERIFICATION = 0x5239deaf;
        public static final Integer PGS_STARTED_EMAIL_ENCRYPTION = 0x97f3ac73;
        public static final Integer PGS_STARTED_ADDING_USER = 0x0180c36fd;

        public static final int JOB_TYPE_OWNER = -1;
        public static final int JOB_TYPE_NULL = 0;
        public static final int JOB_TYPE_BARMEN = 1;
        public static final int JOB_TYPE_WAITER = 2;
        public static final int JOB_TYPE_COOK = 3;
        public static final int JOB_TYPE_SHIFT_MANAGER = 4;
        public static final int JOB_TYPE_HOSTESS = 5;
        public static final int JOB_TYPE_MANAGER = 6;
    }

    /**
     * INSTRUCTIONS:
     * {NAME OF GENERAL PURPOSE}_{NAME OF FLOW CONTROL GATE}
     */
    final class Fc_Keys {
        public static final String LOGIN_OR_REGISTER_FINISHED = "qWJC3HRAlcgoEIfFFxPD";
        public static final String LOGIN_FAILED = "joHZTHbYUYJRdkZMrzIs";

        public static final String SCHDULE_INFO_UPLOADED = "wtg70BHa4dSRVXsweLHq";
        public static final String SHIFT_SCHEDULE_SETTINGS_PULLED = "5GpKsDhDuyaDQ6J18Zd6";

        public static final String SPEC_SETTING_RESTS_PULLED = "uTsfTqqqvLiooUcYtT5K";
        public static final String SPEC_SETTINGS_EXPANDABLE_INFO_PULLED = "l3GUKFIn8qpfnlyIncIs";
        public static final String SPEC_SETTINGS_TYPES_PULLED = "Q7uMFHOgmUb7RwIDMfqQ";
        public static final String SPEC_SETTINGS_SET_PULLED = "Pw5Lhhgq0h2j2onW7L6c";

        public static final String USER_ACCEPTED = "wqDbAUDCHqVQrZ7xT94A";
        public static final String USERS_RQS_LIST_PULLED = "SakoPszg4WxKHIYFENwP";
        public static final String USER_IP_PULLED = "yGf5z9PmZ7oUCBgs1jUX";

        public static final String USER_LIST_PULLED = "x2rOX9ktMji3J9sUxaEv";
        public static final String JOBS_PULLED = "8CANGzD1UkVFz2LFUtWo";
    }

    final class DialogFragment_Keys {
        public static final String CONNECTION_KEY = "connection_dialog_key";
        public static final String TWO_BUTTON_DIALOG = "two_button_dialog";

        public static final String UPLOADING_KEY = "uploading_data_key";
        public static final String FETCHING_DATA = "fetching_data_key";

        public static final String SHIFT_SUBMISSION_ERROR = "shift_submission_error";
        public static final String SPEC_SETTINGS_VALIDATION_ERROR = "spec_settings_validation_error";
    }
}
