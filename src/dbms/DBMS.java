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
            System.out.println("1 - DDL Operations.");
            System.out.println("2 - DML Operations.");
            System.out.println("Enter any other integer to exit.");
            int choice = Integer.parseInt(CONSOLE.nextLine());
            switch (choice) {
                case 1:
                    System.out.println("1 - Create a Type.");
                    System.out.println("2 - Delete a Type.");
                    System.out.println("3 - List all Types.");
                    switch (Integer.parseInt(CONSOLE.nextLine())) {
                        case 1:
                            createType();
                            break;
                        case 2:
                            //deleteType();
                            break;
                        case 3:
                            systemCatalog("", 6);
                            break;
                    }
                    break;
                case 2:
                    System.out.println("1 - Create a Record.");
                    System.out.println("2 - Delete a Record.");
                    System.out.println("3 - Update a Record.");
                    System.out.println("4 - Search for a Record.");
                    System.out.println("5 - List all Records.");
                    int operation = Integer.parseInt(CONSOLE.nextLine());
                    System.out.println("Enter the type name.");
                    systemCatalog(CONSOLE.nextLine(), operation);
                    break;
                default:
                    return;
            }
        }
    }

    private static boolean createRecord(String scRecord, int numberOfPages, int numberOfFields) throws FileNotFoundException {
        String typeName = stringToName(scRecord.substring(0, TYPE_NAME_LENGTH));
        LinkedList<String> file = readFile(typeName);
        ListIterator<String> iterator = file.listIterator();
        System.out.println("Enter key field.");
        int keyField = Integer.parseInt(CONSOLE.nextLine()), records = 0;
        String record = getFieldValues(keyField, numberOfFields), oldRecord = "";
        boolean inserted = false, newPage = false;
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)) + ".");
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String currentRecord = iterator.next();
                if (inserted) {
                    iterator.set(oldRecord);
                    oldRecord = currentRecord;
                }
                else if (keyField < stringToData(currentRecord.substring(0, FIELD_DATA_LENGTH))) {
                    iterator.set(record);
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
            file.add(record);
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
        System.out.println("Record is created.");
        return newPage;
    }

    private static void createType() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
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
        LinkedList<String> catalog = readFile(SC_FILENAME);
        catalog.add(record);
        String oldCatalogHeader = catalog.get(0);
        int numberOfPages = stringToData(oldCatalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + SC_NUMBER_OF_PAGES_LENGTH));
        int cursor = (numberOfPages - 1) * SC_PAGE_LENGTH + 1;
        String lastPageHeader = catalog.get(cursor);
        System.out.println("Reading system catalog page #" + stringToData(lastPageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH,
                SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH)) + ".");
        int numberOfRecords = stringToData(lastPageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH)) + 1;
        catalog.set(cursor, lastPageHeader.substring(0, SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH)
                + dataToString(numberOfRecords, SC_NUMBER_OF_RECORDS_LENGTH));
        if (numberOfRecords == SC_PAGE_LENGTH - 1) {
            numberOfPages++;
            catalog.add(nameToString("", SC_PAGE_UNUSED_SPACE_LENGTH) + dataToString(numberOfPages, SC_NUMBER_OF_PAGES_LENGTH)
                    + dataToString(0, SC_NUMBER_OF_RECORDS_LENGTH));
            catalog.set(0, oldCatalogHeader.substring(0, SYSTEM_NAME_LENGTH) + dataToString(numberOfPages, SC_NUMBER_OF_PAGES_LENGTH));
        }
        writeFile(SC_FILENAME, catalog);
        LinkedList<String> file = new LinkedList<>();
        file.add(nameToString("", TYPE_PAGE_UNUSED_SPACE_LENGTH) + dataToString(1, TYPE_NUMBER_OF_PAGES_LENGTH)
                + dataToString(0, TYPE_NUMBER_OF_RECORDS_LENGTH));
        writeFile(typeName, file);
        System.out.println("Type is created.");
    }

    private static String dataToString(int data, int desiredLength) {
        int dataLength = ("" + data).length();
        String s = "";
        for (int i = 0; i < desiredLength - dataLength; i++) {
            s += "0";
        }
        return s + ("" + data);
    }

    private static boolean deleteRecord(String scRecord, int numberOfPages) throws FileNotFoundException {
        String typeName = stringToName(scRecord.substring(0, TYPE_NAME_LENGTH));
        LinkedList<String> file = readFile(typeName);
        ListIterator<String> iterator = file.listIterator();
        System.out.println("Enter key field.");
        int keyField = Integer.parseInt(CONSOLE.nextLine()), records = 0;
        boolean deleted = false, emptyPage = false;
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next(), currentRecord;
            System.out.println("Reading page #" + stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)) + ".");
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            if (numberOfRecords > 0) {
                currentRecord = iterator.next();
                if (deleted) {
                    iterator.previous();
                    iterator.previous();
                    iterator.previous();
                    iterator.set(currentRecord);
                    iterator.next();
                    iterator.next();
                    iterator.next();
                }
                else if (keyField == stringToData(currentRecord.substring(0, FIELD_DATA_LENGTH))) {
                    deleted = true;
                }
            }
            for (int j = 1; j < numberOfRecords; j++) {
                currentRecord = iterator.next();
                if (deleted) {
                    iterator.previous();
                    iterator.previous();
                    iterator.set(currentRecord);
                    iterator.next();
                    iterator.next();
                }
                else if (keyField == stringToData(currentRecord.substring(0, FIELD_DATA_LENGTH))) {
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
        System.out.println("Record is deleted.");
        return emptyPage;
    }

    /*private static void deleteType() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        LinkedList<String> catalog = readFile(SC_FILENAME.split("\\.")[0]);
        String catalogHeader = catalog.get(0), systemName = stringToName(catalogHeader.substring(0, SYSTEM_NAME_LENGTH));
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH));
        boolean existType = false;
        for (ListIterator<String> iterator = catalog.listIterator(); iterator.hasNext();) {
            if (iterator.next().equals(typeName)) {
                existType = true;
                LinkedList<String> file = new LinkedList<>();
                String oldFileHeader = readFile(typeName).get(0), fileHeader = dataToString(0, USAGE_STATUS_LENGTH) + dataToString(1, SC_NUMBER_OF_PAGES_LENGTH)
                        + oldFileHeader.substring(USAGE_STATUS_LENGTH + SC_NUMBER_OF_PAGES_LENGTH);
                file.add(fileHeader);
                String pageHeader = nameToString("", TYPE_PAGE_UNUSED_SPACE_LENGTH) + dataToString(1, SC_NUMBER_OF_PAGES_LENGTH)
                        + dataToString(0, TYPE_NUMBER_OF_RECORDS_LENGTH) + dataToString(0, TYPE_NUMBER_OF_RECORDS_LENGTH);
                file.add(pageHeader);
                writeFile(typeName, file);
                iterator.remove();
                System.out.println("Type is deleted.");
                break;
            }
        }
        if (!existType) {
            System.out.println("ERROR: No such type.");
        }
        else {
            catalogHeader = nameToString(systemName, SYSTEM_NAME_LENGTH) + dataToString(numberOfTypes - 1, SC_NUMBER_OF_RECORDS_LENGTH);
            catalog.set(0, catalogHeader);
            writeFile(SC_FILENAME.split("\\.")[0], catalog);
        }
    }*/
    private static String getFieldValues(int keyField, int numberOfFields) {
        String record = dataToString(keyField, FIELD_DATA_LENGTH);
        for (int i = 2; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field value.");
            record += dataToString(Integer.parseInt(CONSOLE.nextLine()), FIELD_DATA_LENGTH);
        }
        for (int i = numberOfFields; i < FIELD_MAX_NUMBER; i++) {
            record += dataToString(0, FIELD_DATA_LENGTH);
        }
        return record;
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

    private static LinkedList<String> readFile(String fileName) throws FileNotFoundException {
        LinkedList<String> file = new LinkedList<>();
        Scanner read = new Scanner(new File(fileName + EXTENSION));
        while (read.hasNextLine()) {
            file.add(read.nextLine());
        }
        read.close();
        return file;
    }

    private static void searchRecord(String scRecord, String operator, int value, int numberOfPages, int numberOfFields) throws FileNotFoundException {
        String typeName = stringToName(scRecord.substring(0, TYPE_NAME_LENGTH));
        ListIterator<String> iterator = readFile(typeName).listIterator();
        int offset = TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH;
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print(stringToName(scRecord.substring(offset + i * FIELD_NAME_LENGTH, offset + (i + 1) * FIELD_NAME_LENGTH) + "\t"));
        }
        System.out.println();
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)) + ".");
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

    private static void systemCatalog(String typeName, int operation) throws FileNotFoundException {
        LinkedList<String> catalog = readFile(SC_FILENAME);
        ListIterator<String> iterator = catalog.listIterator();
        String catalogHeader = iterator.next();
        int numberOfPages = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + SC_NUMBER_OF_PAGES_LENGTH));
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading system catalog page #" + stringToData(pageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH,
                    SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH)) + ".");
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
                    switch (operation) {
                        case 1:
                            if (createRecord(record, numberOfTypePages, numberOfFields)) {
                                iterator.set(record.substring(0, TYPE_NAME_LENGTH) + dataToString(++numberOfTypePages, TYPE_NUMBER_OF_PAGES_LENGTH)
                                        + record.substring(TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH));
                                writeFile(SC_FILENAME, catalog);
                            }
                            return;
                        case 2:
                            if (deleteRecord(record, numberOfTypePages)) {
                                iterator.set(record.substring(0, TYPE_NAME_LENGTH) + dataToString(--numberOfTypePages, TYPE_NUMBER_OF_PAGES_LENGTH)
                                        + record.substring(TYPE_NAME_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH));
                                writeFile(SC_FILENAME, catalog);
                            }
                            return;
                        case 3:
                            updateRecord(typeName, numberOfTypePages, numberOfFields);
                            return;
                        case 4:
                            System.out.println("Enter search operator (<, >, =).");
                            String operator = CONSOLE.nextLine();
                            System.out.println("Enter searched value.");
                            int value = Integer.parseInt(CONSOLE.nextLine());
                            searchRecord(record, operator, value, numberOfTypePages, numberOfFields);
                            return;
                        case 5:
                            searchRecord(record, "L", 0, numberOfTypePages, numberOfFields);
                            return;
                    }
                }
            }
        }
    }

    private static void updateRecord(String typeName, int numberOfPages, int numberOfFields) throws FileNotFoundException {
        LinkedList<String> file = readFile(typeName);
        ListIterator<String> iterator = file.listIterator();
        System.out.println("Enter key field.");
        int keyField = Integer.parseInt(CONSOLE.nextLine());
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)) + ".");
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String oldRecord = iterator.next();
                if (stringToData(oldRecord.substring(0, FIELD_DATA_LENGTH)) == keyField) {
                    iterator.set(getFieldValues(keyField, numberOfFields));
                    writeFile(typeName, file);
                    System.out.println("Record is updated.");
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