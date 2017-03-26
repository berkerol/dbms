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
    private static final int FIELD_NAME_LENGTH = 7;
    private static final int MAX_NUMBER_OF_FIELDS = 16;
    private static final int NUMBER_OF_FIELDS_LENGTH = 2;
    private static final int NUMBER_OF_PAGES_LENGTH = 2;
    private static final int NUMBER_OF_RECORDS_LENGTH = 2;
    private static final int NUMBER_OF_TYPES_LENGTH = 2;
    private static final int PAGE_LENGTH = 16;
    private static final int PAGE_NUMBER_LENGTH = 2;
    private static final int PAGE_UNUSED_SPACE_LENGTH = 11;
    private static final int RECORD_LENGTH = 65;
    private static final String SYSTEM_CATALOG_FILENAME = "SystemCatalog";
    private static final int SYSTEM_CATALOG_PAGE_LENGTH = 36;
    private static final int SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH = 3;
    private static final int SYSTEM_NAME_LENGTH = 50;
    private static final int TYPE_NAME_LENGTH = 27;
    private static final int USAGE_STATUS_LENGTH = 1;

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
                            deleteType();
                            break;
                        case 3:
                            listTypes();
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
                    String typeName = CONSOLE.nextLine();
                    ListIterator<String> iterator = readFile(SYSTEM_CATALOG_FILENAME).listIterator();
                    String catalogHeader = iterator.next();
                    int numberOfPages = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_PAGES_LENGTH));
                    label:
                    for (int i = 0; i < numberOfPages; i++) {
                        String pageHeader = iterator.next();
                        System.out.println("Reading system catalog page #" + stringToData(pageHeader.substring(SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH,
                                SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH)) + ".");
                        int numberOfTypes = stringToData(pageHeader.substring(SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH,
                                SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_TYPES_LENGTH));
                        for (int j = 0; j < numberOfTypes; j++) {
                            if (typeName.equals(stringToName(iterator.next()))) {
                                switch (operation) {
                                    case 1:
                                        createRecord(typeName);
                                        break label;
                                    case 2:
                                        deleteRecord(typeName);
                                        break label;
                                    case 3:
                                        updateRecord(typeName);
                                        break label;
                                    case 4:
                                        searchRecord(typeName);
                                        break label;
                                    case 5:
                                        listRecords(typeName);
                                        break label;
                                }
                            }
                        }
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private static void createRecord(String typeName) throws FileNotFoundException {
        LinkedList<String> file = readFile(typeName);
        ListIterator<String> iterator = file.listIterator();
        String fileHeader = iterator.next();
        int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
        int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
        System.out.println("Enter key field.");
        int keyField = Integer.parseInt(CONSOLE.nextLine());
        String record = getFieldValues(keyField, numberOfFields);
        int activeRecords = 0, deletedRecords = 0;
        String oldRecord = "";
        boolean inserted = false;
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH, PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
            int numberOfActiveRecords = stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                    PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
            int numberOfDeletedRecords = stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH,
                    PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH + NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfActiveRecords; j++) {
                String currentRecord = iterator.next();
                if (inserted) {
                    iterator.set(oldRecord);
                    oldRecord = currentRecord;
                }
                else if (keyField < stringToData(currentRecord.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH))) {
                    iterator.set(record);
                    oldRecord = currentRecord;
                    inserted = true;
                }
            }
            activeRecords = numberOfActiveRecords;
            for (int j = 0; j < numberOfDeletedRecords; j++) {
                String currentRecord = iterator.next();
                if (inserted) {
                    iterator.set(oldRecord);
                    if (stringToData(oldRecord.substring(0, USAGE_STATUS_LENGTH)) == 0) {
                        deletedRecords++;
                    }
                    else {
                        activeRecords++;
                    }
                    oldRecord = currentRecord;
                }
                else {
                    iterator.set(record);
                    oldRecord = currentRecord;
                    inserted = true;
                    activeRecords++;
                }
            }
            if (inserted && i != numberOfPages - 1) {
                file.set(i * PAGE_LENGTH + 1, pageHeader.substring(0, PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)
                        + dataToString(activeRecords, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecords, NUMBER_OF_RECORDS_LENGTH));
            }
            if (i != numberOfPages - 1) {
                activeRecords = 0;
                deletedRecords = 0;
            }
        }
        if (inserted) {
            file.add(oldRecord);
            if (stringToData(oldRecord.substring(0, USAGE_STATUS_LENGTH)) == 0) {
                deletedRecords++;
            }
            else {
                activeRecords++;
            }
        }
        else {
            file.add(record);
            activeRecords++;
        }
        String lastPageHeader = file.get((numberOfPages - 1) * PAGE_LENGTH + 1);
        file.set((numberOfPages - 1) * PAGE_LENGTH + 1, lastPageHeader.substring(0, PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)
                + dataToString(activeRecords, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecords, NUMBER_OF_RECORDS_LENGTH));
        if (activeRecords + deletedRecords == PAGE_LENGTH - 1) {
            String newPageHeader = nameToString("", PAGE_UNUSED_SPACE_LENGTH) + dataToString(++numberOfPages, NUMBER_OF_PAGES_LENGTH)
                    + dataToString(0, NUMBER_OF_RECORDS_LENGTH) + dataToString(0, NUMBER_OF_RECORDS_LENGTH);
            file.add(newPageHeader);
            String newFileHeader = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(numberOfPages, NUMBER_OF_PAGES_LENGTH)
                    + fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH);
            file.set(0, newFileHeader);
        }
        writeFile(typeName, file);
        System.out.println("Record is created.");
    }

    private static void createType() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        LinkedList<String> catalog = readFile(SYSTEM_CATALOG_FILENAME);
        catalog.add(nameToString(typeName, TYPE_NAME_LENGTH));
        String oldCatalogHeader = catalog.get(0);
        int numberOfPages = stringToData(oldCatalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_PAGES_LENGTH));
        int cursor = (numberOfPages - 1) * SYSTEM_CATALOG_PAGE_LENGTH + 1;
        String oldSystemCatalogPageHeader = catalog.get(cursor);
        System.out.println("Reading system catalog page #" + stringToData(oldSystemCatalogPageHeader.substring(SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH,
                SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH)) + ".");
        int numberOfTypes = stringToData(oldSystemCatalogPageHeader.substring(SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH,
                SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_TYPES_LENGTH)) + 1;
        String newSystemCatalogPageHeader = oldSystemCatalogPageHeader.substring(0, SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH)
                + dataToString(numberOfTypes, NUMBER_OF_TYPES_LENGTH);
        catalog.set(cursor, newSystemCatalogPageHeader);
        if (numberOfTypes == SYSTEM_CATALOG_PAGE_LENGTH - 1) {
            numberOfPages++;
            String systemCatalogPageHeader = nameToString("", SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH)
                    + dataToString(numberOfPages, NUMBER_OF_PAGES_LENGTH) + dataToString(0, NUMBER_OF_TYPES_LENGTH);
            catalog.add(systemCatalogPageHeader);
            String newCatalogHeader = oldCatalogHeader.substring(0, SYSTEM_NAME_LENGTH) + dataToString(numberOfPages, NUMBER_OF_PAGES_LENGTH);
            catalog.set(0, newCatalogHeader);
        }
        writeFile(SYSTEM_CATALOG_FILENAME, catalog);
        LinkedList<String> file = new LinkedList<>();
        String pageHeader = nameToString("", PAGE_UNUSED_SPACE_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH)
                + dataToString(0, NUMBER_OF_RECORDS_LENGTH) + dataToString(0, NUMBER_OF_RECORDS_LENGTH);
        System.out.println("Enter the number of fields.");
        file.add(getFieldNames(Integer.parseInt(CONSOLE.nextLine())));
        file.add(pageHeader);
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

    private static void deleteRecord(String typeName) throws FileNotFoundException {
        LinkedList<String> oldFile = readFile(typeName), newFile = new LinkedList<>();
        ListIterator<String> iterator = oldFile.listIterator();
        String fileHeader = iterator.next();
        int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
        System.out.println("Enter key field.");
        int keyField = Integer.parseInt(CONSOLE.nextLine());
        int pageCounter = 0, activeRecordCounter = 0, deletedRecordCounter = 0;
        String record = "";
        label:
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH, PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
            int numberOfActiveRecords = stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                    PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfActiveRecords; j++) {
                String oldRecord = iterator.next();
                if (stringToData(oldRecord.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH)) == keyField) {
                    record = dataToString(0, USAGE_STATUS_LENGTH) + oldRecord.substring(1);
                    break label;
                }
                activeRecordCounter++;
                newFile.add(oldRecord);
            }
            newFile.add((++pageCounter - 1) * PAGE_LENGTH, pageHeader);
            activeRecordCounter = 0;
        }
        if (activeRecordCounter + deletedRecordCounter == PAGE_LENGTH - 1) {
            String pageHeader = nameToString("", PAGE_UNUSED_SPACE_LENGTH) + dataToString(++pageCounter, NUMBER_OF_PAGES_LENGTH)
                    + dataToString(activeRecordCounter, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecordCounter, NUMBER_OF_RECORDS_LENGTH);
            newFile.add((pageCounter - 1) * PAGE_LENGTH, pageHeader);
            deletedRecordCounter = 0;
            activeRecordCounter = 0;
        }
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.length() == RECORD_LENGTH) {
                newFile.add(line);
                if (stringToData(line.substring(0, USAGE_STATUS_LENGTH)) == 1) {
                    activeRecordCounter++;
                }
                else {
                    deletedRecordCounter++;
                }
                if (activeRecordCounter + deletedRecordCounter == PAGE_LENGTH - 1) {
                    String pageHeader = nameToString("", PAGE_UNUSED_SPACE_LENGTH) + dataToString(++pageCounter, NUMBER_OF_PAGES_LENGTH)
                            + dataToString(activeRecordCounter, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecordCounter, NUMBER_OF_RECORDS_LENGTH);
                    newFile.add((pageCounter - 1) * PAGE_LENGTH, pageHeader);
                    System.out.println("Reading page #" + pageCounter + ".");
                    deletedRecordCounter = 0;
                    activeRecordCounter = 0;
                }
            }
        }
        newFile.add(record);
        deletedRecordCounter++;
        String pageHeader = nameToString("", PAGE_UNUSED_SPACE_LENGTH) + dataToString(++pageCounter, NUMBER_OF_PAGES_LENGTH)
                + dataToString(activeRecordCounter, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecordCounter, NUMBER_OF_RECORDS_LENGTH);
        newFile.add((pageCounter - 1) * PAGE_LENGTH, pageHeader);
        String newFileHeader = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(pageCounter, NUMBER_OF_PAGES_LENGTH)
                + fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH);
        newFile.addFirst(newFileHeader);
        writeFile(typeName, newFile);
        System.out.println("Record is deleted.");
    }

    private static void deleteType() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        LinkedList<String> catalog = readFile(SYSTEM_CATALOG_FILENAME.split("\\.")[0]);
        String catalogHeader = catalog.get(0), systemName = stringToName(catalogHeader.substring(0, SYSTEM_NAME_LENGTH));
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        boolean existType = false;
        for (ListIterator<String> iterator = catalog.listIterator(); iterator.hasNext();) {
            if (iterator.next().equals(typeName)) {
                existType = true;
                LinkedList<String> file = new LinkedList<>();
                String oldFileHeader = readFile(typeName).get(0), fileHeader = dataToString(0, USAGE_STATUS_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH)
                        + oldFileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH);
                file.add(fileHeader);
                String pageHeader = nameToString("", PAGE_UNUSED_SPACE_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH)
                        + dataToString(0, NUMBER_OF_RECORDS_LENGTH) + dataToString(0, NUMBER_OF_RECORDS_LENGTH);
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
            catalogHeader = nameToString(systemName, SYSTEM_NAME_LENGTH) + dataToString(numberOfTypes - 1, NUMBER_OF_TYPES_LENGTH);
            catalog.set(0, catalogHeader);
            writeFile(SYSTEM_CATALOG_FILENAME.split("\\.")[0], catalog);
        }
    }

    private static String getFieldNames(int numberOfFields) {
        String fileHeader = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH) + dataToString(numberOfFields, NUMBER_OF_FIELDS_LENGTH);
        for (int i = 1; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field name.");
            fileHeader += nameToString(CONSOLE.nextLine(), FIELD_NAME_LENGTH);
        }
        for (int i = numberOfFields; i < MAX_NUMBER_OF_FIELDS; i++) {
            fileHeader += nameToString("", FIELD_NAME_LENGTH);
        }
        return fileHeader;
    }

    private static String getFieldValues(int keyField, int numberOfFields) {
        String record = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(keyField, FIELD_DATA_LENGTH);
        for (int i = 2; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field value.");
            record += dataToString(Integer.parseInt(CONSOLE.nextLine()), FIELD_DATA_LENGTH);
        }
        for (int i = numberOfFields; i < MAX_NUMBER_OF_FIELDS; i++) {
            record += dataToString(0, FIELD_DATA_LENGTH);
        }
        return record;
    }

    private static void listRecords(String typeName) throws FileNotFoundException {
        ListIterator<String> iterator = readFile(typeName).listIterator();
        String fileHeader = iterator.next();
        int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
        int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
        printFieldNames(numberOfFields, fileHeader);
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH, PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
            int numberOfActiveRecords = stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                    PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfActiveRecords; j++) {
                printFieldValues(numberOfFields, iterator.next());
            }
            if (numberOfActiveRecords < PAGE_LENGTH - 1) {
                break;
            }
        }
    }

    private static void listTypes() throws FileNotFoundException {
        ListIterator<String> iterator = readFile(SYSTEM_CATALOG_FILENAME).listIterator();
        String catalogHeader = iterator.next();
        int numberOfPages = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_PAGES_LENGTH));
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading system catalog page #" + stringToData(pageHeader.substring(SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH,
                    SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH)) + ".");
            int numberOfTypes = stringToData(pageHeader.substring(SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH,
                    SYSTEM_CATALOG_PAGE_UNUSED_SPACE_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_TYPES_LENGTH));
            for (int j = 0; j < numberOfTypes; j++) {
                System.out.println(stringToName(iterator.next()));
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

    private static void printFieldNames(int numberOfFields, String fileHeader) {
        int offset = USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH;
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print(stringToName(fileHeader.substring(i * FIELD_NAME_LENGTH + offset, (i + 1) * FIELD_NAME_LENGTH + offset) + "\t"));
        }
        System.out.println();
    }

    private static void printFieldValues(int numberOfFields, String record) {
        int offset = USAGE_STATUS_LENGTH;
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print(stringToData(record.substring(i * FIELD_DATA_LENGTH + offset, (i + 1) * FIELD_DATA_LENGTH + offset)) + "\t");
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

    private static void searchRecord(String typeName) throws FileNotFoundException {
        System.out.println("Enter search operator (<, >, =).");
        String operator = CONSOLE.nextLine();
        System.out.println("Enter searched value.");
        int value = Integer.parseInt(CONSOLE.nextLine());
        ListIterator<String> iterator = readFile(typeName).listIterator();
        String fileHeader = iterator.next();
        int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
        int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
        printFieldNames(numberOfFields, fileHeader);
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH, PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
            int numberOfActiveRecords = stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                    PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfActiveRecords; j++) {
                String record = iterator.next();
                int keyField = stringToData(record.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH));
                if (operator.equals("<")) {
                    if (keyField < value) {
                        printFieldValues(numberOfFields, record);
                    }
                    else {
                        return;
                    }
                }
                else if (operator.equals(">") && keyField > value) {
                    printFieldValues(numberOfFields, record);
                }
                else if (operator.equals("=") && keyField == value) {
                    printFieldValues(numberOfFields, record);
                    return;
                }
            }
            if (numberOfActiveRecords < PAGE_LENGTH - 1) {
                break;
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

    private static void updateRecord(String typeName) throws FileNotFoundException {
        LinkedList<String> file = readFile(typeName);
        ListIterator<String> iterator = file.listIterator();
        String fileHeader = iterator.next();
        int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
        int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
        System.out.println("Enter key field.");
        int keyField = Integer.parseInt(CONSOLE.nextLine());
        for (int i = 0; i < numberOfPages; i++) {
            String pageHeader = iterator.next();
            System.out.println("Reading page #" + stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH, PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
            int numberOfActiveRecords = stringToData(pageHeader.substring(PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                    PAGE_UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfActiveRecords; j++) {
                String oldRecord = iterator.next();
                if (stringToData(oldRecord.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH)) == keyField) {
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