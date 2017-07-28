package alon.com.shifter.base_classes;

import java.io.Serializable;

/**
 * The FinishableTask is a class that is used to maintain flow within the app, used mostly with {@link alon.com.shifter.utils.FirebaseUtil}, as most methods
 * inside the {@linkplain alon.com.shifter.utils.FirebaseUtil} class contain a <b>FinishableTask</b> parameter.
 *
 * @since V.1.0
 */

public abstract class FinishableTask implements Serializable {

    /**
     * The onFinish method is a method that is called when an action is finished.<br>
     * <b>Note:</b> this method does not take into consideration whether the action succeeded or failed, it merely states that the action was complete.
     */
    public abstract void onFinish();
}
