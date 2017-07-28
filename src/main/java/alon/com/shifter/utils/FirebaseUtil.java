package alon.com.shifter.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import alon.com.shifter.base_classes.BaseUser;
import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.FinishableTask;
import alon.com.shifter.base_classes.FinishableTaskWithParams;
import alon.com.shifter.base_classes.TaskResult;

import static alon.com.shifter.utils.FlowController.setGate;

/**
 * The FirebaseUtil class is used to run methods the have anything to do with the Firebase Database, or the Firebase Authentication.
 */
@SuppressWarnings("unchecked")
public class FirebaseUtil implements Consts {

    private static final String TAG = "Shifter_firebaseUtils";

    /**
     * A {@link FirebaseAuth} instance.
     */
    private static FirebaseAuth mBlazeAuth;

    /**
     * A {@link BaseUser} instance.
     */
    private static BaseUser mUser = new BaseUser();


    /**
     * Get an {@link DatabaseReference} according to a {@link alon.com.shifter.base_classes.Consts.Fb_Dirs} key.
     *
     * @param dir - the directory.
     * @return a {@link DatabaseReference} to the dir.
     */
    public static DatabaseReference getDatabase(String dir) {
        return FirebaseDatabase.getInstance().getReference(dir);
    }

    /**
     * Singleton method to get {@link #mBlazeAuth}.
     *
     * @return {@link #mBlazeAuth}.
     */
    public static FirebaseAuth getFirebaseAuth() {
        return (mBlazeAuth == null ? (mBlazeAuth = FirebaseAuth.getInstance()) : mBlazeAuth);
    }

    /**
     * Get the instance of the {@link BaseUser}.
     *
     * @return {@link #mUser}.
     */
    public static BaseUser getUser() {
        return (mUser == null ? mUser = new BaseUser() : mUser);
    }

    /**
     * Sets the {@link BaseUser} for the entire application.
     *
     * @param user - the {@link BaseUser}.
     */
    public static void setUser(BaseUser user) {
        if (user != null)
            mUser = user;
    }

    /**
     * A method to log into shifter using email and password.
     *
     * @param email    - the user's email.
     * @param password - the user's password.
     * @param caller   - the activity that is calling the method.
     * @param result   - the Task that is to be executed on login success or fail (with regards to success or failure).
     * @see FirebaseAuth#signInWithEmailAndPassword(String, String).
     */
    public static void login(String email, String password, Activity caller, final TaskResult result) {
        getFirebaseAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener(caller, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                    result.onSucceed();
                else
                    result.onFail();
            }
        });
    }

    /**
     * A method to register a user to shifter.
     *
     * @param email    - the user's email.
     * @param password - the user's password.
     * @param caller   - the activity that is calling the method.
     * @param result   - the Task that is to be executed on register success or fail (with regards to success or failure).
     * @see FirebaseAuth#createUserWithEmailAndPassword(String, String).
     */
    @SuppressWarnings("ConstantConditions")
    public static void register(String email, String password, Activity caller, final TaskResult result) {
        getFirebaseAuth().createUserWithEmailAndPassword(email, password).addOnCompleteListener(caller, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mUser.setUID(getFirebaseAuth().getCurrentUser().getUid());
                    result.onSucceed();
                } else
                    result.onFail();
            }
        });
    }

    /**
     * Get the short-hand-code of a workplace.
     * This function is usually executed as part of {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_LOGIN} or
     * {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_REGISTER},
     * the rest of the time it should already be stored in {@link #mUser}.
     *
     * @param task - the Task that is to be executed when the data pull is finished.
     */
    public static void getShorthandCode(final FinishableTask task) {
        if (!mUser.getUID().equals(Strings.NULL) && mUser.getSHCode().equals(Strings.NULL))
            getDatabase(Fb_Dirs.USERS).child(mUser.getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                        if (mSnap.getKey().equals(Fb_Keys.WORKPLACE_SH_CODE)) {
                            mUser.setSHCode(mSnap.getValue().toString());
                            task.onFinish();
                            return;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled: Failed.", databaseError.toException());
                }
            });
        else
            task.onFinish();
    }

    /**
     * Checks if a given workplace code exists in the database.
     * Only executed on {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_REGISTER}.
     *
     * @param workplaceCode - the workplace.
     * @param taskResult    - the task to be executed on succeed or failure.
     */
    public static void verifyCode(final String workplaceCode, final TaskResult taskResult) {
        getDatabase(Fb_Dirs.WORKPLACES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot correctSnap = null;
                for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                    if (mSnap.getKey().equals(workplaceCode)) {
                        correctSnap = mSnap;
                        break;
                    }
                }

                if (correctSnap != null) {
                    for (DataSnapshot mSnap : correctSnap.getChildren()) {
                        if (mSnap.getKey().equals(Fb_Keys.WORKPLACE_SH_CODE)) {
                            mUser.setSHCode(mSnap.getValue().toString());
                            taskResult.onSucceed();
                            return;
                        }
                    }
                }
                taskResult.onFail();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: failed", databaseError.toException());
                taskResult.onFail();
            }
        });
    }

    /**
     * Gets a {@link BaseUser} attribute {@link HashMap} from the database, assuming one exists,
     * and then calls the task passed in with regards to success or failure.
     * Usually called with {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_REGISTER}
     * and {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_LOGIN}.
     *
     * @param task - the task to execute once it finished.
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public static void getUserFromDatabase(final TaskResult task) {
        getDatabase(Fb_Dirs.USERS).child(getFirebaseAuth().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                    if (mSnap.getKey().equals(Fb_Keys.USER_BASE_USER_OBJECT)) {
                        mUser = BaseUser.construct((HashMap<String, Object>) mSnap.getValue());
                        task.onSucceed();
                        return;
                    }
                }
                task.onFail();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: failed", databaseError.toException());
                task.onFail();
            }
        });
    }

    /**
     * Add a request to approve a user to the manager.
     * Only executed on {@link alon.com.shifter.base_classes.Consts.Linker_Keys#TYPE_REGISTER}.
     *
     * @param user - the user that needs to have his request inserted.
     */
    public static void insertRequest(BaseUser user) {
        getDatabase(Fb_Dirs.MGR_SEC_USER_REQUESTS).child(user.getCode()).child(user.getUID()).setValue(false);
        setGate(Fc_Keys.USER_ACCEPTED, false);
    }

    /**
     * Gets all users (as a {@link ArrayList} of type {@link BaseUser}) according to a {@link Set} of UID's.
     *
     * @param uids - the {@link Set} of all uids that need to be found.
     * @param task - the task that needs to be executed once the method finishes finding out what it needs.
     */
    public static void getUsersByUID(final Set<String> uids, final FinishableTaskWithParams task) {
        getDatabase(Fb_Dirs.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<BaseUser> mBaseUsers = new ArrayList<>();
                for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                    if (uids.contains(mSnap.getKey())) {
                        for (DataSnapshot mChild : mSnap.getChildren()) {
                            if (mChild.getKey().equals(Fb_Keys.USER_BASE_USER_OBJECT))
                                mBaseUsers.add(BaseUser.construct((HashMap<String, Object>) mChild.getValue()));
                        }
                    }
                }
                task.addParamToTask(Param_Keys.KEY_BASE_USER_OBJECT_LIST, mBaseUsers);
                task.onFinish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: failed", databaseError.toException());
                task.onFinish();
            }
        });
    }

    /**
     * Find a {@link BaseUser} according to it's UID.
     *
     * @param uid  - the user's UID.
     * @param task - the task to be executed once the method finds the user, or doesn't.
     */
    @Deprecated
    public static void getUserByUID(final String uid, final FinishableTaskWithParams task) {
/*        getDatabase(Fb_Dirs.USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onDataChange(DataSnapshot dataSnapshot) {
                //PROBLEM - when there are many users running over all the users will be time consuming.
                for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                    if (mSnap.getKey().equals(Fb_Keys.USER_BASE_USER_OBJECT)) {
                        BaseUser user = BaseUser.construct((HashMap<String, Object>) mSnap.getValue());

                        task.addParamToTask(Param_Keys.KEY_BASE_USER_OBJECT, user);
                        task.onFinish();
                        return;
                    }
                }
                task.onFinish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: failed", databaseError.toException());
                task.onFinish();
            }
        });*/
        throw new UnsupportedOperationException("This function is deprecated.");

    }

    /**
     * Find all {@link BaseUser}s for a manager, that is not using the manager's {@link BaseUser} object,
     * But instead find the {@link BaseUser#getCode()} to find the Manager info in the {@link alon.com.shifter.base_classes.Consts.Fb_Dirs#MGR_SEC},
     * and in turn get {@link alon.com.shifter.base_classes.Consts.Fb_Keys#MGR_SEC_USERS_ACCEPTED} to get all the uids of the users accepted by the manager.
     *
     * @param task
     */

    public static void getUsersUIDsForManager(final FinishableTaskWithParams task) {
        getDatabase(Fb_Dirs.MGR_SEC).child(getUser().getCode()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> userStrings = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    if (snap.getKey().equals(Fb_Keys.MGR_SEC_USERS_ACCEPTED)) {
                        String value = (String) snap.getValue();
                        userStrings.addAll(Arrays.asList(value.split("~")));
                        break;
                    }
                }
                task.addParamToTask(Param_Keys.KEY_BASE_USER_STRING_LIST, userStrings);
                task.onFinish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: failed", databaseError.toException());
                task.onFinish();
            }
        });
    }

    /**
     * Change a user that is currently waiting for approval to 'accepted' state.
     *
     * @param acceptedUser - the user that needs to be changed.
     */
    public static void acceptedUser(final BaseUser acceptedUser) {
        getDatabase(Fb_Dirs.MGR_SEC_USER_REQUESTS).child(getUser().getCode()).child(acceptedUser.getUID()).setValue(true);
        final FinishableTaskWithParams mTask = new FinishableTaskWithParams() {
            @Override
            public void onFinish() {
                String str = getParamsFromTask().get(Param_Keys.KEY_ACCEPTED_USERS_STRING).toString();
                String mAcceptedUsers = str.isEmpty() ? acceptedUser.getUID() : str + "~" + acceptedUser.getUID();
                getDatabase(Fb_Dirs.MGR_SEC).child(getUser().getCode()).child(Fb_Keys.MGR_SEC_USERS_ACCEPTED).setValue(mAcceptedUsers);
            }
        };
        getDatabase(Fb_Dirs.MGR_SEC).child(getUser().getCode()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                    if (mSnap.getKey().equals(Fb_Keys.MGR_SEC_USERS_ACCEPTED)) {
                        mTask.addParamToTask(Param_Keys.KEY_ACCEPTED_USERS_STRING, mSnap.getValue());
                        mTask.onFinish();
                        return;
                    }
                }
                mTask.addParamToTask(Param_Keys.KEY_ACCEPTED_USERS_STRING, "");
                mTask.onFinish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: couldn't accept user.", databaseError.toException());
            }
        });
    }

    /**
     * Decline a user's approval request.
     * This assigns the {@link Strings#VALUE_DELETE_ACCOUNT}
     * to the database reference of the user in {@link alon.com.shifter.base_classes.Consts.Fb_Dirs#MGR_SEC_USER_REQUESTS} path.
     *
     * @param rejectedUser - the rejected user.
     */
    public static void declineUser(BaseUser rejectedUser) {
        getDatabase(Fb_Dirs.MGR_SEC_USER_REQUESTS).child(getUser().getCode()).child(rejectedUser.getUID()).setValue(Strings.VALUE_DELETE_ACCOUNT);
    }

    /**
     * Check whether the current user that is using this application is approved, or not.
     *
     * @param task - the task to be executed once a result was found.
     */
    public static void amIApproved(final FinishableTaskWithParams task) {
        getDatabase(Fb_Dirs.MGR_SEC).child(getUser().getCode()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean state = false;
                for (DataSnapshot mSnap : dataSnapshot.getChildren())
                    if (mSnap.getKey().equals(Fb_Keys.MGR_SEC_USERS_ACCEPTED)) {
                        String mString = (String) mSnap.getValue();
                        if (mString.contains("~")) {
                            for (String user : mString.split("~"))
                                if (user.equals(getUser().getUID())) {
                                    state = true;
                                    break;
                                }
                        } else if (mString.equals(getUser().getUID()))
                            state = true;
                    }
                task.addParamToTask(Param_Keys.KEY_APPROVED_STATE, state);
                task.onFinish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: failed.", databaseError.toException());
                task.addParamToTask(Param_Keys.KEY_APPROVED_STATE, false);
                task.onFinish();
            }
        });
    }

    /**
     * Checks if a user is marked as deleted from the database.
     *
     * @param task - the task to be executed once the method finds the result.
     */
    public static void amIDeleted(final FinishableTaskWithParams task) {
        getDatabase(Fb_Dirs.MGR_SEC_USER_REQUESTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot mSnap : dataSnapshot.getChildren()) {
                    if (mSnap.getKey().equals(getUser().getUID())) {
                        if (mSnap.getValue().equals(Strings.VALUE_DELETE_ACCOUNT))
                            task.addParamToTask(Param_Keys.KEY_DELETED_ACCOUNT, true);
                        else
                            task.addParamToTask(Param_Keys.KEY_DELETED_ACCOUNT, false);
                        task.onFinish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: failed.", databaseError.toException());
                task.addParamToTask(Param_Keys.KEY_APPROVED_STATE, false);
                task.onFinish();
            }
        });
    }
}
