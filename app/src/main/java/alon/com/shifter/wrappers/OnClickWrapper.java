package alon.com.shifter.wrappers;

import android.view.View;

/**
 * Created by Alon on 1/8/2017.
 */

public abstract class OnClickWrapper implements View.OnClickListener, WrapperBaseAdapter {
    @Override
    public WrapperBase setWrapperParam(String key, Object value) {
        mWrapperParams.put(key, value);
        return this;
    }

    @Override
    public Object getWrapperParam(String key) {
        return mWrapperParams.get(key);
    }
}
