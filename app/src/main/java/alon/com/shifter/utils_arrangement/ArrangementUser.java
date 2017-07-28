package alon.com.shifter.utils_arrangement;


import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.Consts;

public class ArrangementUser {
    private BaseUser user;

    private String titlePageName;
    private String timeSelection = Consts.Strings.MGR_ARRGMENT_DEFAULT_TIME;
    private String comment;

    private boolean isTitleRow = false;
    private boolean isListed = false;


    public ArrangementUser(BaseUser user) {
        this.user = user;
    }

    //Getters ====================================

    BaseUser getUser() {
        return user;
    }

    boolean isListed() {
        return isListed;
    }

    boolean getIsTitleRow() {
        return isTitleRow;
    }

    String getTime() {
        return timeSelection;
    }

    String getTitleRow() {
        return titlePageName;
    }

    String getComment() {
        return comment;
    }

    //Setters ====================================

    void setTime(String time) {
        timeSelection = time;
    }

    public void setTitleRow(boolean flag) {
        isTitleRow = flag;
    }

    void setListed(boolean listed) {
        isListed = listed;
    }

    public void setTitlePageName(String titlePageName) {
        this.titlePageName = titlePageName;
    }

    void setComment(String cmt) {
        comment = cmt;
    }
}