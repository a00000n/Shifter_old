package alon.com.shifter.base_classes;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Alon on 2/6/2017.
 */

public class SwipeGestureDetector implements View.OnTouchListener {

    private static final float MIN_SWIPE_DEFAULT = 30f;

    private OnSwipeListener swipeListener;
    private float startX = -1;
    private float startY = -1;
    private float endX = -1;
    private float endY = -1;
    private float minSwipeTrigger;

    public SwipeGestureDetector(View view) {
        this(view, MIN_SWIPE_DEFAULT);
    }

    public SwipeGestureDetector(View view, float minSwipe) {
        minSwipeTrigger = minSwipe;
    }

    public void setListener(OnSwipeListener listener) {
        swipeListener = listener;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startX = event.getX();
            startY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            endX = event.getX();
            endY = event.getY();

            float deltaX = endX - startX;
            float deltaY = endY - startY;

            //SWIPE ALONG WIDTH
            if (Math.abs(deltaX) >= Math.abs(deltaY)) {
                Log.i("SWIPE_GESTURE_DETECTOR", "onTouch: Swipe detected.");
                if (Math.abs(deltaX) > minSwipeTrigger) {
                    swipeListener.onSwipe(v, deltaX < 0 ? SwipeType.RIGHT_TO_LEFT : (deltaX > 0 ? SwipeType.LEFT_TO_RIGHT : null));
                    return true;
                } else
                    return false;
            } // NO NEED FOR HORIZONTAL SWIPE
            //Reset.
            startX = startY = endY = endX = -1;
        }
        return true;
    }

    public static boolean hierarchyCheck(View currentSearchScope, int searchId) {
        try {
            View parent = (View) currentSearchScope.getParent();
            if (parent != null)
                //noinspection SimplifiableIfStatement
                if (parent.getId() == searchId)
                    return true;
                else
                    return hierarchyCheck(parent, searchId);
            else
                return false;
        } catch (ClassCastException ex) {
            return false;
        }
    }

    public interface OnSwipeListener {
        void onSwipe(View touchedView, SwipeType swipeType);
    }

    public enum SwipeType {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP;
    }
}
