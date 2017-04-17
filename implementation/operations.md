<h1 align="center">Operations</h1>

<h2 align="center">DDL Operations</h2>

<h3 align="center">Create a Type</h3>

```
FUNCTION createType
  create a new record
  GET type name from user
  add type name to the record
  GET number of fields from user
  FOREACH field
    GET name from user
    add field to the record
  END FOREACH
  OPEN system catalog
  go to last page in system catalog
  add this record after the last record in last page
  update number of records in page header
  IF there is no empty record slots in the last page
    create a new page
    add page number to newly created page header
  END IF
  SAVE and CLOSE system catalog
  create a file with type name
  OPEN the file
  create a new page
  add page number to newly created page header
  SAVE and CLOSE the file
END createType
```

<h3 align="center">Delete a Type</h3>

```
FUNCTION deleteType
  GET type name from user
  OPEN system catalog
  FOREACH page in system catalog
    read number of records from page header
    FOREACH record in that page
      IF old record is deleted
        shift the current record by one record to the left
      ELSE IF key field of the current record matches the user input
        extract old record
      END IF
    END FOREACH
  END FOREACH
  IF there is no type names in the last page
    delete this page
    update number of pages in system catalog header
  END IF
  IF old record is NOT deleted
    delete the last record
  END IF
  IF there is no records in the last page
    delete last page
  END IF
  update number of records in last page header
  SAVE and CLOSE system catalog
  OPEN the file
  delete all pages and records of that file
  SAVE and CLOSE the file
END deleteType
```

<P style="page-break-before: always">

<h3 align="center">List all Types</h3>

```
FUNCTION listAllTypes
  OPEN system catalog
  FOREACH page in system catalog
    read number of records from page header
    FOREACH record in that page
      PRINT type name and number of fields and field names
    END FOREACH
  END FOREACH
  CLOSE system catalog
END listAllTypes
```

<h2 align="center">DML Operations</h2>

```
FUNCTION chooseDML(operation type)
  GET type name from user
  OPEN system catalog
  FOREACH page in system catalog
    read number of records from page header
    FOREACH record in that page
      IF type name matches the user input
        CALL the function according to the operation type
        BREAK ALL
      END IF
    END FOREACH
  END FOREACH
  CLOSE system catalog
END chooseDML
```

<h3 align="center">Create a Record</h3>

```
FUNCTION createRecord(number of fields)
  OPEN the file
  create a new record
  FOREACH field
    GET value from user
    add field to the record
  END FOREACH
  FOREACH page in that file
    read number of records from page header
    FOREACH record in that page
      IF new record is inserted
        shift the current record by one record to the right
      ELSE IF key field of the new record is less than key field of the current record
        insert new record before current record
      END IF
    END FOREACH
  END FOREACH
  IF new record is inserted
    insert the last shifted record
  ELSE
    insert new record
  END IF
  update number of records in last page header
  IF there is no empty record slots in the last page
    create a new page
    add page number to newly created page header
  END IF
  SAVE and CLOSE the file
END createRecord
```

<P style="page-break-before: always">

<h3 align="center">Delete a Record</h3>

```
FUNCTION deleteRecord
  OPEN the file
  GET key field from user
  FOREACH page in that file
    read number of records from page header
    FOREACH record in that page
      IF old record is deleted
        shift the current record by one record to the left
      ELSE IF key field of the current record matches the user input
        extract old record
      END IF
    END FOREACH
  END FOREACH
  IF old record is NOT deleted
    delete the last record
  END IF
  IF there is no records in the last page
    delete last page
  END IF
  update number of records in last page header
  SAVE and CLOSE the file
END deleteRecord
```

<h3 align="center">Update a Record</h3>

```
FUNCTION updateRecord(number of fields)
  OPEN the file
  create a new record
  FOREACH field
    GET value from user
    add field to the record
  END FOREACH
  FOREACH page in that file
    read number of records from page header
    FOREACH record in that page
      IF key field of the current record matches the user input
        insert new record
        BREAK ALL
      END IF
    END FOREACH
  END FOREACH
  SAVE and CLOSE the file
END updateRecord
```

<P style="page-break-before: always">

<h3 align="center">Search for a Record</h3>

```
FUNCTION searchRecord
  GET search operator from user
  GET searched value from user
  OPEN the file
  FOREACH page in that file
    FOREACH record in that page
      IF search operator is <
        IF key field is less than searched value
          PRINT all fields of that record
        ELSE
          BREAK ALL
        END IF
      ELSE IF search operator is > AND key field is more than searched value
        PRINT all fields of that record
      ELSE IF search operator is = AND key field is equal to searched value
        PRINT all fields of that record
        BREAK ALL
      END IF
    END FOREACH
  END FOREACH
  CLOSE the file
END searchRecord
```

<h3 align="center">List all Records</h3>

```
FUNCTION listAllRecords
  OPEN the file
  FOREACH page in that file
    FOREACH record in that page
      PRINT all fields of that record
    END FOREACH
  END FOREACH
  CLOSE the file
END listAllRecords
```