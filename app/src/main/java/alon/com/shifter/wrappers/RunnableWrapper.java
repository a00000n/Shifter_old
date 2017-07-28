package alon.com.shifter.wrappers;

/**
 * Created by Alon on 2/5/2017.
 */

public abstract class RunnableWrapper implements Runnable, WrapperBaseAdapter {

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
