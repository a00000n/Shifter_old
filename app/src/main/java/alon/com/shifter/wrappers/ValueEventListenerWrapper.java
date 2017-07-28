package alon.com.shifter.wrappers;

import com.google.firebase.database.ValueEventListener;

/**
 * Created by Alon on 3/2/2017.
 */

public abstract class ValueEventListenerWrapper implements WrapperBaseAdapter, WrapperBase, ValueEventListener {

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
