package dbms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class DBMS {

    private static final Scanner CONSOLE = new Scanner(System.in);
    private static final int FIELD_DATA_LENGTH = 4;
    private static final int FIELD_NAME_LENGTH = 10;
    private static final int MAX_NUMBER_OF_FIELDS = 16;
    private static final int NUMBER_OF_FIELDS_LENGTH = 2;
    private static final int NUMBER_OF_PAGES_LENGTH = 2;
    private static final int NUMBER_OF_RECORDS_LENGTH = 2;
    private static final int NUMBER_OF_TYPES_LENGTH = 5;
    private static final int PAGE_LENGTH = 16;
    private static final int PAGE_NUMBER_LENGTH = 2;
    private static final int RECORD_LENGTH = 65;
    private static final String SYSTEM_CATALOG_FILENAME = "SystemCatalog.txt";
    private static final int SYSTEM_NAME_LENGTH = 50;
    private static final int UNUSED_SPACE_LENGTH = 11;
    private static final int USAGE_STATUS_LENGTH = 1;

    public static void main(String[] args) throws FileNotFoundException {
        while (true) {
            System.out.println("DDL Operations.");
            System.out.println("\t1 - Create a Type.");
            System.out.println("\t2 - Delete a Type.");
            System.out.println("\t3 - List all Types.");
            System.out.println("DML Operations.");
            System.out.println("\t4 - Create a Record.");
            System.out.println("\t5 - Delete a Record.");
            System.out.println("\t6 - Update a Record.");
            System.out.println("\t7 - Search for a Record.");
            System.out.println("\t8 - List all Records.");
            System.out.println("Enter any other integer to exit.");
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
                case 4:
                    createRecord();
                    break;
                case 5:
                    deleteRecord();
                    break;
                case 6:
                    updateRecord();
                    break;
                case 7:
                    searchRecord();
                    break;
                case 8:
                    listRecords();
                    break;
                default:
                    return;
            }
        }
    }

    private static void createRecord() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        Scanner readCatalog = new Scanner(new File(SYSTEM_CATALOG_FILENAME));
        String catalogHeader = readCatalog.nextLine();
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        boolean exist = false;
        for (int i = 0; i < numberOfTypes; i++) {
            if (typeName.equals(readCatalog.nextLine())) {
                exist = true;
                LinkedList<String> file = readFile(typeName), records = readFileOnlyRecords(typeName), newFile = new LinkedList<>();
                int cursor = 0;
                String fileHeader = file.get(cursor);
                int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
                int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                        USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
                System.out.println("Enter key field.");
                int keyField = Integer.parseInt(CONSOLE.nextLine());
                for (int j = 0; j < numberOfPages; j++) {
                    cursor = j * PAGE_LENGTH + 1;
                    String pageHeader = file.get(cursor);
                    System.out.println("Reading page #" + stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH, UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
                    int numberOfActiveRecords = stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                            UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
                    if (numberOfActiveRecords == 0) {
                        break;
                    }
                    for (int k = 1; k <= numberOfActiveRecords; k++) {
                        if (stringToData(file.get(cursor + k).substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH)) == keyField) {
                            System.out.println("ERROR: Record already exists.");
                            return;
                        }
                    }
                }
                System.out.println("No old record with the given key was found, you can continue.");
                String record = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(keyField, FIELD_DATA_LENGTH);
                for (int j = 2; j <= numberOfFields; j++) {
                    System.out.println("Enter " + j + "'th field.");
                    record += dataToString(Integer.parseInt(CONSOLE.nextLine()), FIELD_DATA_LENGTH);
                }
                for (int j = numberOfFields; j < MAX_NUMBER_OF_FIELDS; j++) {
                    record += dataToString(0, FIELD_DATA_LENGTH);
                }
                String next = "";
                boolean found = false;
                for (Iterator<String> iterator = records.iterator(); iterator.hasNext();) {
                    next = iterator.next();
                    if (stringToData(next.substring(0, USAGE_STATUS_LENGTH)) == 0) {
                        found = true;
                        break;
                    }
                    if (keyField < stringToData(next.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH))) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    records.add(records.indexOf(next), record);
                }
                else {
                    records.addLast(record);
                }
                int pageCounter = 0, activeRecordCounter = 0, deletedRecordCounter = 0;
                while (!records.isEmpty()) {
                    record = records.poll();
                    newFile.add(record);
                    if (stringToData(record.substring(0, USAGE_STATUS_LENGTH)) == 1) {
                        activeRecordCounter++;
                    }
                    else {
                        deletedRecordCounter++;
                    }
                    if (activeRecordCounter + deletedRecordCounter == PAGE_LENGTH - 1) {
                        pageCounter++;
                        String pageHeader = nameToString("", UNUSED_SPACE_LENGTH) + dataToString(pageCounter, NUMBER_OF_PAGES_LENGTH)
                                + dataToString(activeRecordCounter, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecordCounter, NUMBER_OF_RECORDS_LENGTH);
                        newFile.add((pageCounter - 1) * PAGE_LENGTH, pageHeader);
                        deletedRecordCounter = 0;
                        if (activeRecordCounter == 0) {
                            pageCounter--;
                            for (int j = 0; j < PAGE_LENGTH; j++) {
                                newFile.removeLast();
                            }
                            break;
                        }
                        activeRecordCounter = 0;
                    }
                }
                pageCounter++;
                String pageHeader = nameToString("", UNUSED_SPACE_LENGTH) + dataToString(pageCounter, NUMBER_OF_PAGES_LENGTH)
                        + dataToString(activeRecordCounter, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecordCounter, NUMBER_OF_RECORDS_LENGTH);
                newFile.add((pageCounter - 1) * PAGE_LENGTH, pageHeader);
                String newFileHeader = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(pageCounter, NUMBER_OF_PAGES_LENGTH)
                        + fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH);
                newFile.addFirst(newFileHeader);
                writeFile(typeName, newFile);
                System.out.println("Record is added.");
                break;
            }
        }
        readCatalog.close();
        if (!exist) {
            System.out.println("ERROR: No such type.");
        }
    }

    private static void createType() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        LinkedList<String> catalog = readFile(SYSTEM_CATALOG_FILENAME.split("\\.")[0]);
        String catalogHeader = catalog.get(0), systemName = stringToName(catalogHeader.substring(0, SYSTEM_NAME_LENGTH));
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        for (int i = 1; i <= numberOfTypes; i++) {
            if (catalog.get(i).equals(typeName)) {
                System.out.println("ERROR: Type already exists.");
                return;
            }
        }
        System.out.println("No old type with the given key was found, you can continue.");
        LinkedList<String> file = new LinkedList<>();
        String pageHeader = nameToString("", UNUSED_SPACE_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH)
                + dataToString(0, NUMBER_OF_RECORDS_LENGTH) + dataToString(0, NUMBER_OF_RECORDS_LENGTH);
        String fileHeader = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH);
        System.out.println("Enter the number of fields.");
        int numberOfFields = Integer.parseInt(CONSOLE.nextLine());
        fileHeader += dataToString(numberOfFields, NUMBER_OF_FIELDS_LENGTH);
        for (int i = 1; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field name.");
            fileHeader += nameToString(CONSOLE.nextLine(), FIELD_NAME_LENGTH);
        }
        for (int i = numberOfFields; i < MAX_NUMBER_OF_FIELDS; i++) {
            fileHeader += nameToString("", FIELD_NAME_LENGTH);
        }
        file.add(fileHeader);
        file.add(pageHeader);
        writeFile(typeName, file);
        catalogHeader = nameToString(systemName, SYSTEM_NAME_LENGTH) + dataToString(numberOfTypes + 1, NUMBER_OF_TYPES_LENGTH);
        catalog.set(0, catalogHeader);
        catalog.add(typeName);
        writeFile(SYSTEM_CATALOG_FILENAME.split("\\.")[0], catalog);
    }

    private static String dataToString(int data, int desiredLength) {
        int dataLength = ("" + data).length();
        String s = "";
        for (int i = 0; i < desiredLength - dataLength; i++) {
            s += "0";
        }
        return s + ("" + data);
    }

    private static void deleteRecord() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        Scanner readCatalog = new Scanner(new File(SYSTEM_CATALOG_FILENAME));
        String catalogHeader = readCatalog.nextLine();
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        boolean existType = false;
        label:
        for (int i = 0; i < numberOfTypes; i++) {
            if (typeName.equals(readCatalog.nextLine())) {
                existType = true;
                LinkedList<String> file = readFile(typeName), records = readFileOnlyRecords(typeName), newFile = new LinkedList<>();
                int cursor = 0;
                String fileHeader = file.get(cursor);
                int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
                System.out.println("Enter key field.");
                int keyField = Integer.parseInt(CONSOLE.nextLine());
                boolean existKey = false;
                for (int j = 0; j < numberOfPages; j++) {
                    cursor = j * PAGE_LENGTH + 1;
                    String pageHeader = file.get(cursor);
                    System.out.println("Reading page #" + stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH, UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
                    int numberOfActiveRecords = stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                            UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
                    if (numberOfActiveRecords == 0) {
                        break;
                    }
                    for (int k = 1; k <= numberOfActiveRecords; k++) {
                        String oldRecord = file.get(cursor + k);
                        if (stringToData(oldRecord.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH)) == keyField) {
                            String record = dataToString(0, USAGE_STATUS_LENGTH) + oldRecord.substring(1);
                            records.remove(oldRecord);
                            records.add(record);
                            int pageCounter = 0, activeRecordCounter = 0, deletedRecordCounter = 0;
                            while (!records.isEmpty()) {
                                record = records.poll();
                                newFile.add(record);
                                if (stringToData(record.substring(0, USAGE_STATUS_LENGTH)) == 1) {
                                    activeRecordCounter++;
                                }
                                else {
                                    deletedRecordCounter++;
                                }
                                if (activeRecordCounter + deletedRecordCounter == PAGE_LENGTH - 1) {
                                    pageCounter++;
                                    pageHeader = nameToString("", UNUSED_SPACE_LENGTH) + dataToString(pageCounter, NUMBER_OF_PAGES_LENGTH)
                                            + dataToString(activeRecordCounter, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecordCounter, NUMBER_OF_RECORDS_LENGTH);
                                    newFile.add((pageCounter - 1) * PAGE_LENGTH, pageHeader);
                                    deletedRecordCounter = 0;
                                    if (activeRecordCounter == 0) {
                                        pageCounter--;
                                        for (int l = 0; l < PAGE_LENGTH; l++) {
                                            newFile.removeLast();
                                        }
                                        break;
                                    }
                                    activeRecordCounter = 0;
                                }
                            }
                            pageCounter++;
                            pageHeader = nameToString("", UNUSED_SPACE_LENGTH) + dataToString(pageCounter, NUMBER_OF_PAGES_LENGTH)
                                    + dataToString(activeRecordCounter, NUMBER_OF_RECORDS_LENGTH) + dataToString(deletedRecordCounter, NUMBER_OF_RECORDS_LENGTH);
                            newFile.add((pageCounter - 1) * PAGE_LENGTH, pageHeader);
                            String newFileHeader = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(pageCounter, NUMBER_OF_PAGES_LENGTH)
                                    + fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH);
                            newFile.addFirst(newFileHeader);
                            writeFile(typeName, newFile);
                            System.out.println("Record is deleted.");
                            break label;
                        }
                    }
                }
                if (!existKey) {
                    System.out.println("ERROR: No such record.");
                }
            }
        }
        readCatalog.close();
        if (!existType) {
            System.out.println("ERROR: No such type.");
        }
    }

    private static void deleteType() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        LinkedList<String> catalog = readFile(SYSTEM_CATALOG_FILENAME.split("\\.")[0]);
        String catalogHeader = catalog.get(0), systemName = stringToName(catalogHeader.substring(0, SYSTEM_NAME_LENGTH));
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        boolean existType = false;
        for (int i = 1; i <= numberOfTypes; i++) {
            if (typeName.equals(catalog.get(i))) {
                existType = true;
                LinkedList<String> oldFile = readFile(typeName), file = new LinkedList<>();
                String oldFileHeader = oldFile.get(0), fileHeader = dataToString(0, USAGE_STATUS_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH)
                        + oldFileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH);
                file.add(fileHeader);
                String pageHeader = nameToString("", UNUSED_SPACE_LENGTH) + dataToString(1, NUMBER_OF_PAGES_LENGTH)
                        + dataToString(0, NUMBER_OF_RECORDS_LENGTH) + dataToString(0, NUMBER_OF_RECORDS_LENGTH);
                file.add(pageHeader);
                writeFile(typeName, file);
                catalog.remove(i);
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

    private static void listRecords() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        Scanner readCatalog = new Scanner(new File(SYSTEM_CATALOG_FILENAME));
        String catalogHeader = readCatalog.nextLine();
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        boolean exist = false;
        for (int i = 0; i < numberOfTypes; i++) {
            if (typeName.equals(readCatalog.nextLine())) {
                exist = true;
                LinkedList<String> file = readFile(typeName);
                int cursor = 0;
                String fileHeader = file.get(cursor);
                int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
                int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                        USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
                for (int j = 0; j < numberOfPages; j++) {
                    cursor = j * PAGE_LENGTH + 1;
                    String pageHeader = file.get(cursor);
                    System.out.println("Reading page #" + stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH, UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
                    int numberOfActiveRecords = stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                            UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
                    if (numberOfActiveRecords == 0) {
                        break;
                    }
                    for (int k = 1; k <= numberOfActiveRecords; k++) {
                        String record = file.get(cursor + k);
                        for (int l = 0; l < numberOfFields; l++) {
                            System.out.print(stringToData(record.substring(l * FIELD_DATA_LENGTH + 1, (l + 1) * FIELD_DATA_LENGTH + 1)) + "\t");
                        }
                        System.out.println();
                    }
                }
            }
        }
        readCatalog.close();
        if (!exist) {
            System.out.println("ERROR: No such type.");
        }
    }

    private static void listTypes() throws FileNotFoundException {
        Scanner readCatalog = new Scanner(new File(SYSTEM_CATALOG_FILENAME));
        String catalogHeader = readCatalog.nextLine();
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        for (int i = 0; i < numberOfTypes; i++) {
            System.out.println(readCatalog.nextLine());
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

    private static LinkedList<String> readFile(String typeName) throws FileNotFoundException {
        LinkedList<String> file = new LinkedList<>();
        Scanner read = new Scanner(new File(typeName + "." + SYSTEM_CATALOG_FILENAME.split("\\.")[1]));
        while (read.hasNextLine()) {
            file.add(read.nextLine());
        }
        read.close();
        return file;
    }

    private static LinkedList<String> readFileOnlyRecords(String typeName) throws FileNotFoundException {
        LinkedList<String> file = new LinkedList<>();
        Scanner read = new Scanner(new File(typeName + "." + SYSTEM_CATALOG_FILENAME.split("\\.")[1]));
        while (read.hasNextLine()) {
            String line = read.nextLine();
            if (line.length() == RECORD_LENGTH) {
                file.add(line);
            }
        }
        read.close();
        return file;
    }

    private static void searchRecord() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        System.out.println("Enter search operator (<, >, =).");
        String operator = CONSOLE.nextLine();
        System.out.println("Enter searched value.");
        int value = Integer.parseInt(CONSOLE.nextLine());
        Scanner readCatalog = new Scanner(new File(SYSTEM_CATALOG_FILENAME));
        String catalogHeader = readCatalog.nextLine();
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        boolean existType = false;
        label:
        for (int i = 0; i < numberOfTypes; i++) {
            if (typeName.equals(readCatalog.nextLine())) {
                existType = true;
                LinkedList<String> file = readFile(typeName);
                int cursor = 0;
                String fileHeader = file.get(cursor);
                int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
                int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                        USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
                boolean existKey = false;
                for (int j = 0; j < numberOfPages; j++) {
                    cursor = j * PAGE_LENGTH + 1;
                    String pageHeader = file.get(cursor);
                    System.out.println("Reading page #" + stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH, UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
                    int numberOfActiveRecords = stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                            UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
                    if (numberOfActiveRecords == 0) {
                        break;
                    }
                    for (int k = 1; k <= numberOfActiveRecords; k++) {
                        String record = file.get(cursor + k);
                        int keyField = stringToData(record.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH));
                        if (operator.equals("<")) {
                            if (keyField < value) {
                                for (int l = 0; l < numberOfFields; l++) {
                                    System.out.print(stringToData(record.substring(l * FIELD_DATA_LENGTH + 1, (l + 1) * FIELD_DATA_LENGTH + 1)) + "\t");
                                }
                                System.out.println();
                                existKey = true;
                            }
                            else {
                                break label;
                            }
                        }
                        else if (operator.equals(">") && keyField > value) {
                            for (int l = 0; l < numberOfFields; l++) {
                                System.out.print(stringToData(record.substring(l * FIELD_DATA_LENGTH + 1, (l + 1) * FIELD_DATA_LENGTH + 1)) + "\t");
                            }
                            System.out.println();
                            existKey = true;
                        }
                        else if (operator.equals("=") && keyField == value) {
                            for (int l = 0; l < numberOfFields; l++) {
                                System.out.print(stringToData(record.substring(l * FIELD_DATA_LENGTH + 1, (l + 1) * FIELD_DATA_LENGTH + 1)) + "\t");
                            }
                            System.out.println();
                            break label;
                        }
                    }
                }
                if (!existKey) {
                    System.out.println("ERROR: No such record.");
                }
            }
        }
        readCatalog.close();
        if (!existType) {
            System.out.println("ERROR: No such type.");
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

    private static void updateRecord() throws FileNotFoundException {
        System.out.println("Enter the type name.");
        String typeName = CONSOLE.nextLine();
        Scanner readCatalog = new Scanner(new File(SYSTEM_CATALOG_FILENAME));
        String catalogHeader = readCatalog.nextLine();
        int numberOfTypes = stringToData(catalogHeader.substring(SYSTEM_NAME_LENGTH, SYSTEM_NAME_LENGTH + NUMBER_OF_TYPES_LENGTH));
        boolean existType = false;
        label:
        for (int i = 0; i < numberOfTypes; i++) {
            if (typeName.equals(readCatalog.nextLine())) {
                existType = true;
                LinkedList<String> file = readFile(typeName);
                int cursor = 0;
                String fileHeader = file.get(cursor);
                int numberOfPages = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH));
                int numberOfFields = stringToData(fileHeader.substring(USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH,
                        USAGE_STATUS_LENGTH + NUMBER_OF_PAGES_LENGTH + NUMBER_OF_FIELDS_LENGTH));
                System.out.println("Enter key field.");
                int keyField = Integer.parseInt(CONSOLE.nextLine());
                boolean existKey = false;
                for (int j = 0; j < numberOfPages; j++) {
                    cursor = j * PAGE_LENGTH + 1;
                    String pageHeader = file.get(cursor);
                    System.out.println("Reading page #" + stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH, UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH)) + ".");
                    int numberOfActiveRecords = stringToData(pageHeader.substring(UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH,
                            UNUSED_SPACE_LENGTH + PAGE_NUMBER_LENGTH + NUMBER_OF_RECORDS_LENGTH));
                    if (numberOfActiveRecords == 0) {
                        break;
                    }
                    for (int k = 0; k < numberOfActiveRecords; k++) {
                        String oldRecord = file.get(++cursor);
                        if (stringToData(oldRecord.substring(USAGE_STATUS_LENGTH, USAGE_STATUS_LENGTH + FIELD_DATA_LENGTH)) == keyField) {
                            String record = dataToString(1, USAGE_STATUS_LENGTH) + dataToString(keyField, FIELD_DATA_LENGTH);
                            for (int l = 2; l <= numberOfFields; l++) {
                                System.out.println("Enter " + l + "'th field.");
                                record += dataToString(Integer.parseInt(CONSOLE.nextLine()), FIELD_DATA_LENGTH);
                            }
                            for (int l = numberOfFields; l < MAX_NUMBER_OF_FIELDS; l++) {
                                record += dataToString(0, FIELD_DATA_LENGTH);
                            }
                            file.set(cursor, record);
                            writeFile(typeName, file);
                            System.out.println("Record is updated.");
                            break label;
                        }
                    }
                }
                if (!existKey) {
                    System.out.println("ERROR: No such record.");
                }
            }
        }
        readCatalog.close();
        if (!existType) {
            System.out.println("ERROR: No such type.");
        }
    }

    private static void writeFile(String typeName, LinkedList<String> file) throws FileNotFoundException {
        PrintStream write = new PrintStream(new File(typeName + "." + SYSTEM_CATALOG_FILENAME.split("\\.")[1]));
        for (String line : file) {
            write.println(line);
        }
        write.close();
    }
}