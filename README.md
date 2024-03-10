# Batch Data Transformer

This project is a configurable batch for data transformation. It allows for the inclusion of multiple datasets and related configurations that define the processing methods applied by the batch.

## Batch Configuration

The batch configuration is based on two different parts:

- **Data:** all the files in csv format that you have to process.
- **Structure:** all the files in json format configuring the structure of csv (e.g. columns, type of fields etc.) and operation to perform to transform the data on csv (filter conditions, aggregations, join etc.). Every csv file will have a json configuration file with the same file name.

You can find here an example of the configuration file structure and location:
[restaurant_orders](src/main/resources/config/restaurant_orders)


If you want to run the application as a **"jar"** file, you need to place the dataset configuration folder under the **"config"** directory in the same location as the jar file.

## Basic configuration

There are some basic fields to configure inside json files related to each entity:

- **fileName:** the name of the file.
- **key:** the primary key of the entity.
- **fields:** array of object defining fields properties. Each object will define
  - **name:** the name of the field.
  - **type:** the type of the field. It can assume the following values: **string**, **numeric**, **date**.
  - **logicalFormatterFunc (Optional):** custom function to manipulate the value of the field, described later.
  
Below an example of **fields** array configuration:

```json
{
  "fields": [
    {
      "name": "Order_ID",
      "type": "numeric",
      "logicalFormatterFunc": ""
    },
    {
      "name": "Customer_Name",
      "type": "string",
      "logicalFormatterFunc": ""
    }
  ]
}
```

## Configurable operation

Each JSON configuration file can be customized to instruct the batch to perform the following operation:

### Join

To join two or multiple csv together, you have to configure a json structure as shown in the example below:

```json
{
  "links": [
    {
      "joinKeys": {
        "logicalOperator": "",
        "joinKeyFields": [
          "Restaurant_ID"
        ]
      },
      "fileName": "Restaurants",
      "fileNameTarget": "Orders",
      "joinType": "INNER"
    }
  ]
}
```

Below the field doc for **links**:

- **joinKeys:** the structure to define one or multiple join keys. If you define multiple join keys inside **joinKeyFields**, you have also to define the **logicalOperator**
- **fileName:** the current file name.
- **fileNameTarget:** the name of the file to join with.
- **joinType:** define the type of join to perform. It can assume the following values: **INNER**, **RIGHT** and **LEFT**.

You can configure multiple items inside **links** array, to join multiple csv together.
Since the batch don't know the order to apply join between csv in advance, **you have to define the link structure on both file side**.

### Distinct

To perform the distinct on the current file, removing duplicate rows, you can configure the **applyDistinct** field as shown below:

```json
{
  "applyDistinct": true
}
```

### Filter

To filter the data of the current csv, you have to configure a json structure as shown in the example below:

```json
{
  "filterConditionConfigs": [
    {
      "conditionKey": "123",
      "logicalOperator": "OR",
      "conditions": [
        {
          "fieldKey": "Cuisine",
          "comparisonOperator": "=",
          "fieldValue": "French"
        },
        {
          "fieldKey": "Cuisine",
          "comparisonOperator": "=",
          "fieldValue": "Italian"
        }
      ]
    }
  ]
}
```

Below the field doc for **filterConditionConfigs**:

- **conditionKey:** ID that defines the condition and will be useful in the filter advanced operation.
- **logicalOperator:** can be "AND" or "OR" and defines how the filters defined inside the **conditions** array should be concatenate.
- **conditions:** array containing the filter conditions.
- **fieldKey:** name of the field.
- **comparisonOperator:** operator defining how to compare the field. It can assume the following values:
  - **=** and **!=**: numeric, alphanumeric and date
  - **>**, **<**, **>=**, **<=**: numeric and date
- **fieldValue:** the fixed value of the comparison.

### Advanced filter

To apply advanced filter, concatenating multiple conditions defined into the **filterConditionConfigs** array, you have to configure a json structure as shown in the example below:

```json
{
  "filterLinkConfigs": [
    {
      "conditionKey": "555",
      "currentConditionKey": "123",
      "targetConditionKey": "456",
      "logicalOperator": "AND"
    }
  ]
}
```

Below the field doc for **filterLinkConfigs**:

- **conditionKey:** ID that defines the condition.
- **currentConditionKey:** the condition ID of the first item that you want to concatenate defined in the **filterConditionConfigs** array.
- **targetConditionKey:** the condition ID of the item that you want to concatenate with defined in the **filterConditionConfigs** array.
- **logicalOperator:** can be "AND" or "OR" and defines how the filters should be concatenated.

## Field Logical Formatting

## Run the application

Below an example of the instruction to run the application as jar, passing **datasetName** as program argument:

```shell
java -jar ./target/data-transformer-0.0.1-SNAPSHOT.jar --datasetName=restaurant_orders
```

The output will be produced as csv file into **/output** directory.

## Additional properties

Below some additional properties configurable into the application.properties file before building the jar:

- **json.config.path:** the path where the datasource data are placed (data and structure directories)
- **csv.delimiter:** delimiter used in csv input files to divide columns.
- **force.not.join:** if set to true, if you have multiple files, the batch doesn't apply joins, performing only the other defined operations and producing an output file for each entity.






