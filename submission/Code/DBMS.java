import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
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
     * Size of a field data in bytes.
     */
    private static final int FIELD_DATA_LENGTH = 4;
    /**
     * Max number of fields in a record.
     */
    private static final int FIELD_MAX_NUMBER = 10;
    /**
     * Size of a field name in bytes.
     */
    private static final int FIELD_NAME_LENGTH = 7;
    /**
     * Size of the string containing number of fields in a record in bytes.
     */
    private static final int NUMBER_OF_FIELDS_LENGTH = 2;
    /**
     * System catalog file name.
     */
    private static final String SC_FILENAME = "SystemCatalog";
    /**
     * Length of the string containing page number in a system catalog page header in bytes.
     */
    private static final int SC_NUMBER_OF_PAGES_LENGTH = 2;
    /**
     * Length of the string containing number of records in a system catalog page in bytes.
     */
    private static final int SC_NUMBER_OF_RECORDS_LENGTH = 2;
    /**
     * Total number of elements (page header + records) in a system catalog page.
     */
    private static final int SC_PAGE_LENGTH = 11;
    /**
     * Size of a system catalog page in bytes.
     */
    private static final int SC_PAGE_SIZE = 1024;
    /**
     * Size of the unused space (due to fixed page size) in a system catalog page.
     */
    private static final int SC_PAGE_UNUSED_SPACE_LENGTH = 8;
    /**
     * Size of a system catalog page header in bytes.
     */
    private static final int SC_PAGE_HEADER_SIZE = SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH;
    /**
     * Size of a type name in bytes.
     */
    private static final int TYPE_NAME_LENGTH = 27;
    /**
     * Size of a system catalog record in bytes.
     */
    private static final int SC_RECORD_SIZE = TYPE_NAME_LENGTH + NUMBER_OF_FIELDS_LENGTH + FIELD_MAX_NUMBER * FIELD_NAME_LENGTH;
    /**
     * Length of the string containing page number in a page header in bytes.
     */
    private static final int TYPE_NUMBER_OF_PAGES_LENGTH = 2;
    /**
     * Length of the string containing number of records in a page in bytes.
     */
    private static final int TYPE_NUMBER_OF_RECORDS_LENGTH = 2;
    /**
     * Total number of elements (page header + records) in a page.
     */
    private static final int TYPE_PAGE_LENGTH = 25;
    /**
     * Size of a page in bytes.
     */
    private static final int TYPE_PAGE_SIZE = 1024;
    /**
     * Size of the unused space (due to fixed page size) in a page.
     */
    private static final int TYPE_PAGE_UNUSED_SPACE_LENGTH = 10;
    /**
     * Size of a page header in bytes.
     */
    private static final int TYPE_PAGE_HEADER_SIZE = TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH;
    /**
     * Size of a record in bytes.
     */
    private static final int TYPE_RECORD_SIZE = FIELD_MAX_NUMBER * FIELD_DATA_LENGTH;
    /**
     * Stores current number of system catalog pages (used during and updated after operations) for
     * file I/O.
     */
    private static int numberOfSystemCatalogPages = 0;
    /**
     * Stores current number of pages (used during and updated after operations) for file I/O.
     */
    private static int numberOfTypePages = 0;

    public static void main(String[] args) throws IOException {
        if (!new File(SC_FILENAME + EXTENSION).exists()) {
            createEmptyFile(SC_FILENAME, true, SC_PAGE_UNUSED_SPACE_LENGTH, SC_NUMBER_OF_PAGES_LENGTH, SC_NUMBER_OF_RECORDS_LENGTH);
            System.out.println("System Catalog is created.");
        }
        while (true) {
            LinkedList<String> file = readFile(SC_FILENAME, true, "system catalog ", SC_PAGE_SIZE,
                    SC_PAGE_UNUSED_SPACE_LENGTH, SC_NUMBER_OF_PAGES_LENGTH, SC_NUMBER_OF_RECORDS_LENGTH);
            System.out.println("1 - DDL Operations.");
            System.out.println("2 - DML Operations.");
            System.out.println("Enter any other integer to exit.");
            switch (stringToData(CONSOLE.nextLine())) {
                case 1:
                    System.out.println("1 - Create a Type.");
                    System.out.println("2 - Delete a Type.");
                    System.out.println("3 - List all Types.");
                    System.out.println("Enter any other integer to go back.");
                    switch (stringToData(CONSOLE.nextLine())) {
                        case 1:
                            System.out.println("Enter the type name.");
                            createType(file, CONSOLE.nextLine());
                            System.out.println("Type is created.");
                            break;
                        case 2:
                            System.out.println("Enter the type name.");
                            deleteType(file, CONSOLE.nextLine());
                            System.out.println("Type is deleted.");
                            break;
                        case 3:
                            System.out.print("name\t" + "#fields\t");
                            for (int i = 1; i <= FIELD_MAX_NUMBER; i++) {
                                System.out.print(i + ".field\t");
                            }
                            System.out.println();
                            openSystemCatalog(file.listIterator(), "", 6);
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
                    int operation = stringToData(CONSOLE.nextLine());
                    if (operation < 1 || operation > 5) {
                        break;
                    }
                    System.out.println("Enter the type name.");
                    openSystemCatalog(file.listIterator(), CONSOLE.nextLine(), operation);
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * Creates empty files (files without record) for system catalog and types.
     *
     * @param fileName name of the created file
     * @param fileType system catalog or type file
     * @param pageUnusedSpaceLength length of unused space in a page
     * @param numberOfPagesLength length of the string containing page number
     * @param numberOfRecordsLength length of the string containing number of records
     * @throws IOException when file cannot be written
     */
    private static void createEmptyFile(String fileName, boolean fileType, int pageUnusedSpaceLength,
            int numberOfPagesLength, int numberOfRecordsLength) throws IOException {
        LinkedList<String> file = new LinkedList<>();
        file.add(nameToString("", pageUnusedSpaceLength) + dataToString(1, numberOfPagesLength)
                + dataToString(0, numberOfRecordsLength));
        if (fileType) {
            numberOfSystemCatalogPages = 1;
        }
        else {
            numberOfTypePages = 1;
        }
        writeFile(fileName, file, fileType);
    }

    private static void createRecord(String typeName, LinkedList<String> file, int keyField, String newRecord) throws IOException {
        ListIterator<String> iterator = file.listIterator();
        boolean inserted = false;
        String oldRecord = "";
        int records = 0;
        for (int i = 0; i < numberOfTypePages; i++) {
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
        String lastPageHeader = file.get((numberOfTypePages - 1) * TYPE_PAGE_LENGTH);
        file.set((numberOfTypePages - 1) * TYPE_PAGE_LENGTH, lastPageHeader.substring(0, TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)
                + dataToString(records, TYPE_NUMBER_OF_RECORDS_LENGTH));
        if (records == TYPE_PAGE_LENGTH - 1) {
            file.add(nameToString("", TYPE_PAGE_UNUSED_SPACE_LENGTH) + dataToString(++numberOfTypePages, TYPE_NUMBER_OF_PAGES_LENGTH)
                    + dataToString(0, TYPE_NUMBER_OF_RECORDS_LENGTH));
        }
        writeFile(typeName, file, false);
    }

    private static void createType(LinkedList<String> catalog, String typeName) throws IOException {
        System.out.println("Enter the number of fields.");
        int numberOfFields = stringToData(CONSOLE.nextLine());
        StringBuilder record = new StringBuilder().append(nameToString(typeName, TYPE_NAME_LENGTH)).append(dataToString(numberOfFields, NUMBER_OF_FIELDS_LENGTH));
        for (int i = 1; i <= numberOfFields; i++) {
            System.out.println("Enter " + i + "'th field name.");
            record.append(nameToString(CONSOLE.nextLine(), FIELD_NAME_LENGTH));
        }
        for (int i = numberOfFields; i < FIELD_MAX_NUMBER; i++) {
            record.append(nameToString("", FIELD_NAME_LENGTH));
        }
        catalog.add(record.toString());
        String lastPageHeader = catalog.get((numberOfSystemCatalogPages - 1) * SC_PAGE_LENGTH);
        int numberOfRecords = stringToData(lastPageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH)) + 1;
        catalog.set((numberOfSystemCatalogPages - 1) * SC_PAGE_LENGTH, lastPageHeader.substring(0, SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH)
                + dataToString(numberOfRecords, SC_NUMBER_OF_RECORDS_LENGTH));
        if (numberOfRecords == SC_PAGE_LENGTH - 1) {
            catalog.add(nameToString("", SC_PAGE_UNUSED_SPACE_LENGTH) + dataToString(++numberOfSystemCatalogPages, SC_NUMBER_OF_PAGES_LENGTH)
                    + dataToString(0, SC_NUMBER_OF_RECORDS_LENGTH));
        }
        writeFile(SC_FILENAME, catalog, true);
        createEmptyFile(typeName, false, TYPE_PAGE_UNUSED_SPACE_LENGTH, TYPE_NUMBER_OF_PAGES_LENGTH, TYPE_NUMBER_OF_RECORDS_LENGTH);
    }

    /**
     * Converts integers to fixed size strings by adding zeroes to left.
     *
     * @param data integer to be converted to fixed size string
     * @param desiredLength length of fixed size string
     * @return fixed size string
     */
    private static String dataToString(int data, int desiredLength) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < desiredLength - ("" + data).length(); i++) {
            s.append("0");
        }
        return s.append(data).toString();
    }

    private static void deleteRecord(String typeName, LinkedList<String> file, int keyField) throws IOException {
        ListIterator<String> iterator = file.listIterator();
        boolean deleted = false;
        int records = 0;
        for (int i = 0; i < numberOfTypePages; i++) {
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
            numberOfTypePages--;
            records = TYPE_PAGE_LENGTH - 2;
        }
        String lastPageHeader = file.get((numberOfTypePages - 1) * TYPE_PAGE_LENGTH);
        file.set((numberOfTypePages - 1) * TYPE_PAGE_LENGTH, lastPageHeader.substring(0, TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH)
                + dataToString(records, TYPE_NUMBER_OF_RECORDS_LENGTH));
        writeFile(typeName, file, false);
    }

    private static void deleteType(LinkedList<String> catalog, String typeName) throws IOException {
        ListIterator<String> iterator = catalog.listIterator();
        boolean deleted = false;
        int records = 0;
        for (int i = 0; i < numberOfSystemCatalogPages; i++) {
            String pageHeader = iterator.next(), record;
            int numberOfRecords = stringToData(pageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                    SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH));
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
                else if (typeName.equals(stringToName(record.substring(0, TYPE_NAME_LENGTH)))) {
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
                else if (typeName.equals(stringToName(record.substring(0, TYPE_NAME_LENGTH)))) {
                    deleted = true;
                }
            }
            records = numberOfRecords;
        }
        catalog.removeLast();
        records--;
        if (records == -1) {
            catalog.removeLast();
            records = SC_PAGE_LENGTH - 2;
            numberOfSystemCatalogPages--;
        }
        String lastPageHeader = catalog.get((numberOfSystemCatalogPages - 1) * SC_PAGE_LENGTH);
        catalog.set((numberOfSystemCatalogPages - 1) * SC_PAGE_LENGTH, lastPageHeader.substring(0, SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH)
                + dataToString(records, SC_NUMBER_OF_RECORDS_LENGTH));
        writeFile(SC_FILENAME, catalog, true);
        createEmptyFile(typeName, false, TYPE_PAGE_UNUSED_SPACE_LENGTH, TYPE_NUMBER_OF_PAGES_LENGTH, TYPE_NUMBER_OF_RECORDS_LENGTH);
    }

    /**
     * Converts names to fixed size strings by adding spaces to left.
     *
     * @param data name to be converted to fixed size string
     * @param desiredLength length of fixed size string
     * @return fixed size string
     */
    private static String nameToString(String name, int desiredLength) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < desiredLength - name.length(); i++) {
            s.append(" ");
        }
        return s.append(name).toString();
    }

    /**
     * Executes DML operations (also used for listing types). Finds the related system catalog
     * record, opens the type file, takes the required inputs from user then calls the related
     * method.
     *
     * @param iterator iterator of the list of the system catalog
     * @param typeName name of the type used in DML operations
     * @param operation operation type (create, delete, update, search, list)
     * @throws IOException when type file cannot be read or written
     */
    private static void openSystemCatalog(ListIterator<String> iterator, String typeName, int operation) throws IOException {
        for (int i = 0; i < numberOfSystemCatalogPages; i++) {
            String pageHeader = iterator.next();
            int numberOfRecords = stringToData(pageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                    SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String record = iterator.next(), name = stringToName(record.substring(0, TYPE_NAME_LENGTH));
                int numberOfFields = stringToData(record.substring(TYPE_NAME_LENGTH, TYPE_NAME_LENGTH + NUMBER_OF_FIELDS_LENGTH));
                if (operation == 6) {
                    System.out.print(name + "\t" + numberOfFields + "\t");
                    printFieldNames(numberOfFields, record);
                }
                else if (typeName.equals(name)) {
                    LinkedList<String> file = readFile(typeName, false, "", TYPE_PAGE_SIZE, TYPE_PAGE_UNUSED_SPACE_LENGTH,
                            TYPE_NUMBER_OF_PAGES_LENGTH, TYPE_NUMBER_OF_RECORDS_LENGTH);
                    int keyField = 0;
                    if (operation != 5) {
                        System.out.println("Enter key field.");
                        keyField = stringToData(CONSOLE.nextLine());
                    }
                    StringBuilder newRecord = new StringBuilder().append(dataToString(keyField, FIELD_DATA_LENGTH));
                    if (operation == 1 || operation == 3) {
                        for (int k = 2; k <= numberOfFields; k++) {
                            System.out.println("Enter " + k + "'th field value.");
                            newRecord.append(dataToString(stringToData(CONSOLE.nextLine()), FIELD_DATA_LENGTH));
                        }
                        for (int k = numberOfFields; k < FIELD_MAX_NUMBER; k++) {
                            newRecord.append(dataToString(0, FIELD_DATA_LENGTH));
                        }
                    }
                    switch (operation) {
                        case 1:
                            createRecord(typeName, file, keyField, newRecord.toString());
                            System.out.println("Record is created.");
                            return;
                        case 2:
                            deleteRecord(typeName, file, keyField);
                            System.out.println("Record is deleted.");
                            return;
                        case 3:
                            updateRecord(typeName, file, keyField, newRecord.toString());
                            System.out.println("Record is updated.");
                            return;
                        case 4:
                            System.out.println("Enter search operator (<, >, =).");
                            String operator = CONSOLE.nextLine();
                            printFieldNames(numberOfFields, record);
                            searchRecord(file.listIterator(), numberOfFields, keyField, operator);
                            System.out.println("Records are searched.");
                            return;
                        case 5:
                            printFieldNames(numberOfFields, record);
                            searchRecord(file.listIterator(), numberOfFields, 0, "L");
                            System.out.println("Records are listed.");
                            return;
                    }
                }
            }
        }
        System.out.println("Types are listed.");
    }

    private static void printFieldNames(int numberOfFields, String record) {
        int offset = TYPE_NAME_LENGTH + NUMBER_OF_FIELDS_LENGTH;
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print(stringToName(record.substring(offset + i * FIELD_NAME_LENGTH, offset + (i + 1) * FIELD_NAME_LENGTH) + "\t"));
        }
        System.out.println();
    }

    private static void printFieldValues(int numberOfFields, String record) {
        for (int i = 0; i < numberOfFields; i++) {
            System.out.print(stringToData(record.substring(i * FIELD_DATA_LENGTH, (i + 1) * FIELD_DATA_LENGTH)) + "\t");
        }
        System.out.println();
    }

    private static LinkedList<String> readFile(String fileName, boolean fileType, String messageType, int pageSize,
            int pageUnusedSpaceLength, int numberOfPagesLength, int numberOfRecordsLength) throws IOException {
        LinkedList<String> file = new LinkedList<>();
        RandomAccessFile raf = new RandomAccessFile(fileName + EXTENSION, "r");
        int numberOfPages = (int) (raf.length() / pageSize);
        if (fileType) {
            numberOfSystemCatalogPages = numberOfPages;
        }
        else {
            numberOfTypePages = numberOfPages;
        }
        for (int i = 0; i < numberOfPages; i++) {
            System.out.println("Reading " + messageType + "page #" + (i + 1) + ".");
            byte[] bytes = new byte[pageSize];
            raf.seek(i * pageSize);
            raf.read(bytes);
            String all = new String(bytes);
            int numberOfRecords = stringToData(all.substring(pageUnusedSpaceLength + numberOfPagesLength,
                    pageUnusedSpaceLength + numberOfPagesLength + numberOfRecordsLength));
            for (int j = 0; j <= numberOfRecords; j++) {
                file.add(all.split("\\r?\\n")[j]);
            }
        }
        raf.close();
        return file;
    }

    private static void searchRecord(ListIterator<String> iterator, int numberOfFields, int value, String operator) {
        for (int i = 0; i < numberOfTypePages; i++) {
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

    /**
     * Converts fixed size strings to integers.
     *
     * @param s string to be converted to integer
     * @return integer
     */
    private static int stringToData(String s) {
        return Integer.parseInt(s);
    }

    /**
     * Converts fixed size strings to names by removing spaces in the beginning.
     *
     * @param s string to be converted to name
     * @return name
     */
    private static String stringToName(String s) {
        int i = 0;
        while (s.charAt(i) == ' ') {
            i++;
        }
        return s.substring(i);
    }

    private static void updateRecord(String typeName, LinkedList<String> file, int keyField, String newRecord) throws IOException {
        ListIterator<String> iterator = file.listIterator();
        for (int i = 0; i < numberOfTypePages; i++) {
            String pageHeader = iterator.next();
            int numberOfRecords = stringToData(pageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            for (int j = 0; j < numberOfRecords; j++) {
                String currentRecord = iterator.next();
                if (stringToData(currentRecord.substring(0, FIELD_DATA_LENGTH)) == keyField) {
                    iterator.set(newRecord);
                    writeFile(typeName, file, false);
                    return;
                }
            }
        }
    }

    private static void writeFile(String fileName, LinkedList<String> file, boolean fileType) throws IOException {
        int emptyBytes;
        if (fileType) {
            String lastPageHeader = file.get((numberOfSystemCatalogPages - 1) * SC_PAGE_LENGTH);
            int numberOfRecords = stringToData(lastPageHeader.substring(SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH,
                    SC_PAGE_UNUSED_SPACE_LENGTH + SC_NUMBER_OF_PAGES_LENGTH + SC_NUMBER_OF_RECORDS_LENGTH));
            emptyBytes = SC_PAGE_SIZE - SC_PAGE_HEADER_SIZE - 2 - numberOfRecords * (SC_RECORD_SIZE + 2) - 2;
        }
        else {
            String lastPageHeader = file.get((numberOfTypePages - 1) * TYPE_PAGE_LENGTH);
            int numberOfRecords = stringToData(lastPageHeader.substring(TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH,
                    TYPE_PAGE_UNUSED_SPACE_LENGTH + TYPE_NUMBER_OF_PAGES_LENGTH + TYPE_NUMBER_OF_RECORDS_LENGTH));
            emptyBytes = TYPE_PAGE_SIZE - TYPE_PAGE_HEADER_SIZE - 2 - numberOfRecords * (TYPE_RECORD_SIZE + 2) - 2;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < emptyBytes; i++) {
            sb.append('#');
        }
        file.add(sb.toString());
        Path path = Paths.get(fileName + EXTENSION);
        Files.write(path, file);
    }
}