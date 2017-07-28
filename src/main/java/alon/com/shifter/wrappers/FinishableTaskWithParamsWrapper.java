package alon.com.shifter.wrappers;

import alon.com.shifter.base_classes.FinishableTaskWithParams;

/**
 * Created by Alon on 1/11/2017.
 */

public abstract class FinishableTaskWithParamsWrapper extends FinishableTaskWithParams implements WrapperBaseAdapter {

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
