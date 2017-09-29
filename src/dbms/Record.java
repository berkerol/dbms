package dbms;

public class Record {

    static int numberOfFields;
    static String[] names;
    static int[] types;
    static int[] lengths;

    static void numberOfFields(int numberOfFields) {
        Record.numberOfFields = numberOfFields;
    }

    static void names(int fieldNameLength, String names) {
        int cursor = 0;
        Record.names = new String[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            Record.names[i] = DBMS.toData(names.substring(cursor, cursor + fieldNameLength));
            cursor += fieldNameLength;
        }
    }

    static void types(int fieldTypeLength, String types) {
        int cursor = 0;
        Record.types = new int[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            Record.types[i] = Integer.parseInt(DBMS.toData(types.substring(cursor, cursor + fieldTypeLength)));
            cursor += fieldTypeLength;
        }
    }

    static void lengths(int fieldLengthLength, String lengths) {
        int cursor = 0;
        Record.lengths = new int[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            Record.lengths[i] = Integer.parseInt(DBMS.toData(lengths.substring(cursor, cursor + fieldLengthLength)));
            cursor += fieldLengthLength;
        }
    }

    String[] fields;

    Record(String record) {
        int cursor = 0;
        this.fields = new String[numberOfFields];
        for (int i = 0; i < numberOfFields; i++) {
            this.fields[i] = DBMS.toData(record.substring(cursor, cursor + lengths[i]));
            cursor += lengths[i];
        }
    }

    void printFields() {
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            System.out.print((i + 1) + ".field value: ");
            switch (types[i]) {
                case 0:
                    System.out.print(Integer.parseInt(field));
                    break;
                case 1:
                    System.out.print(Double.parseDouble(field));
                    break;
                default:
                    System.out.print(field);
                    break;
            }
            System.out.print("\t");
        }
        System.out.println();
    }

    int compareTo(Record r) {
        return compareTo(r.fields[0]);
    }

    int compareTo(String s) {
        return compareTo(0, s);
    }

    int compareTo(int index, String s) {
        switch (types[index]) {
            case 0:
                return (Integer.valueOf(this.fields[index])).compareTo((Integer.valueOf(s)));
            case 1:
                return (Double.valueOf(this.fields[index])).compareTo((Double.valueOf(s)));
            default:
                return this.fields[index].compareTo(s);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < numberOfFields; i++) {
            String field = fields[i];
            for (int j = 0; j < lengths[i] - field.length(); j++) {
                s.append(" ");
            }
            s.append(field);
        }
        return s.toString();
    }

}
