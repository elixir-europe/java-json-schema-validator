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
 * *****************************************************************************
 */

package es.elixir.bsc.json.schema.model.impl;

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonSchema;
import es.elixir.bsc.json.schema.model.JsonType;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.net.URI;

/**
 * The multiple types wrapper which is used when no type or a set of types is defined.
 * For instance, the wrapper is used when we have something like "type": ["string", "integer"].
 * It acts as "anyOf", but has it's own scope which is the same as the child elements.
 * 
 * @author Dmitry Repchevsky
 */

public class JsonMultitypeSchemaWrapper extends JsonAnyOfImpl<JsonObject> {
    
    private JsonSchemaLocator scope;
    private final JsonArray types;
    
    public JsonMultitypeSchemaWrapper(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer, JsonArray types) {
        super(parent, locator, jsonPointer);
        
        this.types = types;
    }

    @Override
    public JsonSchemaLocator getScope() {
        return scope;
    }
    
    @Override
    public JsonAnyOfImpl read(final JsonSubschemaParser parser,
                              final JsonObject object,
                              final JsonType type) throws JsonSchemaException {

        JsonValue $id = object.get(JsonSchema.ID);
        if ($id == null) {
            $id = object.get("id"); // draft4
        } 

        if ($id == null) {
            scope = locator;
        } else if ($id.getValueType() != JsonValue.ValueType.STRING) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                    "id", $id.getValueType().name(), JsonValue.ValueType.STRING.name()));
        } else {
            final String id = ((JsonString)$id).getString();
            try {
                scope = locator.resolve(URI.create(id));
                scope.setSchema(object);
            } catch(IllegalArgumentException ex) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_REFERENCE,
                                              new Object[] {id}));
            }
        }

        if (types == null) {
            for (JsonType val : JsonType.values()) {
                try {
                    final AbstractJsonSchema s = parser.parse(locator, this, getJsonPointer(), object, val);
                    if (s != null) {
                        add(s);
                    }
                } catch(JsonSchemaException ex) {
                    // do nothing
                }
            }
        } else {
            for (JsonValue val : types) {
                if (JsonValue.ValueType.STRING != val.getValueType()) {
                    
                }
                try {
                     final JsonType t = JsonType.fromValue(((JsonString)val).getString());
                     add(parser.parse(locator, parent, getJsonPointer(), object, t));
                } catch(IllegalArgumentException ex) {
                    throw new JsonSchemaException(
                        new ParsingError(ParsingMessage.UNKNOWN_OBJECT_TYPE, val));
                }
            }            
        }
        
        return this;
    }
}
