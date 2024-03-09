# Batch Data Transformer

This project is a configurable batch for data transformation. It allows for the inclusion of multiple datasets and related configurations that define the processing methods applied by the batch.

## Configuration

The batch configuration is based on two different parts:

- **Data:** all the files in csv format that you have to process.
- **Structure:** all the files in json format configuring the structure of csv (e.g. columns, type of fields etc.) and the operations to perform to transform the data on csv (filter conditions, aggregations, join etc.). Every csv file will have a json configuration file with the same file name.

You can find here an example of the configuration file structure and location:
[restaurant_orders](src/main/resources/config/restaurant_orders)


If you want to run the application as a **"jar"** file, you need to place the dataset configuration folder under the **"config"** directory in the same location as the jar file.

## Configurable operations

Each JSON configuration file can be customized to instruct the batch to perform the following operations:

### Filter

To filter the data of the current csv, you have to configure the following fields inside **filterConditionConfigs**:

- **conditionKey:** ID that defines the condition and will be useful in more advanced operations.
- **logicalOperator:** can be "AND" or "OR" and defines how the filters defined inside the **conditions** array should be concatenate.
- **conditions:** array containing the filter conditions.
- **fieldKey:** name of the field.
- **comparisonOperator:** operator defining how to compare the field. It can assume the following values:
  - **=** and **!=**: numeric, alphanumeric and date
  - **>**, **<**, **>=**, **<=**: numeric and date
- **fieldValue:** the fixed value of the comparison.

**Example:**

```json
"filterConditionConfigs": [
    {
      "conditionKey": "[CONDITION_ID]",
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
```






