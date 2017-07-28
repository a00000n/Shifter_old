package alon.com.shifter.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.wrappers.OnCheckedChangedWrapper;
import alon.com.shifter.wrappers.OnClickWrapper;

import static alon.com.shifter.wrappers.WrapperBase.CHECK_BOX_ID;
import static alon.com.shifter.wrappers.WrapperBase.LAYOUT_ID;

/**
 * A class representing a single container of special settings,
 * It contains one header, which states the special settings name, in the form of a layout,
 * and one layout containing multiple layouts, each of which contains a textview and a checkbox, which represent the sub-settings for
 * this particular special setting.
 */
@SuppressWarnings("RedundantCast")
public class SpecSettingView extends LinearLayout {

    /**
     * The tag for the header container.
     */
    private static final String HEADER_TAG = "Header_Check_Box";
    /**
     * The tag for the child container.
     */
    private static final String CHILD_TAG = "Child_Tag";
    /**
     * A sorted map for the values.
     */
    private final TreeMap<Integer, String> mChildrenSelected = new TreeMap<>();
    /**
     * The name of the header in this special settings.
     */
    private String mHeader;
    /**
     * The name of chidren for this special settings.
     */
    private String mChildren;
    /**
     * The behavioural restrictions for this special settings.
     */
    private String mRestrictions;
    /**
     * The value for the restriction.
     */
    private int mRestValue;

    private Context mCon;

    private Toast mToast;

    private ArrayList<CheckBox> mBoxes = new ArrayList<>();

    @SuppressLint("ShowToast")
    public SpecSettingView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        mCon = context;
        mToast = Toast.makeText(context, R.string.spec_setting_limit_amount, Toast.LENGTH_SHORT);
    }

    /**
     * The function creates the headers and mChildren of said headers for the view, as well as setting the onCheckedChangedListener for each child and parent.
     */
    public void genViews() {
        if (mHeader != null && mChildren != null) {
            LayoutParams mHeaderParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutParams mChildParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutParams mDefaultParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //Contains all mHeader views.
            LinearLayout mHeader = new LinearLayout(mCon);
            mHeader.setOrientation(HORIZONTAL);
            mHeader.setWeightSum(1);

            // Contains all child views for mHeader
            LinearLayout mContainer = new LinearLayout(mCon);
            mContainer.setOrientation(VERTICAL);
            mContainer.setVisibility(View.GONE);

            CheckBox mActiveCat = new CheckBox(mCon);
            TextView mTitle = new TextView(mCon);
            mTitle.setText(this.mHeader);
            mTitle.setTextAppearance(mCon, android.R.style.TextAppearance_DeviceDefault_Medium);
            mTitle.setTypeface(Typeface.DEFAULT_BOLD);
            mTitle.setGravity(Gravity.CENTER);
            OnClickWrapper wrapper = new OnClickWrapper() {
                @Override
                public void onClick(View v) {
                    ((CheckBox) getWrapperParam(CHECK_BOX_ID)).setChecked(!((CheckBox) getWrapperParam(CHECK_BOX_ID)).isChecked());
                }
            };
            wrapper.setWrapperParam(CHECK_BOX_ID, mActiveCat);
            mTitle.setOnClickListener(wrapper);

            mHeaderParams.weight = 0.925f;

            mHeader.addView(mTitle, mHeaderParams);

            mActiveCat.setText("");
            mActiveCat.setTag(mTitle.getText().toString());
            OnCheckedChangedWrapper wrapperChecked = new OnCheckedChangedWrapper() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    LinearLayout mLayout = (LinearLayout) getWrapperParam(LAYOUT_ID);
                    if (isChecked) {
                        mLayout.setVisibility(View.VISIBLE);
                        //Loop through all child checkboxes.
                        for (CheckBox cb : mBoxes) {
                            if (cb.isChecked()) {
                                for (int i = 0; i < mLayout.getChildCount(); i++) {
                                    LinearLayout layout = (LinearLayout) mLayout.getChildAt(i);
                                    int cbIndex = layout.getChildAt(0) instanceof CheckBox ? 0 : 1;
                                    CheckBox checkBox = (CheckBox) layout.getChildAt(cbIndex);
                                    TextView textView = (TextView) layout.getChildAt(cbIndex == 0 ? 1 : 0);
                                    if (checkBox.getTag() != null) {
                                        int tag = (int) checkBox.getTag();
                                        String setting = mChildrenSelected.get(tag);
                                        if (setting == null || setting.isEmpty())
                                            mChildrenSelected.put(tag, textView.getText().toString());
                                        else if (!setting.equals(textView.getText().toString())) {
                                            Log.i(CHILD_TAG, "onCheckedChanged: changing setting from: " + setting + " to: " + textView.getText().toString());
                                            mChildrenSelected.put(tag, textView.getText().toString());
                                        } else
                                            Log.i(CHILD_TAG, "onCheckedChanged: tried to set the same tag.");
                                    }
                                }
                            }
                        }
                    } else {
                        //If the header isn't checked then remove all selected items.
                        mLayout.setVisibility(View.GONE);
                        mChildrenSelected.clear();
                    }
                }
            };
            wrapperChecked.setWrapperParam(LAYOUT_ID, mContainer);
            mActiveCat.setOnCheckedChangeListener(wrapperChecked);

            mHeaderParams.weight = 1 - mHeaderParams.weight;

            mHeader.addView(mActiveCat, mHeaderParams);
            mHeader.setTag(HEADER_TAG);

            addView(mHeader, mDefaultParams);

            View mSep = new View(mCon);
            float scale = mCon.getResources().getDisplayMetrics().density;
            float height = (2 * scale + 0.5f);
            LayoutParams mSepParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) height);

            mSep.setBackgroundColor(Color.LTGRAY);

            addView(mSep, mSepParams);

            String[] childrenTemp = this.mChildren.split("~");
            String[] childrenSet = new String[childrenTemp.length - 1];
            System.arraycopy(childrenTemp, 1, childrenSet, 0, childrenTemp.length - 1);
            int count = 0;
            for (String child : childrenSet) {
                LinearLayout mAddedLayout = new LinearLayout(mCon);
                mAddedLayout.setOrientation(HORIZONTAL);
                mAddedLayout.setWeightSum(1);


                final CheckBox mBox = new CheckBox(mCon);
                mBox.setText("");
                mBox.setTag(count++);
                final TextView mChildName = new TextView(mCon);

                child = "\t" + child;
                mChildName.setText(child);
                mChildName.setTextAppearance(mCon, android.R.style.TextAppearance_DeviceDefault_Medium);
                mChildName.setGravity(Gravity.RIGHT);
                mChildParams.weight = 0.925f;

                mChildName.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBox.setChecked(!mBox.isChecked());
                    }
                });

                mAddedLayout.addView(mChildName, mChildParams);
                mChildParams.weight = 1 - mChildParams.weight;

                mBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int tag = (int) buttonView.getTag();
                        switch (mRestrictions) {
                            case Consts.Strings.MGR_SEC_SPEC_SETTING_REST_LIMIT_AMOUNT:
                                if (getCheckedAmount() <= mRestValue)
                                    if (isChecked) {
                                        mChildrenSelected.put(tag, mChildName.getText().toString());
                                    } else
                                        mChildrenSelected.remove(tag);
                                else {
                                    mBox.setChecked(false);
                                    if (!mToast.getView().isShown())
                                        ((Activity) mCon).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mToast.show();
                                            }
                                        });
                                }
                                break;
                            case Consts.Strings.MGR_SEC_SPEC_SETTING_REST_RADIO_BTN_BEHAVIOUR:
                                if (isChecked)
                                    for (CheckBox cb : mBoxes) {
                                        if (cb != mBox)
                                            cb.setChecked(false);
                                        else {
                                            mChildrenSelected.clear();
                                            mChildrenSelected.put(tag, mChildName.getText().toString());
                                        }
                                    }

                                break;
                            default:
                                if (isChecked) {
                                    mChildrenSelected.put(tag, mChildName.getText().toString());
                                } else
                                    mChildrenSelected.remove(tag);
                                break;
                        }
                    }
                });

                mBoxes.add(mBox);

                mAddedLayout.addView(mBox, mChildParams);
                mContainer.addView(mAddedLayout, mDefaultParams);
            }
            mContainer.setTag(CHILD_TAG);
            addView(mContainer, mDefaultParams);
        } else
            throw new RuntimeException("Header or mChildren are not setGate.");
    }

    public boolean isSelected() {
        return mChildrenSelected.isEmpty();
    }

    public boolean isActive() {
        LinearLayout mHeader = (LinearLayout) findViewWithTag(HEADER_TAG);
        for (int i = 0; i < mHeader.getChildCount(); i++) {
            View mView = mHeader.getChildAt(i);
            if (mView instanceof CheckBox)
                return ((CheckBox) mView).isChecked();
        }
        return false;
    }

    public boolean isAnyChildChecked() {
        LinearLayout mChildLayout = (LinearLayout) findViewWithTag(CHILD_TAG);
        for (int i = 0; i < mChildLayout.getChildCount(); i++) {
            LinearLayout mGrandChild = (LinearLayout) mChildLayout.getChildAt(i);
            for (int j = 0; j < mGrandChild.getChildCount(); j++) {
                View mView = mGrandChild.getChildAt(j);
                if (mView instanceof CheckBox)
                    if (((CheckBox) mView).isChecked())
                        return true;
            }
        }
        return false;
    }

    public String getChildrenSelected() {
        if (!mChildrenSelected.isEmpty()) {
            String children = "";
            for (Map.Entry<Integer, String> str : mChildrenSelected.entrySet())
                children += str.getValue().trim() + "~";
            return children.substring(0, children.lastIndexOf('~'));
        } else return "";
    }

    public String getHeader() {
        return mHeader;
    }

    public void setHeader(String mHeader) {
        this.mHeader = mHeader;
    }

    public void setHeaderChecked(boolean flag) {
        LinearLayout mHeaderLayout = (LinearLayout) findViewWithTag(HEADER_TAG);
        for (int i = 0; i < mHeaderLayout.getChildCount(); i++) {
            View mChild = mHeaderLayout.getChildAt(i);
            if (mChild instanceof CheckBox) {
                ((CheckBox) mChild).setChecked(flag);
                return;
            }
        }
    }

    public void setChildChecked(String str, boolean flag) {
        LinearLayout mCorrectLayout = null;

        LinearLayout mChildLayout = (LinearLayout) findViewWithTag(CHILD_TAG);
        for (int i = 0; i < mChildLayout.getChildCount(); i++) {
            LinearLayout mGrandChild = (LinearLayout) mChildLayout.getChildAt(i);
            for (int j = 0; j < mGrandChild.getChildCount(); j++) {
                View mView = mGrandChild.getChildAt(j);
                if (mView instanceof TextView)
                    if (((TextView) mView).getText().toString().trim().equals(str)) {
                        mCorrectLayout = mGrandChild;
                        break;
                    }
            }
            if (mCorrectLayout != null)
                break;
        }

        if (mCorrectLayout == null) {
            Log.d("Shifter_SSView", "setChildChecked: mCorrectLayout is null, exiting.");
            return;
        }
        for (int i = 0; i < mCorrectLayout.getChildCount(); i++) {
            View mView = mCorrectLayout.getChildAt(i);
            if (mView instanceof CheckBox) {
                ((CheckBox) mView).setChecked(flag);
                return;
            }
        }
    }

    public void setChildren(String mChildren) {
        this.mChildren = mChildren;
    }

    public void setRestrictions(String rest, int value) {
        mRestrictions = rest;
        mRestValue = value;
    }

    private int getCheckedAmount() {
        int count = 0;
        for (CheckBox mBox : mBoxes)
            if (mBox.isChecked())
                count++;
        return count;
    }

}
