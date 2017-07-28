package alon.com.shifter.wrappers;

import com.google.android.gms.tasks.OnCompleteListener;

/**
 * Created by Alon on 2/12/2017.
 */

public abstract class OnCompleteWrapper<T> implements OnCompleteListener<T>, WrapperBaseAdapter {

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
