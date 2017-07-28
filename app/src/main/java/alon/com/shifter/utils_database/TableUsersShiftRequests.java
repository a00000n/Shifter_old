package alon.com.shifter.utils_database;

/**
 * Created by Alon on 2/2/2017.
 */

public final class TableUsersShiftRequests extends Table {

    public static final String commentBreakString = "~~~";

    static {
        columns = new Column[]{
                new Column("UserRequestID", DataType.INTEGER, true, true, true),
                new Column("UserName", DataType.TEXT, false, false, true),
                new Column("FirstSundayDate", DataType.TEXT, false, false, true),
                new Column("UserRequests_Sunday", DataType.TEXT, false, false, true),
                new Column("UserRequests_Monday", DataType.TEXT, false, false, true),
                new Column("UserRequests_Tuesday", DataType.TEXT, false, false, true),
                new Column("UserRequests_Wednesday", DataType.TEXT, false, false, true),
                new Column("UserRequests_Thursday", DataType.TEXT, false, false, true),
                new Column("UserRequests_Friday", DataType.TEXT, false, false, true),
                new Column("UserRequests_Saturday", DataType.TEXT, false, false, true)
        };
        name = "UsersShiftRequests";
    }

    public static void init() {

    }


}
