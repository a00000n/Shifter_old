package alon.com.shifter.base_classes;

import android.content.Context;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Alon on 10/12/2016.
 */

public class BaseUser implements Serializable {

    /**
     * The User's UID, corresponding to {@link FirebaseUser#getUid()}.
     * Initialize to {@link alon.com.shifter.base_classes.Consts.Strings#NULL}.
     */
    private String mUID = Consts.Strings.NULL;

    /**
     * The User's phone number, obtained using the {@link alon.com.shifter.utils.Util#getPhoneNumber(Context)} method.
     * Initialized to {@link alon.com.shifter.base_classes.Consts.Strings#NULL}.
     */
    private String mPhoneNum = Consts.Strings.NULL;

    /**
     * The User's name, full name, obtained when the user registered.
     * Initialized to {@link alon.com.shifter.base_classes.Consts.Strings#NULL}.
     */
    private String mName = Consts.Strings.NULL;

    /**
     * The User's IP, obtained using the {@link Linker} class.
     * Initialized to {@link alon.com.shifter.base_classes.Consts.Strings#NULL}.
     */
    private String mIP = Consts.Strings.NULL;

    /**
     * The User's shorthandcode, obtained once when registered when we created the user on the database.
     * Initialized to {@link alon.com.shifter.base_classes.Consts.Strings#NULL}.
     */
    private String mSHCode = Consts.Strings.NULL;

    /**
     * The User's email, obtained once when registered.
     * Initialized to {@link alon.com.shifter.base_classes.Consts.Strings#NULL}.
     */
    private String mEmail = Consts.Strings.NULL;

    /**
     * The User's workplace code, obtained when created the user.
     * Initialized to {@link alon.com.shifter.base_classes.Consts.Strings#NULL}.
     */
    private String mCode = Consts.Strings.NULL;

    /**
     * The User's job type code, set by the manager when accepting a user.
     * Initialized to {@link alon.com.shifter.base_classes.Consts.Ints#JOB_TYPE_NULL}.
     */
    private int mJobType = Consts.Ints.JOB_TYPE_NULL;

    /**
     * Is the user a manager.
     * Initialized to false.
     */
    private boolean mIsManager = false;


    /**
     * The rating of the user, from 0 to 5.
     * Will be used in auto generation of a schedule.
     */
    private int mRating = 0;

    /**
     * A default {@link BaseUser} instance, empty.
     */
    public BaseUser() {

    }

    /**
     * An instance of {@link BaseUser}, with all the params.
     *
     * @param UID
     *         - The user's UID.
     * @param name
     *         - The user's name.
     * @param phoneNumber
     *         - The user's phone number.
     * @param IP
     *         - The user's IP.
     * @param SHCode
     *         - The user's workplace shorthandcode.
     * @param email
     *         - The user's email.
     * @param isManager
     *         - Is the user a manager.
     * @param code
     *         - The user's workplace code.
     */
    public BaseUser(String UID, String name, String phoneNumber, String IP, String SHCode, String email, boolean isManager, String code) {
        mUID = UID;
        mPhoneNum = phoneNumber;
        mName = name;
        mIP = IP;
        mSHCode = SHCode;
        mEmail = email;
        mIsManager = isManager;
        mCode = code;
    }

    /**
     * A method used to construct the {@link BaseUser} object from the database.
     *
     * @param map
     *         - A map obtained from the database using {@link alon.com.shifter.base_classes.Consts.Fb_Keys#USER_BASE_USER_OBJECT}.
     *
     * @return A base user using the {@link #BaseUser()} Constructor.
     */
    public static BaseUser construct(HashMap<String, Object> map) {
        BaseUser mUser = new BaseUser();
        mUser.setEmail(map.get("email").toString());
        mUser.setIP(map.get("ip").toString());
        mUser.setIsManager((boolean) map.get("manager"));
        mUser.setName(map.get("name").toString());
        mUser.setPhoneNum(map.get("phoneNum").toString());
        mUser.setSHCode(map.get("shcode").toString());
        mUser.setUID(map.get("uid").toString());
        mUser.setCode(map.get("code").toString());
        mUser.setJobType(((Long) map.get("jobType")).intValue());
        mUser.setRating(((Long) map.get("rating")).intValue());
        return mUser;
    }

    public boolean isManager() {
        return mIsManager;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getSHCode() {
        return mSHCode;
    }

    public void setSHCode(String mSHCode) {
        this.mSHCode = mSHCode;
    }

    public String getIP() {
        return mIP;
    }

    public void setIP(String mIP) {
        this.mIP = mIP;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public void setPhoneNum(String mPhoneNum) {
        this.mPhoneNum = mPhoneNum;
    }

    public String getUID() {
        return mUID;
    }

    public void setUID(String mUID) {
        this.mUID = mUID;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String mCode) {
        this.mCode = mCode;
    }

    public int getJobType() {
        return mJobType;
    }

    public void setJobType(int mJobType) {
        this.mJobType = mJobType;
    }

    public void setIsManager(boolean mIsManager) {
        this.mIsManager = mIsManager;
    }

    public int getRating() {
        return mRating;
    }

    public void setRating(int mRating) {
        this.mRating = mRating;
    }
}
