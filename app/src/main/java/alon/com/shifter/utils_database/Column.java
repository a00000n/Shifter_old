package alon.com.shifter.utils_database;

/**
 * Created by Alon on 2/2/2017.
 */
class Column {

    private String mName;
    private DataType mType;
    private boolean mIsPKey;
    private boolean mIsAIncrement;
    private boolean mIsNonNull;
    private String name;

    public Column(String name, DataType type, boolean is_p_key, boolean is_a_inc, boolean is_n_null) {
        mName = name;
        mType = type;
        mIsPKey = is_p_key;
        mIsAIncrement = is_a_inc;
        mIsNonNull = is_n_null;
    }


    public String genCreateCommandSubPortion() {
        return mName + " " + mType.toString() + " " + (mIsPKey ? "PRIMARY KEY " : "") +
                (mIsAIncrement ? "AUTOINCREMENT " : "") + (mIsNonNull ? "NOT NULL" : "") + ", ";
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }
}
