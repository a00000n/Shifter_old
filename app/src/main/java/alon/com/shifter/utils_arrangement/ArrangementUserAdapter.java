package alon.com.shifter.utils_arrangement;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import alon.com.shifter.R;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.dialog_fragments.TimePickerDialogFragment;
import alon.com.shifter.wrappers.OnCheckedChangedWrapper;
import alon.com.shifter.wrappers.OnClickWrapper;

public class ArrangementUserAdapter extends ArrayAdapter<ArrangementUser> {

    private ArrangementArrayList mUsers;
    private Context mCon;
    private int mCurrentDay;

    public ArrangementUserAdapter(Context context, ArrangementArrayList users, int currentDay) {
        super(context, R.layout.listview_arrgment_user_item, users);
        mUsers = users;
        mCon = context;
        mCurrentDay = currentDay;
    }

    public void setDay(int day) {
        mCurrentDay = day;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mUsers.cancelAllListedStatusWithoutUpdate();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ViewContainer container = null;
        if (view == null || view.getId() == R.id.LV_MGR_ARRGMENT_title) {
            if (mUsers.get(position).getIsTitleRow()) {
                view = LayoutInflater.from(mCon).inflate(R.layout.listview_arrgment_title_item, parent, false);
                ((TextView) view.findViewById(R.id.LV_MGR_ARRGMENT_title)).setText(mUsers.get(position).getTitleRow());
            } else {
                view = LayoutInflater.from(mCon).inflate(R.layout.listview_arrgment_user_item, parent, false);
                container = new ViewContainer();
                TextView name = (TextView) view.findViewById(R.id.LV_MGR_ARRGMENT_user_name);
                TextView comment = (TextView) view.findViewById(R.id.LV_MGR_ARRGMENT_user_comment);
                CheckBox listed = (CheckBox) view.findViewById(R.id.LV_MGR_ARRGMENT_user_cb);
                Button define = (Button) view.findViewById(R.id.LV_MGR_ARRGMENT_user_define);
                container.mName = name;
                container.mComment = comment;
                container.mIsListed = listed;
                container.mHourDefine = define;
                view.setTag(container);
            }
        } else {
            if (mUsers.get(position).getIsTitleRow()) {
                view = LayoutInflater.from(mCon).inflate(R.layout.listview_arrgment_title_item, parent, false);
                ((TextView) view.findViewById(R.id.LV_MGR_ARRGMENT_title)).setText(mUsers.get(position).getTitleRow());
            } else
                container = (ViewContainer) view.getTag();
        }

        if (container != null) {

            OnClickWrapper nameClickWrapper = new OnClickWrapper() {
                @Override
                public void onClick(View v) {
                    ViewContainer container = (ViewContainer) getWrapperParam(Integer.toString(hashCode()));
                    container.mIsListed.setChecked(!container.mIsListed.isChecked());
                }
            };
            nameClickWrapper.setWrapperParam(Integer.toString(nameClickWrapper.hashCode()), container);

            OnCheckedChangedWrapper checkedWrapper = new OnCheckedChangedWrapper() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int pos = (int) getWrapperParam(Integer.toString(hashCode()));
                    mUsers.setListed(pos, isChecked, mCurrentDay);
                }
            };
            checkedWrapper.setWrapperParam(Integer.toString(checkedWrapper.hashCode()), position);

            OnClickWrapper defineClickWrapper = new OnClickWrapper() {
                @Override
                public void onClick(View v) {
                    int pos = (int) getWrapperParam(Integer.toString(hashCode()));

                    TimePickerDialogFragment fromTimePicker = new TimePickerDialogFragment();
                    TimePickerDialogFragment toTimePicker = new TimePickerDialogFragment();

                    OnClickWrapper toTimeCancelClickWrapper = new OnClickWrapper() {
                        @Override
                        public void onClick(View v) {
                            TimePickerDialogFragment toTimeFrag = (TimePickerDialogFragment) getWrapperParam(Integer.toString(hashCode()));
                            toTimeFrag.setPreviousTime("");
                            toTimeFrag.dismiss();
                        }
                    };
                    toTimeCancelClickWrapper.setWrapperParam(Integer.toString(toTimeCancelClickWrapper.hashCode()), toTimePicker);

                    OnClickWrapper toTimeDoneClickWrapper = new OnClickWrapper() {
                        @Override
                        public void onClick(View v) {
                            int pos = (int) getWrapperParam(WRAPPER_CONVERTED_VIEW_POSITION_ID);
                            TimePickerDialogFragment toTimeFrag = (TimePickerDialogFragment) getWrapperParam(Integer.toString(hashCode()));

                            String time = toTimeFrag.getTime();

                            mUsers.setTime(pos, time, mCurrentDay);
                            Toast.makeText(getContext(), getContext().getString(R.string.dialog_arrangment_defined), Toast.LENGTH_SHORT).show();
                            toTimeFrag.dismiss();
                        }
                    };
                    toTimeDoneClickWrapper.setWrapperParam(WRAPPER_CONVERTED_VIEW_POSITION_ID, pos).setWrapperParam(Integer.toString(toTimeDoneClickWrapper.hashCode()), toTimePicker);


                    OnClickWrapper fromTimeCancelClickWrapper = new OnClickWrapper() {
                        @Override
                        public void onClick(View v) {
                            TimePickerDialogFragment fromTimeFrag = (TimePickerDialogFragment) getWrapperParam(Integer.toString(hashCode()));
                            fromTimeFrag.dismiss();
                        }
                    };
                    fromTimeCancelClickWrapper.setWrapperParam(Integer.toString(fromTimeCancelClickWrapper.hashCode()), fromTimePicker);

                    OnClickWrapper fromTimeDoneClickWrapper = new OnClickWrapper() {
                        @Override
                        public void onClick(View v) {
                            int pos = (int) getWrapperParam(WRAPPER_CONVERTED_VIEW_POSITION_ID);
                            TimePickerDialogFragment[] fragsArray = (TimePickerDialogFragment[]) getWrapperParam(WRAPPER_DIALOG_FRAGMENT_MULTIPLE_ID);
                            OnClickWrapper[] wrapperArray = (OnClickWrapper[]) getWrapperParam(WRAPPER_WRAPPER_OBJECT_MULTIPLE_ID);

                            //From frag
                            TimePickerDialogFragment fragFrom = fragsArray[0];
                            //To frag
                            TimePickerDialogFragment fragTo = fragsArray[1];
                            fragTo.setPreviousTime(fragFrom.getTime());

                            fragFrom.dismiss();

                            String info = getContext().getString(R.string.dialog_arrangment_define_end_hours) +
                                    mUsers.get(pos).getUser().getName() + "\n" + getContext().getString(R.string.dialog_arrangment_start_hour) +
                                    fragTo.getPreviousTime();

                            fragTo.setTitle(getContext().getString(R.string.dialog_arrangment_to_shift_time));
                            fragTo.setInfoTxt(info);
                            fragTo.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                            fragTo.setTime(mUsers.get(pos).getTime(), 1);
                            fragTo.setOnCancelListener(wrapperArray[0]);
                            fragTo.setOnDoneListener(wrapperArray[1]);
                            fragTo.show(((Activity) getContext()).getFragmentManager(), Consts.DialogFragment_Keys.TIME_PICKER_SHIFT_END);
                        }
                    };
                    fromTimeDoneClickWrapper
                            .setWrapperParam(WRAPPER_DIALOG_FRAGMENT_MULTIPLE_ID, new TimePickerDialogFragment[]{fromTimePicker, toTimePicker})
                            .setWrapperParam(WRAPPER_WRAPPER_OBJECT_MULTIPLE_ID, new OnClickWrapper[]{toTimeCancelClickWrapper, toTimeDoneClickWrapper})
                            .setWrapperParam(WRAPPER_CONVERTED_VIEW_POSITION_ID, pos);


                    String info = getContext().getString(R.string.dialog_arrangment_define_start_hours) + mUsers.get(pos).getUser().getName();

                    fromTimePicker.setTitle(getContext().getString(R.string.dialog_arrangment_from_shift_time));
                    fromTimePicker.setInfoTxt(info);
                    fromTimePicker.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    fromTimePicker.setTime(mUsers.get(pos).getTime(), 0);
                    fromTimePicker.setOnCancelListener(fromTimeCancelClickWrapper);
                    fromTimePicker.setOnDoneListener(fromTimeDoneClickWrapper);
                    fromTimePicker.show(((Activity) getContext()).getFragmentManager(), Consts.DialogFragment_Keys.TIME_PICKER_SHIFT_START);
                }
            };
            defineClickWrapper.setWrapperParam(Integer.toString(defineClickWrapper.hashCode()), position);

            String comment = mUsers.get(position).getComment();
            if (comment != null && !comment.isEmpty()) {
                container.mComment.setVisibility(View.VISIBLE);
                container.mComment.setText(comment);
            } else
                container.mComment.setVisibility(View.GONE);

            container.mName.setOnClickListener(nameClickWrapper);
            container.mName.setText(mUsers.get(position).getUser().getName());
            container.mIsListed.setOnCheckedChangeListener(checkedWrapper);
            container.mHourDefine.setOnClickListener(defineClickWrapper);
            if (mUsers.isListed(position, mCurrentDay))
                container.mIsListed.setChecked(true);
            else
                container.mIsListed.setChecked(false);
        }
        return view;
    }

    private class ViewContainer {
        TextView mName;
        TextView mComment;
        Button mHourDefine;
        CheckBox mIsListed;
    }
}