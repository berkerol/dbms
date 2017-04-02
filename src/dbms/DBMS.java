package dbms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

public class DBMS {

    private static final Scanner CONSOLE = new Scanner(System.in);
    private static final String EXTENSION = ".txt";
    private static final int FIELD_DATA_LENGTH = 4;
    private static final int FIELD_MAX_NUMBER = 10;
    private static final int FIELD_NAME_LENGTH = 7;
    private static final int NUMBER_OF_FIELDS_LENGTH = 2;
    private static final String SC_FILENAME = "SystemCatalog";
    private static final int SC_NUMBER_OF_PAGES_LENGTH = 2;
    private static final int SC_NUMBER_OF_RECORDS_LENGTH = 2;
    private static final int SC_PAGE_LENGTH = 11;
    private static final int SC_PAGE_UNUSED_SPACE_LENGTH = 8;
    private static final int SYSTEM_NAME_LENGTH = 50;
    private static final int TYPE_NAME_LENGTH = 25;
    private static final int TYPE_NUMBER_OF_PAGES_LENGTH = 2;
    private static final int TYPE_NUMBER_OF_RECORDS_LENGTH = 2;
    private static final int TYPE_PAGE_LENGTH = 25;
    private static final int TYPE_PAGE_UNUSED_SPACE_LENGTH = 10;

    public static void main(String[] args) throws FileNotFoundException {
        while (true) {
            LinkedList<String> file = readFile(SC_FILENAME, true, 0, "system catalog ", SC_PAGE_UNUSED_SPACE_LENGTH,
                    SC_NUMBER_OF_PAGES_LENGTH, SC_NUMBER_OF_RECORDS_LENGTH);
            int numberOfPages = stringToData(file.get(0).substring(SYSTEM_NAME_LENGTH));
            System.out.println("1 - DDL Operations.");
            System.out.println("2 - DML Operations.");
            System.out.println("Enter any other integer to exit.");
            switch (Integer.parseInt(CONSOLE.nextLine())) {
                case 1:
                    System.out.println("1 - Create a Type.");
                    System.out.println("2 - Delete a Type.");
                    System.out.println("3 - List all Types.");
                    int operation = Integer.parseInt(CONSOLE.nextLine());
                    String typeName = "";
                    if (operation != 3) {
                        System.out.println("Enter the type name.");
                        typeName = CONSOLE.nextLine();
                    }
                    switch (operation) {
                        case 1:
                            createType(file, numberOfPages, typeName);
                            System.out.println("Type is created.");
                            break;
                        case 2:
                            deleteType(file, numberOfPages, typeName);
                            System.out.println("Type is deleted.");
                            break;
                        case 3:
                            systemCatalog(file, numberOfPages, "", 6);
                            break;
                    }
                    break;
                case 2:
                    System.out.println("1 - Create a Record.");
                    System.out.println("2 - Delete a Record.");
                    System.out.println("3 - Update a Record.");
                    System.out.println("4 - Search for a Record.");
                    System.out.println("5 - List all Records.");
                    operation = Integer.parseInt(CONSOLE.nextLine());
                    System.out.println("Enter the type name.");
                    systemCatalog(file, numberOfPages, CONSOLE.nextLine(), operation);
                    break;
                default:
                    return;
            }
        }
    }

    private static boolean createRecord(String typeName, LinkedList<String> file, int numberOfPages, int keyField, String newRecord) throws FileNotFoundException {
        ListIterator<String> iterator = file.listIterator();
        boolean inserted = false, newPage = false;
        String oldRecord = "";
        int records = 0;
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String currentRecord = iterator.next();
                if (inserted) {
                    iterator.set(oldRecord);
                    oldRecord = currentRecord;
                }
                else if (keyField < stringToData(currentRecord.substring(0, FIELD_DATA_LENGTH))) {
                    iterator.set(newRecord);
                    oldRecord = currentRecord;
                    inserted = true;
                }
            }
            records = numberOfRecords;
        }
        if (inserted) {
            file.add(oldRecord);
        }
        else {
            file.add(newRecord);
        }
        records++;
        String lastPageHeader = file.get((numberOfPages - 1) * TYPE_PAGE_LENGTH);
        file.set((numberOfPages - 1) * TYPE_PAGE_LENGTH, lastPageHeader.substring(0, TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)
                + dataToString(records, TYPE_NUMBER_OF_RECORDS_LENGTH));
        if (records == TYPE_PAGE_LENGTH - 1) {
            file.add(nameToString("", TYPE_PAGE_UNUSED_SPACE_LENGTH) + dataToString(++numberOfPages, SC_NUMBER_OF_PAGES_LENGTH)
                    + dataToString(0, TYPE_NUMBER_OF_RECORDS_LENGTH));
            newPage = true;
        }
        writeFile(typeName, file);
        return newPage;
    }

    private static void createType(LinkedList<String> catalog, int numberOfPages, String typeName) throws FileNotFoundException {
        System.out.println("Enter the number of fields.");
        int numberOfFields = Integer.parseInt(CONSOLE.nextLine());
        String record = nameToString(typeName, TYPE_NAME_LENGTH) + dataToString(1, TYPE_NUMBER_OF_PAGES_LENGTH)
                + dataToString(numberOfFields, NUMBER_OF_FIELDS_LENGTH);
        for (int i = 1; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field name.");
            record += nameToString(CONSOLE.nextLine(), FIELD_NAME_LENGTH);
        }
        for (int i = numberOfFields; i < FIELD_MAX_NUMBER; i++) {
            record += nameToString("", FIELD_NAME_LENGTH);
        }
        catalog.add(record);
        int cursor = (numberOfPages - 1) * SC_PAGE_LENGTH + 1;
        String lastPageHeader = catalog.get(cursor);
        int numberOfRecords = stringToData(lastPageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH)) + 1;
        catalog.set(cursor, lastPageHeader.substring(0, SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH)
                + dataToString(numberOfRecords, SC_NUMBER_OF_RECORDS_LENGTH));
        if (numberOfRecords == SC_PAGE_LENGTH - 1) {
            catalog.add(nameToString("", SC_PAGE_UNUSED_SPACE_LENGTH) + dataToString(++numberOfPages, SC_NUMBER_OF_PAGES_LENGTH)
                    + dataToString(0, SC_NUMBER_OF_RECORDS_LENGTH));
            catalog.set(0, catalog.get(0).substring(0, SYSTEM_NAME_LENGTH) + dataToString(numberOfPages, SC_NUMBER_OF_PAGES_LENGTH));
        }
        writeFile(SC_FILENAME, catalog);
        LinkedList<String> file = new LinkedList<>();
        file.add(nameToString("", TYPE_PAGE_UNUSED_SPACE_LENGTH) + dataToString(1, TYPE_NUMBER_OF_PAGES_LENGTH)
                + dataToString(0, TYPE_NUMBER_OF_RECORDS_LENGTH));
        writeFile(typeName, file);
    }

    private static String dataToString(int data, int desiredLength) {
        int dataLength = ("" + data).length();
        String s = "";
        for (int i = 0; i < desiredLength - dataLength; i++) {
            s += "0";
        }
        return s + ("" + data);
    }

    private static boolean deleteRecord(String typeName, LinkedList<String> file, int numberOfPages, int keyField) throws FileNotFoundException {
        ListIterator<String> iterator = file.listIterator();
        boolean deleted = false, emptyPage = false;
        int records = 0;
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next(), record;
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            if (numberOfRecords > 0) {
                record = iterator.next();
                if (deleted) {
                    iterator.previous();
                    iterator.previous();
                    iterator.previous();
                    iterator.set(record);
                    iterator.next();
                    iterator.next();
                    iterator.next();
                }
                else if (keyField == stringToData(record.substring(0, FIELD_DATA_LENGTH))) {
                    deleted = true;
                }
            }
            for (int j = 1; j < numberOfRecords; j++) {
                record = iterator.next();
                if (deleted) {
                    iterator.previous();
                    iterator.previous();
                    iterator.set(record);
                    iterator.next();
                    iterator.next();
                }
                else if (keyField == stringToData(record.substring(0, FIELD_DATA_LENGTH))) {
                    deleted = true;
                }
            }
            records = numberOfRecords;
        }
        file.removeLast();
        records--;
        if (records == -1) {
            file.removeLast();
            numberOfPages--;
            records = TYPE_PAGE_LENGTH - 2;
            emptyPage = true;
        }
        String lastPageHeader = file.get((numberOfPages - 1) * TYPE_PAGE_LENGTH);
        file.set((numberOfPages - 1) * TYPE_PAGE_LENGTH, lastPageHeader.substring(0, TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)
                + dataToString(records, TYPE_NUMBER_OF_RECORDS_LENGTH));
        writeFile(typeName, file);
        return emptyPage;
    }

    private static void deleteType(LinkedList<String> catalog, int numberOfPages, String typeName) throws FileNotFoundException {
        ListIterator<String> iterator = catalog.listIterator();
        iterator.next();
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            int numberOfRecords = stringToData(pageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                    SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String record = iterator.next();
                if (typeName.equals(stringToName(record.substring(0, TYPE_NAME_LENGTH)))) {
                    LinkedList<String> file = new LinkedList<>();
                    file.add(nameToString("", TYPE_PAGE_UNUSED_SPACE_LENGTH) + dataToString(1, TYPE_NUMBER_OF_PAGES_LENGTH)
                            + dataToString(0, TYPE_NUMBER_OF_RECORDS_LENGTH));
                    writeFile(typeName, file);
                    iterator.set(nameToString("DELETED", TYPE_NAME_LENGTH) + record.substring(TYPE_NAME_LENGTH));
                    writeFile(SC_FILENAME, catalog);
                    return;
                }
            }
        }
    }

    private static String nameToString(String name, int desiredLength) {
        int nameLength = name.length();
        String s = "";
        for (int i = 0; i < desiredLength - nameLength; i++) {
            s += " ";
        }
        return s + name;
    }

    private static void printFieldValues(int numberOfFields, String record) {
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print(stringToData(record.substring(i * FIELD_DATA_LENGTH, (i + 1) * FIELD_DATA_LENGTH)) + "\t");
        }
        System.out.println();
    }

    private static LinkedList<String> readFile(String fileName, boolean catalog, int numberOfPages, String message, int pageUnusedSpaceLength,
            int numberOfPagesLength, int numberOfRecordsLength) throws FileNotFoundException {
        LinkedList<String> file = new LinkedList<>();
        Scanner read = new Scanner(new File(fileName + EXTENSION));
        if (catalog) {
            String catalogHeader = read.nextLine();
            file.add(catalogHeader);
            numberOfPages = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH));
        }
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = read.nextLine();
            file.add(pageHeader);
            System.out.println("Reading " + message + "page #"
                    + stringToData(pageHeader.substring(pageUnusedSpaceLength, pageUnusedSpaceLength + numberOfPagesLength)) + ".");
            int numberOfRecords = stringToData(pageHeader.substring(pageUnusedSpaceLength + numberOfPagesLength,
                    pageUnusedSpaceLength + numberOfPagesLength + numberOfRecordsLength));
            for (int j = 0; j < numberOfRecords; j++) {
                file.add(read.nextLine());
            }
        }
        read.close();
        return file;
    }

    private static void searchRecord(String scRecord, LinkedList<String> file, int numberOfPages, int numberOfFields, int value, String operator) {
        ListIterator<String> iterator = file.listIterator();
        int offset = TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH;
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print(stringToName(scRecord.substring(offset + i * FIELD_NAME_LENGTH, offset + (i + 1) * FIELD_NAME_LENGTH) + "\t"));
        }
        System.out.println();
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String record = iterator.next();
                int keyField = stringToData(record.substring(0, FIELD_DATA_LENGTH));
                if (operator.equals("L")) {
                    printFieldValues(numberOfFields, record);
                }
                else if (operator.equals("=") && keyField == value) {
                    printFieldValues(numberOfFields, record);
                    return;
                }
                else if (operator.equals(">") && keyField > value) {
                    printFieldValues(numberOfFields, record);
                }
                else if (operator.equals("<")) {
                    if (keyField < value) {
                        printFieldValues(numberOfFields, record);
                    }
                    else {
                        return;
                    }
                }
            }
        }
    }

    private static int stringToData(String s) {
        return Integer.parseInt(s);
    }

    private static String stringToName(String s) {
        int i = 0;
        while (s.charAt(i) == ' ') {
            i++;
        }
        return s.substring(i);
    }

    private static void systemCatalog(LinkedList<String> catalog, int numberOfPages, String typeName, int operation) throws FileNotFoundException {
        ListIterator<String> iterator = catalog.listIterator();
        iterator.next();
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            int numberOfRecords = stringToData(pageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                    SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String record = iterator.next(), name = stringToName(record.substring(0, TYPE_NAME_LENGTH));
                if (operation == 6) {
                    System.out.println(name);
                }
                else if (typeName.equals(name)) {
                    int numberOfTypePages = stringToData(record.substring(TYPE_NAME_LENGTH, TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH));
                    int numberOfFields = stringToData(record.substring(TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                            TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
                    LinkedList<String> file = readFile(typeName, false, numberOfTypePages, "", TYPE_PAGE_UNUSED_SPACE_LENGTH,
                            TYPE_NUMBER_OF_PAGES_LENGTH, TYPE_NUMBER_OF_RECORDS_LENGTH);
                    int keyField = 0;
                    if (operation != 5) {
                        System.out.println("Enter key field.");
                        keyField = Integer.parseInt(CONSOLE.nextLine());
                    }
                    String newRecord = dataToString(keyField, FIELD_DATA_LENGTH);
                    if (operation == 1 || operation == 3) {
                        for (int k = 2; k <= numberOfFields; k++) {
                            System.out.println("Enter " + k + "'th field value.");
                            newRecord += dataToString(Integer.parseInt(CONSOLE.nextLine()), FIELD_DATA_LENGTH);
                        }
                        for (int k = numberOfFields; k < FIELD_MAX_NUMBER; k++) {
                            newRecord += dataToString(0, FIELD_DATA_LENGTH);
                        }
                    }
                    switch (operation) {
                        case 1:
                            if (createRecord(typeName, file, numberOfTypePages, keyField, newRecord)) {
                                iterator.set(record.substring(0, TYPE_NAME_LENGTH) + dataToString(++numberOfTypePages, TYPE_NUMBER_OF_PAGES_LENGTH)
                                        + record.substring(TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH));
                                writeFile(SC_FILENAME, catalog);
                            }
                            System.out.println("Record is created.");
                            return;
                        case 2:
                            if (deleteRecord(typeName, file, numberOfTypePages, keyField)) {
                                iterator.set(record.substring(0, TYPE_NAME_LENGTH) + dataToString(--numberOfTypePages, TYPE_NUMBER_OF_PAGES_LENGTH)
                                        + record.substring(TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH));
                                writeFile(SC_FILENAME, catalog);
                            }
                            System.out.println("Record is deleted.");
                            return;
                        case 3:
                            updateRecord(typeName, file, numberOfTypePages, keyField, newRecord);
                            System.out.println("Record is updated.");
                            return;
                        case 4:
                            System.out.println("Enter search operator (<, >, =).");
                            searchRecord(record, file, numberOfTypePages, numberOfFields, keyField, CONSOLE.nextLine());
                            return;
                        case 5:
                            searchRecord(record, file, numberOfTypePages, numberOfFields, 0, "L");
                            return;
                    }
                }
            }
        }
    }

    private static void updateRecord(String typeName, LinkedList<String> file, int numberOfPages, int keyField, String newRecord) throws FileNotFoundException {
        ListIterator<String> iterator = file.listIterator();
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String currentRecord = iterator.next();
                if (stringToData(currentRecord.substring(0, FIELD_DATA_LENGTH)) == keyField) {
                    iterator.set(newRecord);
                    writeFile(typeName, file);
                    return;
                }
            }
        }
    }

    private static void writeFile(String fileName, LinkedList<String> file) throws FileNotFoundException {
        PrintStream write = new PrintStream(new File(fileName + EXTENSION));
        for (String line : file) {
            write.println(line);
        }
        write.close();
    }
}