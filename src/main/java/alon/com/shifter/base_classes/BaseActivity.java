package alon.com.shifter.base_classes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import alon.com.shifter.utils.Util;

/**
 * The BaseActivity class is the base class, all activities in the app must use.
 * The reason behind this is due to the fact that we want all activities to implement the {@link #setupUI()} function.
 * <b>Note:</b> there is no override for the {@link #onCreate(Bundle)} function, it is supposed to be overridden by you.
 */
public abstract class BaseActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, Consts {

    /**
     * The 'TAG' string used in {@link Log}'s static method.
     *
     * @since V.1.0
     */
    protected String TAG = ">>base_activity_tag<<";

    /**
     * Base util instance.
     *
     * @since V.1.0
     */
    protected Util mUtil;

    /**
     * An abstract method that is a must-have in every activity class in this app.
     * The purpose of this method is to generate the pointers of the views on the screen, as well as anything else that might be used.
     *
     * @since V.1.0
     */
    protected abstract void setupUI();

    /**
     * <b>Note:</b> if not overridden, then will cause an error without any processing to checked change.
     *
     * @param buttonView
     *         - the button that had its state changed.
     * @param isChecked
     *         - the value of the state.
     *
     * @see android.widget.CompoundButton.OnCheckedChangeListener
     * @since V.1.0
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.e(TAG, "onCheckedChanged:Didn't override oncheckedchanged and tried to use it. ");
    }


    /**
     * <b>Note:</b> if not overridden, then will cause an error without any processing to click.
     *
     * @param v
     *         - the view that was clicked.
     *
     * @see android.view.View.OnClickListener
     * @since V.1.0
     */
    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick: Didn't override onclick and tried to use it.");
    }

    /**
     * A function that is used in order to get the {@link Util} object.
     * <b>Note: MUST BE CALLED IN EVERY {@link Activity#onCreate(Bundle)}!!!</b>
     *
     * @param mCon
     *         - the context.
     *
     * @throws IllegalAccessException
     *         - When called not from "onCreate".
     */
    @SuppressWarnings("JavaDoc")
    protected void getUtil(Context mCon) {
        StackTraceElement[] mElems = Thread.currentThread().getStackTrace();
        for (StackTraceElement mElem : mElems)
            if (mElem.getMethodName().equals("onCreate")) {
                mUtil = Util.getInstance(mCon);
                return;
            }
        try {
            throw new IllegalAccessException();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
