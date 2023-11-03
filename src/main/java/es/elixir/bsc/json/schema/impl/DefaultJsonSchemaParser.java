/**
 * *****************************************************************************
 * Copyright (C) 2023 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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
import es.elixir.bsc.json.schema.model.impl.JsonAnyOfImpl;
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
import java.util.Map;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * @author Dmitry Repchevsky
 */

public class DefaultJsonSchemaParser implements JsonSubschemaParser {
    
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

        if (value.getValueType() == ValueType.TRUE ||
            value.getValueType() == ValueType.FALSE) {
            return new BooleanJsonSchemaImpl(parent, locator, jsonPointer).read(this, value, type);
        }

        if (value.getValueType() != ValueType.OBJECT) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.SCHEMA_OBJECT_ERROR, 
                   new Object[] {value.getValueType()}));
        }

        final JsonObject object = value.asJsonObject();
        
        final JsonValue jref = object.get(JsonReference.REF);
        if (jref != null) {
            if (JsonValue.ValueType.STRING != jref.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                       new Object[] {JsonReference.REF, jref.getValueType().name(), JsonValue.ValueType.STRING.name()}));
            }
            
            // before draft 2019-09 $ref ignored any other properties
            if (JsonSchemaVersion.SCHEMA_DRAFT_2019_09.compareTo(getJsonSchemaVersion(object)) > 0) {
                return new JsonReferenceImpl(parent, locator, jsonPointer).read(this, object, null);
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
                        throw new JsonSchemaException(new ParsingError(ParsingMessage.UNKNOWN_OBJECT_TYPE, 
                            new Object[] {((JsonString)type_value).getString()}));
                    }
                case ARRAY: break;
                default: 
                    throw new JsonSchemaException(new ParsingError(
                            ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                            new Object[] {"type", type_value.getValueType().name(), 
                                          "either a string or an array"}));
            }
        }
        
        final JsonArray jenum = JsonSchemaUtil.check(object.get(ENUM), JsonValue.ValueType.ARRAY);
        if (jenum != null) {
            if (jenum.isEmpty()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.EMPTY_ENUM));
            }
            return new JsonEnumImpl(parent, locator, jsonPointer).read(this, object, type);
        }

        final JsonValue jconst = object.get(CONST);
        if (jconst != null) {
            return new JsonConstImpl(parent, locator, jsonPointer).read(this, object, type);
        }

        if (type == null) {
            return new JsonAnyOfImpl(parent, locator, jsonPointer, vtype == ValueType.ARRAY ? type_value.asJsonArray() : null)
                    .read(this, object, null);
        }

        final AbstractJsonSchema schema;
        switch(type) {
            case OBJECT: schema = new JsonObjectSchemaImpl(parent, locator, jsonPointer).read(this, object, type); break;
            case ARRAY: schema = new JsonArraySchemaImpl(parent, locator, jsonPointer).read(this, object, type); break;
            case STRING: schema = new JsonStringSchemaImpl(parent, locator, jsonPointer).read(this, object, type); break;
            case NUMBER: schema = new JsonNumberSchemaImpl(parent, locator, jsonPointer).read(this, object, type); break;
            case INTEGER: schema = new JsonIntegerSchemaImpl(parent, locator, jsonPointer).read(this, object, type); break;
            case BOOLEAN: schema = new JsonBooleanSchemaImpl(parent, locator, jsonPointer).read(this, object, type); break;
            case NULL: schema = new JsonNullSchemaImpl(parent, locator, jsonPointer).read(this, object, type); break;
            default: return null;
        }

        return schema;
    }
    
    @Override
    public JsonSchemaVersion getJsonSchemaVersion(JsonObject object) {
        final JsonValue jversion = object.get(JsonSchema.SCHEMA);
        if (jversion != null && jversion.getValueType() == JsonValue.ValueType.STRING) {
            try {
                return JsonSchemaVersion.fromValue(((JsonString)jversion).getString());
            } catch(IllegalArgumentException ex) {}
        }
        
        final Object version = properties.get(JsonSchemaParserConfig.JSON_SCHEMA_VERSION);
        if (version instanceof JsonSchemaVersion) {
            return (JsonSchemaVersion)version;
        }
        
        return JsonSchemaVersion.SCHEMA_DRAFT_07; // default
    }
}
