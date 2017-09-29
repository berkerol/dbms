package dbms;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;

public class DBMS {

    /**
     * Input of CLI.
     */
    private static final Scanner CONSOLE = new Scanner(System.in);
    /**
     * Extension of all data files.
     */
    private static final String EXTENSION = ".txt";
    /**
     * Size of a field name in bytes.
     */
    private static final int FIELD_NAME_LENGTH = 20;
    /**
     * Size of a field type in bytes.
     */
    private static final int FIELD_TYPE_LENGTH = 1;
    /**
     * Size of a field length in bytes.
     */
    private static final int FIELD_LENGTH_LENGTH = 2;
    /**
     * Size of the string containing number of fields in a record in bytes.
     */
    private static final int NUMBER_OF_FIELDS_LENGTH = 2;
    /**
     * System catalog file name.
     */
    private static final String SYSTEM_CATALOG = "SystemCatalog";
    /**
     * Size of a table name in bytes.
     */
    private static final int TABLE_NAME_LENGTH = 30;

    public static void main(String[] args) throws IOException {
        if (!new File(SYSTEM_CATALOG + EXTENSION).exists()) {
            writeFile(SYSTEM_CATALOG, new ArrayList<>());
            System.out.println("System Catalog is created.");
        }
        while (true) {
            ArrayList<String> file = readFile(SYSTEM_CATALOG);
            System.out.println("1 - DDL Operations.");
            System.out.println("2 - DML Operations.");
            System.out.println("Enter any other integer to exit.");
            switch (Integer.parseInt(CONSOLE.nextLine())) {
                case 1:
                    System.out.println("1 - Create a Table.");
                    System.out.println("2 - Delete a Table.");
                    System.out.println("3 - Update a Table.");
                    System.out.println("4 - Search for a Table.");
                    System.out.println("5 - List all Tables.");
                    System.out.println("Enter any other integer to go back.");
                    switch (Integer.parseInt(CONSOLE.nextLine())) {
                        case 1:
                            System.out.println("Enter the table name.");
                            String tableName = CONSOLE.nextLine();
                            createTable(file, tableName, getTableProperties(tableName));
                            System.out.println("Table is created.");
                            break;
                        case 2:
                            System.out.println("Enter the table name.");
                            deleteTable(file, CONSOLE.nextLine());
                            System.out.println("Table is deleted.");
                            break;
                        case 3:
                            System.out.println("Enter the table name.");
                            tableName = CONSOLE.nextLine();
                            updateTable(file, tableName, getTableProperties(tableName));
                            System.out.println("Table is updated.");
                            break;
                        case 4:
                            System.out.println("Enter searched value.");
                            String value = CONSOLE.nextLine();
                            System.out.println("Enter search operator (<, >, =).");
                            openSystemCatalog(file, value, 6, CONSOLE.nextLine());
                            System.out.println("Tables are searched.");
                            break;
                        case 5:
                            openSystemCatalog(file, "", 7, "");
                            System.out.println("Tables are listed.");
                            break;
                        default:
                            break;
                    }
                    break;
                case 2:
                    System.out.println("1 - Create a Record.");
                    System.out.println("2 - Delete a Record.");
                    System.out.println("3 - Update a Record.");
                    System.out.println("4 - Search for a Record.");
                    System.out.println("5 - List all Records.");
                    System.out.println("Enter any other integer to go back.");
                    int operation = Integer.parseInt(CONSOLE.nextLine());
                    if (operation < 1 || operation > 5) {
                        break;
                    }
                    System.out.println("Enter the table name.");
                    openSystemCatalog(file, CONSOLE.nextLine(), operation, "");
                    break;
                default:
                    return;
            }
        }
    }

    private static void createRecord(ArrayList<Record> file, String tableName, Record newRecord) throws IOException {
        ListIterator<Record> iterator = file.listIterator();
        boolean inserted = false;
        for (int i = 0; i < file.size(); i++) {
            Record r = iterator.next();
            if (newRecord.compareTo(r) < 0) {
                file.add(i, newRecord);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            file.add(newRecord);
        }
        writeTable(tableName, file);
    }

    private static void createTable(ArrayList<String> file, String tableName, String newRecord) throws IOException {
        ListIterator<String> iterator = file.listIterator();
        boolean inserted = false;
        for (int i = 0; i < file.size(); i++) {
            String s = iterator.next();
            if (tableName.compareTo(s) < 0) {
                file.add(i, newRecord);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            file.add(newRecord);
        }
        writeFile(SYSTEM_CATALOG, file);
        writeFile(tableName, new ArrayList<>());
    }

    private static void deleteRecord(ArrayList<Record> file, String tableName, String keyField) throws IOException {
        ListIterator<Record> iterator = file.listIterator();
        for (int i = 0; i < file.size(); i++) {
            Record r = iterator.next();
            if (r.compareTo(keyField) == 0) {
                file.remove(i);
                break;
            }
        }
        writeTable(tableName, file);
    }

    private static void deleteTable(ArrayList<String> file, String tableName) throws IOException {
        ListIterator<String> iterator = file.listIterator();
        for (int i = 0; i < file.size(); i++) {
            String s = iterator.next();
            if (tableName.equals(toData(s.substring(0, TABLE_NAME_LENGTH)))) {
                file.remove(i);
                break;
            }
        }
        writeFile(SYSTEM_CATALOG, file);
        writeFile(tableName, new ArrayList<>());
    }

    private static String getTableProperties(String tableName) {
        System.out.println("Enter the number of fields.");
        int numberOfFields = Integer.parseInt(CONSOLE.nextLine());
        StringBuilder sb = new StringBuilder().append(toString(tableName, TABLE_NAME_LENGTH)).append(toString(("" + numberOfFields), NUMBER_OF_FIELDS_LENGTH));
        for (int i = 1; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field name.");
            sb.append(toString(CONSOLE.nextLine(), FIELD_NAME_LENGTH));
        }
        for (int i = 1; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field type.");
            sb.append(toString(CONSOLE.nextLine(), FIELD_TYPE_LENGTH));
        }
        for (int i = 1; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field length.");
            sb.append(toString(CONSOLE.nextLine(), FIELD_LENGTH_LENGTH));
        }
        return sb.toString();
    }

    /**
     * Executes DML operations (also used for searching and listing tables).
     * Finds the related system catalog record, opens the table file, takes the
     * required inputs from user then calls the related method.
     *
     * @param catalog list of the system catalog
     * @param tableName name of the table used in DML operations
     * @param operation operation type (create, delete, update, search, list)
     * @param operator search operator for searching tables
     * @throws IOException when table file cannot be read or written
     */
    private static void openSystemCatalog(ArrayList<String> catalog, String tableName, int operation, String operator) throws IOException {
        for (String s : catalog) {
            int cursor = 0;
            String name = toData(s.substring(cursor, cursor + TABLE_NAME_LENGTH));
            cursor += TABLE_NAME_LENGTH;
            int numberOfFields = Integer.parseInt(toData(s.substring(cursor, cursor + NUMBER_OF_FIELDS_LENGTH)));
            cursor += NUMBER_OF_FIELDS_LENGTH;
            Record.numberOfFields(numberOfFields);
            String names = s.substring(cursor, cursor + numberOfFields * FIELD_NAME_LENGTH);
            cursor += numberOfFields * FIELD_NAME_LENGTH;
            Record.names(FIELD_NAME_LENGTH, names);
            String types = s.substring(cursor, cursor + numberOfFields * FIELD_TYPE_LENGTH);
            cursor += numberOfFields * FIELD_TYPE_LENGTH;
            Record.types(FIELD_TYPE_LENGTH, types);
            String lengths = s.substring(cursor, cursor + numberOfFields * FIELD_LENGTH_LENGTH);
            Record.lengths(FIELD_LENGTH_LENGTH, lengths);
            if (operation == 6) {
                if (operator.equals("=") && name.compareTo(tableName) == 0) {
                    printFields(name, numberOfFields, names, types, lengths);
                    return;
                }
                else if (operator.equals(">") && name.compareTo(tableName) > 0) {
                    printFields(name, numberOfFields, names, types, lengths);
                }
                else if (operator.equals("<")) {
                    if (name.compareTo(tableName) < 0) {
                        printFields(name, numberOfFields, names, types, lengths);
                    }
                    else {
                        return;
                    }
                }
            }
            else if (operation == 7) {
                printFields(name, numberOfFields, names, types, lengths);
            }
            else if (tableName.equals(name)) {
                ArrayList<Record> file = readTable(tableName);
                String keyField = "";
                if (operation == 1 || operation == 2 || operation == 3) {
                    System.out.println("Enter key field.");
                    keyField = CONSOLE.nextLine();
                }
                StringBuilder sb = new StringBuilder().append(toString(keyField, Record.lengths[0]));
                if (operation == 1 || operation == 3) {
                    for (int i = 2; i <= numberOfFields; i++) {
                        System.out.println("Enter " + i + "'th field.");
                        sb.append(toString(CONSOLE.nextLine(), Record.lengths[i - 1]));
                    }
                }
                switch (operation) {
                    case 1:
                        createRecord(file, tableName, new Record(sb.toString()));
                        System.out.println("Record is created.");
                        return;
                    case 2:
                        deleteRecord(file, tableName, keyField);
                        System.out.println("Record is deleted.");
                        return;
                    case 3:
                        updateRecord(file, tableName, new Record(sb.toString()));
                        System.out.println("Record is updated.");
                        return;
                    case 4:
                        System.out.println("Enter searched field.");
                        int index = Integer.parseInt(CONSOLE.nextLine());
                        System.out.println("Enter searched value.");
                        String value = CONSOLE.nextLine();
                        System.out.println("Enter search operator (<, >, =).");
                        operator = CONSOLE.nextLine();
                        printFields(name, numberOfFields, names, types, lengths);
                        searchRecord(file, index - 1, value, operator);
                        System.out.println("Records are searched.");
                        return;
                    case 5:
                        printFields(name, numberOfFields, names, types, lengths);
                        searchRecord(file, 0, "", "L");
                        System.out.println("Records are listed.");
                        return;
                }
            }
        }
    }

    private static void printFields(String tableName, int numberOfFields, String names, String types, String lengths) {
        System.out.print(tableName + "\t" + numberOfFields + "\t");
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print((i + 1) + ".field name: " + toData(names.substring(i * FIELD_NAME_LENGTH, (i + 1) * FIELD_NAME_LENGTH)) + "\t");
            System.out.print("type: " + toData(types.substring(i * FIELD_TYPE_LENGTH, (i + 1) * FIELD_TYPE_LENGTH)) + "\t");
            System.out.print("length: " + toData(lengths.substring(i * FIELD_LENGTH_LENGTH, (i + 1) * FIELD_LENGTH_LENGTH)) + "\t");
        }
        System.out.println();
    }

    private static ArrayList<String> readFile(String fileName) throws IOException {
        return (ArrayList<String>) Files.readAllLines(Paths.get(fileName + EXTENSION));
    }

    private static ArrayList<Record> readTable(String fileName) throws IOException {
        ArrayList<String> file = readFile(fileName);
        ArrayList<Record> table = new ArrayList<>();
        file.stream().forEach((line) -> {
            table.add(new Record(line));
        });
        return table;
    }

    private static void searchRecord(ArrayList<Record> file, int index, String value, String operator) {
        for (Record record : file) {
            if (operator.equals("L")) {
                record.printFields();
            }
            else if (operator.equals("=") && record.compareTo(index, value) == 0) {
                record.printFields();
                return;
            }
            else if (operator.equals(">") && record.compareTo(index, value) > 0) {
                record.printFields();
            }
            else if (operator.equals("<")) {
                if (record.compareTo(index, value) < 0) {
                    record.printFields();
                }
                else {
                    return;
                }
            }
        }
    }

    /**
     * Converts fixed size strings to data by removing spaces from the
     * beginning.
     *
     * @param s string to be converted to data
     * @return data
     */
    static String toData(String s) {
        int i = 0;
        while (s.charAt(i) == ' ') {
            i++;
        }
        return s.substring(i);
    }

    /**
     * Converts data to fixed size strings by adding spaces to the beginning.
     *
     * @param data data to be converted to fixed size string
     * @param desiredLength length of fixed size string
     * @return fixed size string
     */
    private static String toString(String data, int desiredLength) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < desiredLength - data.length(); i++) {
            s.append(" ");
        }
        return s.append(data).toString();
    }

    private static void updateRecord(ArrayList<Record> file, String tableName, Record newRecord) throws IOException {
        ListIterator<Record> iterator = file.listIterator();
        for (int i = 0; i < file.size(); i++) {
            Record r = iterator.next();
            if (newRecord.compareTo(r) == 0) {
                file.set(i, newRecord);
                break;
            }
        }
        writeTable(tableName, file);
    }

    private static void updateTable(ArrayList<String> file, String tableName, String newRecord) throws IOException {
        ListIterator<String> iterator = file.listIterator();
        for (int i = 0; i < file.size(); i++) {
            String s = iterator.next();
            if (tableName.compareTo(s) == 0) {
                file.add(i, newRecord);
                break;
            }
        }
        writeFile(SYSTEM_CATALOG, file);
    }

    private static void writeFile(String fileName, ArrayList<String> file) throws IOException {
        Path path = Paths.get(fileName + EXTENSION);
        Files.write(path, file);
    }

    private static void writeTable(String fileName, ArrayList<Record> table) throws IOException {
        ArrayList<String> file = new ArrayList<>();
        table.stream().forEach((record) -> {
            file.add(record.toString());
        });
        writeFile(fileName, file);
    }

}
