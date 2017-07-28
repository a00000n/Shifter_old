package alon.com.shifter.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;

import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.FinishableTask;

/**
 * The FlowController class is a class that implements {@link Serializable} interface.<br>
 * The class is used as a event controller of sorts.<br>
 * The class contains a {@link HashMap} of type {@link String} and {@link Boolean}, with the key being taken from {@link alon.com.shifter.base_classes.Consts.Fc_Keys} class.<br>
 * Each key can either be positive or negative, if the key isn't contained in the map, then the "gate", is assumed as closed (i.e. the value of the key is false).<br>
 * The reason this class was constructed is so that there won't be a situation that a user is trying to access some data that is not yet pulled from the server.<br>
 * This way we can maintain a sort of flow in the application, so that if a data isn't pulled the user is prompted a {@link android.app.ProgressDialog} that tells him to wait for a bit until the data is pulled from the server.<br>
 */

public final class FlowController implements Serializable {

    /**
     * A {@link HashMap} of all {@link FinishableTask}s that need to be run, with the corresponding key value,
     * so that once a gate state has changed, it knows which task to call.
     */
    private static HashMap<String, FinishableTask> mTaskOnGateOpen = new HashMap<>();

    /**
     * A {@link HashMap} of all {@link TaskStateContainer}s that need to be run, with the corresponding key value,
     * so that once a gate state has changed, it knows which task to call.
     */
    private static HashMap<String, TaskStateContainer> mTaskOnGateOpenStateStated = new HashMap<>();

    /**
     * The {@link HashMap} of all the gates, note that there is an {@link Override} on the method {@link HashMap#put(Object, Object)},
     * this override is used to first declare a gate change to the {@link Log}, and secondly,
     * assuming that there is a task waiting on the gate to open, execute the task, otherwise it finishes putting the value in the map.
     */
    private static HashMap<String, Boolean> mGates = new HashMap<String, Boolean>() {
        @Override
        public Boolean put(String key, Boolean value) {
            Boolean o = super.put(key, value);
            //Output the state change for which field.
            Log.i(">>FLOW_CONTROLLER<<", "Gate State Changed for: " + getFieldName(key) + " set to " + value.toString());
            if (mTaskOnGateOpen.containsKey(key)) {
                // Execute a task for the gate, if there is one.
                mTaskOnGateOpen.remove(key).onFinish();
                return o;
            }
            if (mTaskOnGateOpenStateStated.containsKey(key)) {
                if (mTaskOnGateOpenStateStated.get(key).mState == value)
                    mTaskOnGateOpenStateStated.remove(key).mTask.onFinish();
                return o;
            }
            return o;
        }
    };


    /**
     * Check if a certain gate is open.
     *
     * @param key
     *         - the gate key value.
     *
     * @return false if either the key doesn't exists in {@link #mGates}, otherwise it returns the value of the key.
     */
    public static Boolean getIsGateOpen(@NonNull String key) {
        return mGates.containsKey(key) ? mGates.get(key) : false;
    }

    /**
     * Set the value of a gate.
     *
     * @param key
     *         - the gate key value.
     * @param value
     *         - the gate value.
     */
    public static void setGate(@NonNull String key, @NonNull Boolean value) {
        if (!mGates.containsKey(key) || mGates.get(key) != value)
            mGates.put(key, value);
    }

    /**
     * Add a task to {@link #mTaskOnGateOpen}, to be executed on gate state change.
     *
     * @param key
     *         - the gate key value.
     * @param task
     *         - the task to be executed.
     */
    public static void addGateOpenListener(@NonNull String key, @NonNull FinishableTask task) {
        if (mGates.containsKey(key))
            task.onFinish();
        else if (!mTaskOnGateOpen.containsKey(key))
            mTaskOnGateOpen.put(key, task);
        else
            Log.i(">>FLOW_CONTROLLER<<", "Tried to set a finishable task when one exists.");
    }

    /**
     * Add a task to {@link #mTaskOnGateOpenStateStated}, to be executed on gate state change, but on specific state.
     *
     * @param key
     *         - the gate key value.
     * @param task
     *         - the task to be executed.
     * @param state
     *         - the state on which the task is to be executed.
     */
    public static void addGateOpenListener(@NonNull String key, @NonNull FinishableTask task, boolean state) {
        if (mGates.containsKey(key) && mGates.get(key) == state)
            task.onFinish();
        else if (!mTaskOnGateOpen.containsKey(key))
            mTaskOnGateOpenStateStated.put(key, new TaskStateContainer(task, state));
    }


    /**
     * The getFieldName method is a method used to find the name of a field from {@link alon.com.shifter.base_classes.Consts.Fc_Keys} class, according to the value.
     * Using Reflection, since all fields in {@link Consts} are <b>public</b>, <b>static</b> and <b>final</b> we can find the name of the field according to the value.
     *
     * @param fieldVal
     *         - the value of the field.
     *
     * @return - the field name, or a different string otherwise.
     */
    private static String getFieldName(String fieldVal) {
        Field[] mFields = Consts.Fc_Keys.class.getFields();
        for (Field mField : mFields) {
            try {
                Object o = mField.get(null);
                if (mField.get(null) != null)
                    if (o.toString().equals(fieldVal))
                        return mField.getName();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ">>!!!FIELD NAME NOT FOUND!!!<<";
    }

    /**
     * A container class that has a {@link FinishableTask} and a boolean value, representing on what gate state should the task be run.
     */
    private static class TaskStateContainer {
        FinishableTask mTask;
        boolean mState;

        TaskStateContainer(FinishableTask task, boolean state) {
            mTask = task;
            mState = state;
        }
    }
}
