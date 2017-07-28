package alon.com.shifter.utils_database;

enum DataType {

    INTEGER, REAL, BLOB, TEXT;

    @Override
    public String toString() {
        switch (this) {
            case INTEGER:
                return "INTEGER";
            case REAL:
                return "REAL";
            case BLOB:
                return "BLOB";
            case TEXT:
                return "TEXT";
            default:
                return null;
        }
    }

}
