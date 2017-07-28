package alon.com.shifter.utils_database;


/**
 * Created by Alon on 2/2/2017.
 */

abstract class Table {

    static Column[] columns;
    static String name;

    protected static String genCreateCommand() {
        assert columns != null && name != null;

        String command = "CREATE TABLE " + name + "(";
        for (Column column : columns)
            command += column.genCreateCommandSubPortion();
        command = command.substring(0, command.lastIndexOf(','));
        command += ")";
        return command;
    }

    protected static String getDropCommand() {
        assert name != null;
        return "DROP TABLE IF EXISTS " + name;
    }
}
