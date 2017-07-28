package alon.com.shifter.base_classes;

/**
 * TaskResult is an interface that is used, much like {@link FinishableTask}, to maintain flow and control over tha flow of information in the app.
 * <b>However:</b> unlike {@link FinishableTask}, this interface takes into account whether an action has succeeded or failed.
 *
 * @since V.1.0
 */

public interface TaskResult {

    /**
     * The onFail method is called when an action that has a possible positive or negative outcome has failed.
     */
    void onFail();

    /**
     * The onSucceed method is called when an action, that has a possible positive or negative outcome has succeeded.
     */
    void onSucceed();
}
