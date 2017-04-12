<h1 align="center">Operations</h1>

<h2 align="center">DDL Operations</h2>

<h3 align="center">Create a Type</h3>

```
FUNCTION createType
  GET type name from user
  OPEN system catalog
  read number of pages from system catalog header
  go to last page in system catalog
  add this type name after the last type name in last page
  update number of types in page header
  IF there is no empty slots in the last page
    create a new page
    add page number to newly created page header
    update number of pages in system catalog header
  END IF
  create a file with type name
  OPEN the file
  change usage status to active in file header
  create a new page
  add page number to newly created page header
  GET number of fields from user
  add numbers of pages and fields to file header
  FOREACH field name
    GET field names from user
    add field names to file header
  END FOREACH
  SAVE and CLOSE the file
  SAVE and CLOSE system catalog
END createType
```

<h3 align="center">Delete a Type</h3>

```
FUNCTION deleteType
  GET type name from user
  OPEN system catalog
  read number of pages from system catalog header
  FOREACH page in system catalog
    read number of types from page header
    FOREACH type name in that page
      IF type name is deleted
        shift the current type name by one type name to the left
      ELSE IF type name matches the user input
        delete this type name from system catalog
      END IF
    END FOREACH
    update number of types in page header
  END FOREACH
  IF there is no type names in the last page
    delete this page
    update number of pages in system catalog header
  END IF
  OPEN the file
  change usage status to deleted in file header
  delete all pages and records of that file
  SAVE and CLOSE the file
  SAVE and CLOSE system catalog
END deleteType
```

<P style="page-break-before: always">

<h3 align="center">List all Types</h3>

```
FUNCTION listAllTypes
  GET type name from user
  OPEN system catalog
  read number of pages from system catalog header
  FOREACH page in system catalog
    read number of types from page header
    FOREACH type name in that page
      PRINT type name
    END FOREACH
  END FOREACH
  CLOSE system catalog
END listAllTypes
```

<h2 align="center">DML Operations</h2>

<h3 align="center">Create a Record</h3>

```
FUNCTION createRecord
  OPEN the file
  read number of fields from file header
  GET key field from user
  create a new record
  change usage status to active in record header
  add key field to the record
  FOREACH field except key field
    GET value from user
    add field to the record
  END FOREACH
  read number of pages from file header
  FOREACH page in that file
    read numbers of active and deleted records from page header
    FOREACH active record in that page
      IF new record is inserted
        shift the current active record by one record to the right
      ELSE IF key field of the new record is less than key field of the current active record
        insert new record before current active record
      END IF
    END FOREACH
    FOREACH deleted record in that page
      IF new record is inserted
        shift the current deleted record by one record to the right
      ELSE
        insert new record before current deleted record
      END IF
    END FOREACH
    update numbers of active and deleted records in page header
  END FOREACH
  IF new record is inserted
    insert the last shifted record
  ELSE
    insert new record
  END IF
  update numbers of active and deleted records in last page header
  IF there is no empty slots in the last page
    create a new page
    add page number to newly created page header
    update number of pages in file header
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
  read number of pages from file header
  FOREACH page in that file
    read numbers of active and deleted records from page header
    FOREACH active record in that page
      IF old record is deleted
        shift the current active record by one record to the left
      ELSE IF key field of the current active record matches the user input
        extract old record
        change usage status to deleted in record header
      END IF
    END FOREACH
    IF there is a deleted record in that page
      insert old record before current deleted record
      update numbers of active and deleted records in page header
      BREAK
    END IF
    update numbers of active and deleted records in page header
  END FOREACH
  SAVE and CLOSE the file
END deleteRecord
```

<h3 align="center">Update a Record</h3>

```
FUNCTION updateRecord
  OPEN the file
  read number of fields from file header
  GET key field from user
  read number of pages from file header
  FOREACH page in that file
    read number of active records from page header
    FOREACH active record in that page
      IF key field of the current active record matches the user input
        FOREACH field except key field
          GET value from user
          add field to the record
        END FOREACH
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
    FOREACH active record in that page
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
    IF there is a deleted record in that page
      BREAK
    END IF
  END FOREACH
  CLOSE the file
END searchRecord
```

<h3 align="center">List all Records</h3>

```
FUNCTION listAllRecords
  OPEN the file
  FOREACH page in that file
    FOREACH active record in that page
      PRINT all fields of that record
    END FOREACH
    IF there is a deleted record in that page
      BREAK
    END IF
  END FOREACH
  CLOSE the file
END listAllRecords
```

```
FUNCTION chooseDML(operation type)
  GET type name from user
  OPEN system catalog
  read number of pages from system catalog header
  FOREACH page in system catalog
    read number of types from page header
    FOREACH type name in that page
      IF type name matches the user input
        CALL the function according to the operation type
        BREAK ALL
      END IF
    END FOREACH
  END FOREACH
  CLOSE system catalog
END chooseDML
```