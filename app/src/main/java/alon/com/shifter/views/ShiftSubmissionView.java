package alon.com.shifter.views;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.FinishableTaskWithParams;
import alon.com.shifter.dialog_fragments.ShiftCommentDialogFragment;
import alon.com.shifter.utils.SpecSettingsInforcer;
import alon.com.shifter.utils.Util;
import alon.com.shifter.utils_shift.Comment;
import alon.com.shifter.utils_shift.Shift;
import alon.com.shifter.utils_shift.ShiftInfo;
import alon.com.shifter.utils_shift.SpecSettings;

public class ShiftSubmissionView extends RelativeLayout implements View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = "ShiftSubmissionView";
    private final int[] IDS = {R.id.V_SS_SLV_morn, R.id.V_SS_SLV_afr_non, R.id.V_SS_SLV_evening, R.id.V_SS_SLV_night};

    private ShiftLineView[] mViews;

    private boolean isManager;
    private FragmentManager mFragManager;

    private SpecSettings mSpecSettings;
    private SpecSettingsInforcer mInforcer;

    public ShiftSubmissionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ShiftSubmissionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context mCon, AttributeSet attrs) {
        if (mCon instanceof Activity) {
            Log.i(TAG, "init: Context is instance of activity.");
            mFragManager = ((Activity) mCon).getFragmentManager();
        } else
            throw new RuntimeException("Context is not instance of activity.");
        TypedArray arr = mCon.obtainStyledAttributes(attrs, R.styleable.ShiftSubmissionView, 0, 0);
        isManager = arr.getBoolean(R.styleable.ShiftSubmissionView_isManager, false);
        arr.recycle();
        inflate(mCon, R.layout.view_shift_submission, this);
        mViews = new ShiftLineView[4];
        for (int i = 0; i < 4; i++)
            (mViews[i] = (ShiftLineView) findViewById(IDS[i])).setTag(i);
        if (!isInEditMode() && !isManager) {
            Util mUtil = Util.getInstance(mCon);
            String shifts = mUtil.readPref(mCon, Consts.Pref_Keys.USR_SHIFT_SCHEDULE, Consts.Strings.NULL).toString();
            try {
                JSONObject mJson = new JSONObject(shifts);
                for (int i = 0; i < 7; i++) {
                    String dayName = mUtil.getDayString(mCon, i + 1);
                    try {
                        JSONObject mObj = (JSONObject) mJson.get(dayName);
                        for (int j = 0; j < 4; j++) {
                            String dayShiftName = mUtil.getShiftTitle(mCon, j);
                            boolean canSubmit = !(mObj.get(dayShiftName).equals("(-1)"));
                            mViews[j].setEnabled(i, canSubmit);
                            mViews[j].setOnCheckedChangeListener(i, this);
                            mViews[j].setLongClickListener(i, this);
                        }
                    } catch (ClassCastException ex) {
                        for (int j = 0; j < 4; j++) {
                            mViews[j].setEnabled(i, false);
                            mViews[j].setOnCheckedChangeListener(i, this);
                            mViews[j].setLongClickListener(i, this);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!isManager) {
            ShiftCommentDialogFragment mFrag = new ShiftCommentDialogFragment();
            mFrag.setTitle(v.getContext().getString(R.string.dialog_shift_hours_title));
            String[] tagParts = v.getTag().toString().split("-");
            int pos = Integer.parseInt(tagParts[0]);
            int hash = Integer.parseInt(tagParts[1]);
            for (ShiftLineView mView : mViews) {
                if (mView.hashCode() == hash) {
                    String comment = mView.getShiftCommnent(pos);
                    if (comment != null && !comment.isEmpty())
                        mFrag.setComment(comment);
                }
            }
            mFrag.setTask(new ViewContainer_FinishableTaskWithParams(v) {
                @Override
                public void onFinish() {
                    String comment = (String) getParamsFromTask().get(Consts.Param_Keys.KEY_SHIFT_COMMENT);
                    if (comment != null && !comment.isEmpty()) {
                        String tag = (String) this.view.getTag();
                        String[] partsTag = tag.split("-");
                        int pos = Integer.parseInt(partsTag[0]);
                        int hash = Integer.parseInt(partsTag[1]);
                        for (ShiftLineView mView : mViews)
                            if (mView.hashCode() == hash)
                                mView.setShiftComment(pos, comment);

                    }
                }
            });
            mFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            mFrag.show(mFragManager, "");
            return true;
        } else
            return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (ShiftLineView view : mViews)
            view.setEnabled(enabled);
    }

    public Shift getSelectedShifts() {
        Shift mShift = new Shift();
        Comment[] mComments = null;
        if (!isManager)
            mComments = new Comment[]{
                    new Comment(), new Comment(), new Comment(), new Comment(), new Comment(), new Comment(), new Comment()
            };
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 4; j++) {
                mShift.getInfoComplete()[i].set(j, mViews[j].getShifts()[i]);
                if (!isManager && mComments != null)
                    mComments[i].setComment(j, mViews[j].getShiftCommnent(i));
            }
        if (mComments != null)
            mShift.setComments(mComments);
        return mShift;
    }

    public void constructViewFromShift(Shift shift) {
        ShiftInfo[] mInfo = shift.getInfoComplete();
        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 4; j++)
                mViews[j].setToggleState(i, mInfo[i].getForBool(j));
    }

    public void setSpecSettings(SpecSettings settings, String specSettingsString) {
        mSpecSettings = settings;
        if (mSpecSettings != null && !mSpecSettings.equals(SpecSettings.getEmpty()) && !mSpecSettings.isEmpty()) {
            String shifts = Util.getInstance(getContext()).readPref(getContext(), Consts.Pref_Keys.USR_SHIFT_SCHEDULE, Consts.Strings.NULL).toString();
            mInforcer = SpecSettingsInforcer.getInstance(mSpecSettings, specSettingsString, shifts, getContext());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mInforcer != null) {
            String[] tagParts = buttonView.getTag().toString().split("-");
            int pos = Integer.parseInt(tagParts[0]);
            int hash = Integer.parseInt(tagParts[1]);
            for (int i = 0; i < mViews.length; i++)
                if (hash == mViews[i].hashCode())
                    mInforcer.onUpdatedChange(pos, i);
        }
    }

    public boolean[] submit() {
        return mInforcer == null ? null : mInforcer.checkLimits();
    }


    abstract class ViewContainer_FinishableTaskWithParams extends FinishableTaskWithParams {

        protected View view;

        ViewContainer_FinishableTaskWithParams(View v) {
            view = v;
        }
    }
}
