package alon.com.shifter.wrappers;

import alon.com.shifter.base_classes.TaskResult;

/**
 * Created by Alon on 1/22/2017.
 */

public abstract class TaskResultWrapper implements TaskResult, WrapperBaseAdapter {

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
