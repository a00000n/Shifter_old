package alon.com.shifter.utils_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import alon.com.shifter.base_classes.Consts;
import alon.com.shifter.base_classes.Linker;
import alon.com.shifter.utils.Util;

public class DatabaseUtil {

    private static final String DB_NAME = "Shifter_Db";
    private static final int DB_VERSION = 3;

    private static SQLiteDatabase mDB;
    private static DatabaseUtilHelper mDBHelper;

    private static boolean canWrite = false;

    public static int start(Context context) {
        if (mDBHelper != null)
            return Consts.Ints.DB_ERR_ALREADY_STARTED;
        mDBHelper = new DatabaseUtilHelper(context);
        return Consts.Ints.DB_RESULT_OKAY;
    }

    public static int stop() {
        if (mDB == null)
            if (mDBHelper == null)
                return Consts.Ints.DB_RESULT_OKAY;
            else {
                mDBHelper.close();
                mDBHelper = null;
                return Consts.Ints.DB_RESULT_OKAY;
            }
        else {
            mDB.close();
            mDB = null;
            return stop();
        }
    }

    public static int openRead() {
        if (mDBHelper == null)
            return Consts.Ints.DB_ERR_HELPER_NULL;
        else if (mDB != null)
            return Consts.Ints.DB_ERR_DATABASE_EXISTS;

        mDB = mDBHelper.getReadableDatabase();
        canWrite = false;
        return Consts.Ints.DB_RESULT_OKAY;
    }

    public static int openWrite() {
        if (mDBHelper == null)
            return Consts.Ints.DB_ERR_HELPER_NULL;
        else if (mDB != null)
            return Consts.Ints.DB_ERR_DATABASE_EXISTS;

        mDB = mDBHelper.getWritableDatabase();
        canWrite = true;
        return Consts.Ints.DB_RESULT_OKAY;
    }

    public static int addUserRequests(Context context, JSONObject... userRequest) {
        //PROBLEM - Might occur duplicate data pull.
        TableUsersShiftRequests.init();
        Column[] columns = TableUsersShiftRequests.columns;
        for (JSONObject request : userRequest) {
            try {
                ContentValues values = new ContentValues();
                values.put(columns[1].getName(), request.getString(Consts.Strings.VALUE_UPLOAD_SHIFT_JSON_USER_NAME_KEY));
                values.put(columns[2].getName(), request.getString(Consts.Strings.VALUE_UPLOAD_SHIFT_RQS_NEXT_SUNDAY));
                for (int i = 1; i <= 7; i++)
                    values.put(columns[2 + i].getName(), genRequestForDay(context, request, i));
                mDB.insert(TableUsersShiftRequests.name, null, values);
            } catch (JSONException e) {
                e.printStackTrace();
                return Consts.Ints.DB_INJECTION_JSON_PARSE_ERROR;
            }
        }
        return Consts.Ints.DB_RESULT_OKAY;
    }

    public static Cursor getAllUserRequests(String userName) {
        return getAllUserRequests(userName, Linker.determineClosestSunday());
    }

    public static Cursor getAllUserRequests(String userName, String sundayDate) {
        TableUsersShiftRequests.init();
        Column[] columns = TableUsersShiftRequests.columns;
        return mDB.query(TableUsersShiftRequests.name,
                new String[]{columns[3].getName(), columns[4].getName(), columns[5].getName(), columns[6].getName(), columns[7].getName(), columns[8].getName(), columns[9].getName()},
                columns[1].getName() + "=? AND " + columns[2].getName() + "=?",
                new String[]{userName, sundayDate}, null, null, null);
    }

    public static Cursor getAllUsersRequestForDate(String sundayDate) {
        TableUsersShiftRequests.init();
        Column[] columns = TableUsersShiftRequests.columns;
        return mDB.query(TableUsersShiftRequests.name,
                new String[]{columns[1].getName(), columns[3].getName(), columns[4].getName(), columns[5].getName(), columns[6].getName(), columns[7].getName(), columns[8].getName(), columns[9].getName()},
                columns[2].getName() + "=?",
                new String[]{sundayDate}, null, null, null);
    }

    private static String genRequestForDay(Context context, JSONObject request, int day) throws JSONException {
        String requests = "";
        String dayString = Util.getInstance(context).getDayString(context, day);
        JSONArray valueArray = request.getJSONArray(dayString);
        int shiftCounter = 0;
        for (int i = 0; i < valueArray.length(); i++) {
            JSONObject jsonRqsObject = valueArray.getJSONObject(i);
            String shiftTitle = (String) jsonRqsObject.get(Consts.Strings.VALUE_UPLOAD_SHIFT_JSON_KEY);
            int shiftTime = reverseShiftTitle(context, shiftTitle);
            if (shiftCounter != shiftTime) {
                for (int j = 0; j < shiftTime - shiftCounter; j++)
                    requests += (i != 0 || j != 0 ? ":" : "") + "-1";
                shiftCounter = shiftTime;
            } else
                shiftCounter++;
//            if (i == 0 && !requests.isEmpty())
//                requests += ":1";
//            else if (i == 0 && requests.isEmpty())
//                requests += "1";
//            else if (i != 0)
//                requests += ":1";
            requests += (i != 0 || !requests.isEmpty() ? ":1" : "1");
            if (jsonRqsObject.has(Consts.Strings.VALUE_UPLOAD_SHIFT_COMMENT_JSON_KEY))
                requests += TableUsersShiftRequests.commentBreakString + jsonRqsObject.getString(Consts.Strings.VALUE_UPLOAD_SHIFT_COMMENT_JSON_KEY);
        }
        //Is the last shift submitted night, evening?
        if (valueArray.length() != 0) {
            if (!((JSONObject) valueArray.get(valueArray.length() - 1)).getString(Consts.Strings.VALUE_UPLOAD_SHIFT_JSON_KEY).equals(Util.getInstance(context).getShiftTitle(context, 3))) {
                JSONObject rqsObj = valueArray.getJSONObject(valueArray.length() - 1);
                int shiftTime = reverseShiftTitle(context, rqsObj.getString(Consts.Strings.VALUE_UPLOAD_SHIFT_JSON_KEY));
                for (int i = 0; i < 3 - shiftTime; i++)
                    requests += ":-1";
            }
        } else
            for (int i = 0; i < 4; i++)
                requests += (i != 0 ? ":" : "") + "-1";

        return requests;
    }

    private static int reverseShiftTitle(Context context, String shiftTitle) {
        Util util = Util.getInstance(context);
        for (int i = 0; i < 4; i++) {
            if (shiftTitle.equals(util.getShiftTitle(context, i)))
                return i;
        }
        return -1;
    }

    private static class DatabaseUtilHelper extends SQLiteOpenHelper {

        DatabaseUtilHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            TableUsersShiftRequests.init();
            db.execSQL(TableUsersShiftRequests.genCreateCommand());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            TableUsersShiftRequests.init();
            db.execSQL(TableUsersShiftRequests.getDropCommand());
            Log.i(">>DatabaseUtil<<", "onUpgrade: Dropping table.");
            onCreate(db);
        }
    }
}
