package alon.com.shifter.base_classes;

import java.util.HashMap;

/**
 * This class is the same as {@link FinishableTask}, but with the added option of putting parameters in.
 *
 * @since V.1.0
 */

public abstract class FinishableTaskWithParams extends FinishableTask {

    /**
     * The parameter map.
     */
    private HashMap<String, Object> mParams = new HashMap<>();

    @Override
    public abstract void onFinish();

    /**
     * Adds a param to {@link #mParams}.
     *
     * @param key   - The parameter key {@link alon.com.shifter.base_classes.Consts.Param_Keys}.
     * @param param - The parameter itself.
     */
    public void addParamToTask(String key, Object param) {
        mParams.put(key, param);
    }

    /**
     * @return {@link #mParams}
     */
    protected HashMap<String, Object> getParamsFromTask() {
        return mParams;
    }
}
