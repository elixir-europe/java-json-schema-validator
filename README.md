# Java Json Schema validation library based on JSONP v1.1 Json parser.

###### compatibility
The library supports draft-4, draft-6, draft-7, draft-2019-09 and draft-2020-12 versions of Json Schema.

###### maven repository
import via maven:

```xml
<dependencies>
  <dependency>
    <groupId>es.elixir.bsc.json.schema</groupId>
    <artifactId>jaronuinga</artifactId>
    <version>0.5.5</version>
  </dependency>
...
<repositories>
  <repository>
    <id>jaronuinga</id>
    <url>https://inb.bsc.es/maven</url>
  </repository>
```

The simplest usage:
```java
JsonSchema schema = JsonSchemaReader.getReader().read(url); // parse JsonSchema from the URL location
List<ValidationError> errors = new ArrayList<>(); // array to collect errors
schema.validate(json, errors); // validate JsonObject
```
Note that instead of URL users could provide their own schema locators.
JsonSchemaLocator object is used for JsonSchema URI resolution and as a cache for local Schemas' definitions -
to resolve "$ref" Json Pointers.

To provide flexibility it is possible to get callbacks during the validation process.
```java
schema.validate(json, errors, (
    PrimitiveSchema subschema, String pointer, JsonValue value, JsonValue parent, List<ValidationError> err) -> {
});
```
Here above we have:
- subschema - current validating Json (sub)schema
- pointer - Json Pointer to the validating Json value
- value - currently validating Json value
- parent - a parent of currently validating Json value
- err - collected validation errors so far

Note, that providing ExtendedJsonSchemaLocator (which collects all subschemas as originated jsons), we can
associate validated JsonValue with corresponding Json Object which describes the schema:
```java
JsonSchema schema = JsonSchemaReader.getReader().read(locator);
schema.validate(json, errors, (
    PrimitiveSchema subschema, String pointer, JsonValue value, JsonValue parent, List<ValidationError> err) -> {
        JsonObject subschemaJsonObject = locator.getSchema(subschema.getId(), subschema.getJsonPointer());
});
```
We can also stop further parsing on error via the callback:
```java
schema.validate(json, errors, (
    PrimitiveSchema subschema, String pointer, JsonValue value, JsonValue parent, List<ValidationError> err) -> {
        throw new ValidationException(new ValidationError(subschema.getId(), subschema.getJsonPointer(), ""));
});
```
