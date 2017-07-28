package alon.com.shifter.wrappers;

import android.os.AsyncTask;

/**
 * Created by Alon on 1/8/2017.
 */

public abstract class AsyncWrapper extends AsyncTask<Void, Void, Void> implements WrapperBaseAdapter {


    @Override
    public Object getWrapperParam(String key) {
        return mWrapperParams.get(key);
    }

    @Override
    public WrapperBase setWrapperParam(String key, Object value) {
        mWrapperParams.put(key, value);
        return this;
    }
}
