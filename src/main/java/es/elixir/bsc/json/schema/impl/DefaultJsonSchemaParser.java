/**
 * *****************************************************************************
 * Copyright (C) 2024 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
 * and Barcelona Supercomputing Center (BSC)
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *****************************************************************************
 */

package es.elixir.bsc.json.schema.impl;

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.JsonSchemaParserConfig;
import es.elixir.bsc.json.schema.JsonSchemaVersion;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import static es.elixir.bsc.json.schema.model.JsonEnum.ENUM;
import es.elixir.bsc.json.schema.model.JsonType;
import es.elixir.bsc.json.schema.model.impl.JsonArraySchemaImpl;
import es.elixir.bsc.json.schema.model.impl.JsonBooleanSchemaImpl;
import es.elixir.bsc.json.schema.model.impl.JsonEnumImpl;
import es.elixir.bsc.json.schema.model.impl.JsonIntegerSchemaImpl;
import es.elixir.bsc.json.schema.model.impl.JsonNullSchemaImpl;
import es.elixir.bsc.json.schema.model.impl.JsonNumberSchemaImpl;
import es.elixir.bsc.json.schema.model.impl.JsonObjectSchemaImpl;
import es.elixir.bsc.json.schema.model.impl.JsonSchemaUtil;
import es.elixir.bsc.json.schema.model.impl.JsonStringSchemaImpl;
import static es.elixir.bsc.json.schema.model.JsonConst.CONST;
import es.elixir.bsc.json.schema.model.JsonReference;
import es.elixir.bsc.json.schema.model.JsonSchema;
import static es.elixir.bsc.json.schema.model.PrimitiveSchema.TYPE;
import es.elixir.bsc.json.schema.model.impl.AbstractJsonSchema;
import es.elixir.bsc.json.schema.model.impl.BooleanJsonSchemaImpl;
import es.elixir.bsc.json.schema.model.impl.JsonConstImpl;
import es.elixir.bsc.json.schema.model.impl.JsonReferenceImpl;
import es.elixir.bsc.json.schema.model.impl.AbstractJsonSchemaElement;
import es.elixir.bsc.json.schema.model.impl.JsonMultitypeSchemaWrapper;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import static jakarta.json.JsonValue.ValueType.ARRAY;
import static jakarta.json.JsonValue.ValueType.STRING;

/**
 * @author Dmitry Repchevsky
 */

public class DefaultJsonSchemaParser implements JsonSubschemaParser {
    
    private final JsonSchemaElementsCache cache = new JsonSchemaElementsCache();
    private final Map<String, Object> properties;
    
    public DefaultJsonSchemaParser(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Object> getJsonSchemaParserProperties() {
        return properties;
    }
    
    @Override
    public AbstractJsonSchema parse(JsonSchemaLocator locator, AbstractJsonSchemaElement parent, 
            String jsonPointer, JsonValue value, JsonType type) 
            throws JsonSchemaException {

        AbstractJsonSchema schema = cache.get(locator, jsonPointer);
        if (schema != null) {
            return schema;
        }

        if (value.getValueType() == ValueType.TRUE ||
            value.getValueType() == ValueType.FALSE) {
            return new BooleanJsonSchemaImpl(parent, locator, jsonPointer).read(this, value);
        }

        if (value.getValueType() != ValueType.OBJECT) {
                throw new JsonSchemaException(new ParsingError(
                        ParsingMessage.SCHEMA_OBJECT_ERROR, value.getValueType()));
        }
                
        final JsonObject object = value.asJsonObject();
        
        final JsonValue jref = object.get(JsonReference.REF);
        if (jref != null) {
            if (JsonValue.ValueType.STRING != jref.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                        JsonReference.REF, jref.getValueType().name(), JsonValue.ValueType.STRING.name()));
            }
            
            // before draft 2019-09 $ref ignored any other properties
            if (JsonSchemaVersion.SCHEMA_DRAFT_2019_09.compareTo(getJsonSchemaVersion(locator)) > 0) {
                return new JsonReferenceImpl(parent, locator, jsonPointer).read(this, object);
            }
        }
        
        JsonValue $id = object.get(JsonSchema.ID);
        if ($id == null) {
            $id = object.get("id"); // draft4
        } 

        if ($id != null) {
            if ($id.getValueType() != JsonValue.ValueType.STRING) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                    "id", $id.getValueType().name(), JsonValue.ValueType.STRING.name()));
            } else if (!(parent instanceof JsonMultitypeSchemaWrapper)) {
                // special case for wrapper parent - we already resolved $id there!!!
                final String id = ((JsonString)$id).getString();
                try {
                    locator = locator.resolve(URI.create(id));
                    locator.setSchema(object);
                } catch(IllegalArgumentException ex) {
                    throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_REFERENCE, id));
                }
            }
        }

        final JsonValue type_value = object.get(TYPE);
        final ValueType vtype;
        if (type_value == null) {
            vtype = null;
        } else {
            vtype = type_value.getValueType();
            switch(vtype) {
                case STRING: 
                    try {
                        type = JsonType.fromValue(((JsonString)type_value).getString());
                    } catch(IllegalArgumentException ex) {
                        throw new JsonSchemaException(new ParsingError(
                                ParsingMessage.UNKNOWN_OBJECT_TYPE, ((JsonString)type_value).getString()));
                    }
                case ARRAY: break;
                default:
                    throw new JsonSchemaException(new ParsingError(
                            ParsingMessage.INVALID_ATTRIBUTE_TYPE,
                            "type", type_value.getValueType().name(),
                            "either a string or an array"));
            }
        }

        if (type == null) {
            schema = new JsonMultitypeSchemaWrapper(parent, locator, jsonPointer, 
                    vtype == ValueType.ARRAY ? type_value.asJsonArray() : null);
        } else {
            final JsonArray jenum = JsonSchemaUtil.check(object.get(ENUM), JsonValue.ValueType.ARRAY);
            if (jenum != null) {
                if (jenum.isEmpty()) {
                    throw new JsonSchemaException(new ParsingError(ParsingMessage.EMPTY_ENUM));
                }
                return new JsonEnumImpl(parent, locator, jsonPointer).read(this, object);
            }

            final JsonValue jconst = object.get(CONST);
            if (jconst != null) {
                return new JsonConstImpl(parent, locator, jsonPointer).read(this, object);
            }

            switch(type) {
                case OBJECT: schema = new JsonObjectSchemaImpl(parent, locator, jsonPointer); break;
                case ARRAY: schema = new JsonArraySchemaImpl(parent, locator, jsonPointer); break;
                case STRING: schema = new JsonStringSchemaImpl(parent, locator, jsonPointer); break;
                case NUMBER: schema = new JsonNumberSchemaImpl(parent, locator, jsonPointer); break;
                case INTEGER: schema = new JsonIntegerSchemaImpl(parent, locator, jsonPointer); break;
                case BOOLEAN: schema = new JsonBooleanSchemaImpl(parent, locator, jsonPointer); break;
                case NULL: schema = new JsonNullSchemaImpl(parent, locator, jsonPointer); break;
                default: return null;
            }
        }
        
        AbstractJsonSchema sch = cache.get(schema);
        if (sch == null) {
            sch = cache.put(schema.read(this, object));
        }
        return sch;
    }
    
    @Override
    public JsonSchemaVersion getJsonSchemaVersion(JsonSchemaLocator locator) {
        final JsonValue schema;
        try {
            schema = locator.getSchema("/");
            if (JsonValue.ValueType.OBJECT == schema.getValueType()) {
                final JsonValue jversion = schema.asJsonObject().get(JsonSchema.SCHEMA);
                if (jversion != null && jversion.getValueType() == JsonValue.ValueType.STRING) {
                    try {
                        return JsonSchemaVersion.fromValue(((JsonString)jversion).getString());
                    } catch(IllegalArgumentException ex) {}
                }
            }
        } catch (IOException ex) {}
        
        final Object version = properties.get(JsonSchemaParserConfig.JSON_SCHEMA_VERSION);
        if (version instanceof JsonSchemaVersion v) {
            return v;
        }
        
        return JsonSchemaVersion.SCHEMA_DRAFT_07; // default
    }
}
