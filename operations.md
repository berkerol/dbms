<h1 align="center">Operations</h1>

```
% Called before manipulation operations
% Finds type and calls the function for the operation
FUNCTION chooseManipulation(operation type)
  GET type name from user
  OPEN system catalog
    read number of pages from system catalog header
    FOREACH page in system catalog
      read number of types from page header
      FOREACH type name in page
        IF type name matches the input
          CALL the function for the operation
          BREAK ALL
        END IF
      END FOREACH
    END FOREACH
  SAVE and CLOSE system catalog
END chooseManipulation
```

```
% Called before listing operations
% Finds type and calls the function for the operation
FUNCTION chooseListing(operation type)
  GET type name from user
  OPEN system catalog
    read number of pages from system catalog header
    FOREACH page in system catalog
      read number of types from page header
      FOREACH type name in page
        CALL the function for the operation
      END FOREACH
    END FOREACH
  CLOSE system catalog
END chooseListing
```

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
    read page number from page header
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
FUNCTION deleteType(type name, number of pages, number of types)
  delete this type name from system catalog
  update number of types in page header
  shift all type names after that position by one name to the left
  go to last page in system catalog
  IF there is no types in the last page
    delete this page
    update number of pages in system catalog header
  END IF
  OPEN the file
  change usage status to deleted in file header
  delete all pages and records of that file
  SAVE and CLOSE the file
END deleteType
```

<h3 align="center">List all Types</h3>

```
FUNCTION listTypes
  PRINT type name
END listTypes
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
  find the right position to insert new record using key field
  shift all records after that position by one record to the right
  add new record to that position
  IF there is no empty slots in the last page
    read page number from page header
    create a new page
    update number of pages in file header
    add page number to newly created page header
  END IF
  update numbers of active and deleted records in page headers
  SAVE and CLOSE the file
END createRecord
```

<h3 align="center">Delete a Record</h3>

```
FUNCTION deleteRecord
  OPEN the file
  GET key field from user
  find the record with that key
  change usage status to deleted in record header
  move that record to the end
  shift all records after that position by one record to the left
  update numbers of active and deleted records in page headers
  SAVE and CLOSE the file
END deleteRecord
```

<P style="page-break-before: always">

<h3 align="center">Update a Record</h3>

```
FUNCTION updateRecord
  OPEN the file
  GET key field from user
  find the record with that key
  FOREACH field except key field
    GET value from user
    add field to the record
  END FOREACH
  SAVE and CLOSE the file
END updateRecord
```

<h3 align="center">Search for a Record</h3>

```
FUNCTION searchRecord
  GET search operator from user
  GET searched value from user
  OPEN the file
  FOREACH page in that file
    IF there is no active record in that page
      BREAK
    END IF
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
  END FOREACH
  CLOSE the file
END searchRecord
```

<h3 align="center">List all Records</h3>

```
FUNCTION listRecords
  OPEN the file
  FOREACH page in that file
    IF there is no active record in that page
      BREAK
    END IF
    FOREACH active record in that page
      PRINT all fields of that record
    END FOREACH
  END FOREACH
  CLOSE the file
END listRecords
```