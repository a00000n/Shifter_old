package alon.com.shifter.wrappers;

import alon.com.shifter.base_classes.FinishableTask;

/**
 * Created by Alon on 1/8/2017.
 */

public abstract class FinishableTaskWrapper extends FinishableTask implements WrapperBaseAdapter {

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
