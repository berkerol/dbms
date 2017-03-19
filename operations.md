<h1 align="center">Operations</h1>

<h2 align="center">DDL Operations</h2>

<h3 align="center">Create a Type</h3>

```
FUNCTION createType
  GET type name from user
  OPEN system catalog
    IF a type with this name exists
      PRINT error type already exits
    ELSE
      create a file with that name
      add type name to system catalog
      update number of types in system catalog header
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
    END IF
  SAVE and CLOSE system catalog
END createType
```

<h3 align="center">Delete a Type</h3>

```
FUNCTION deleteType
  GET type name from user
  OPEN system catalog
    IF a type with this name does not exist
      PRINT error no such type
    ELSE
      OPEN the file
        change usage status to deleted in file header
      SAVE and CLOSE the file
      delete type name from system catalog
      update number of types in system catalog header
    END IF
  SAVE and CLOSE system catalog
END deleteType
```

<h3 align="center">List all Types</h3>

```
FUNCTION listTypes
  OPEN system catalog
    FOREACH type name
      PRINT type name
    END FOREACH
  CLOSE system catalog
END listTypes
```

<P style="page-break-before: always">

<h2 align="center">DML Operations</h2>

<h3 align="center">Create a Record</h3>

```
FUNCTION createRecord
  GET type name from user
  OPEN system catalog
    IF a type with this name does not exist
      PRINT error no such type
    ELSE
      OPEN the file
        read number of fields from file header
        GET key field from user
        IF a record with this key exists in active records
          PRINT error record already exists
        ELSE
          create a new record
          change usage status to active in record header
          add number of fields to record header
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
        END IF
      SAVE and CLOSE the file
    END IF
  CLOSE system catalog
END createRecord
```

<h3 align="center">Delete a Record</h3>

```
FUNCTION deleteRecord
  GET type name from user
  OPEN system catalog
    IF a type with this name does not exist
      PRINT error no such type
    ELSE
      OPEN the file
        GET key field from user
        IF a record with this key does not exist in active records
          PRINT error no such record
        ELSE
          find the record with that key
          change usage status to deleted in record header
          move that record to the end
          shift all records after that position by one record to the left
          update numbers of active and deleted records in page headers
        END IF
      SAVE and CLOSE the file
    END IF
  CLOSE system catalog
END deleteRecord
```

<P style="page-break-before: always">

<h3 align="center">Update a Record</h3>

```
FUNCTION updateRecord
  GET type name from user
  OPEN system catalog
    IF a type with this name does not exist
      PRINT error no such type
    ELSE
      OPEN the file
        GET key field from user
        IF a record with this key does not exist in active records
          PRINT error no such record
        ELSE
          find the record with that key
          FOREACH field except key field
            GET value from user
            add field to the record
          END FOREACH
        END IF
      SAVE and CLOSE the file
    END IF
  CLOSE system catalog
END updateRecord
```

<h3 align="center">Search for a Record</h3>

```
FUNCTION searchRecord
  GET type name from user
  GET search operator from user
  GET searched value from user
  OPEN system catalog
    IF a type with this name does not exist
      PRINT error no such type
    ELSE
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
    END IF
  CLOSE system catalog
END searchRecord
```

<P style="page-break-before: always">

<h3 align="center">List all Records</h3>

```
FUNCTION listRecords
  GET type name from user
  OPEN system catalog
    IF a type with this name does not exist
      PRINT error no such type
    ELSE
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
    END IF
  CLOSE system catalog
END listRecords
```