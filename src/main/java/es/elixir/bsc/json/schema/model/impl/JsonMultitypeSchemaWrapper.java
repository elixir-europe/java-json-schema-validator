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
import es.elixir.bsc.json.schema.model.JsonArraySchema;
import es.elixir.bsc.json.schema.model.JsonObjectSchema;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.JsonType;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The multiple types wrapper which is used when no type or a set of types is defined.
 * For instance, the wrapper is used when we have something like "type": ["string", "integer"].
 * It acts as "anyOf", but has it's own scope which is the same as the child elements.
 * 
 * @author Dmitry Repchevsky
 */

public class JsonMultitypeSchemaWrapper extends JsonAnyOfImpl<JsonObject> {
    
    private final JsonArray types;
    
    public JsonMultitypeSchemaWrapper(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator scope, JsonSchemaLocator locator, String jsonPointer,
            JsonArray types) {
        super(parent, scope, locator, jsonPointer);
        
        this.types = types;
    }

    @Override
    public Stream<AbstractJsonSchemaElement> getChildren() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED),false)
                .filter(s -> s instanceof JsonObjectSchema || s instanceof JsonArraySchema)
                .map(this::clone)
                .map(c -> c.setParent(this))
                .flatMap(JsonSchemaElement::getChildren);
    }
    
    @Override
    public JsonAnyOfImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {

        if (types == null) {
            for (JsonType val : JsonType.values()) {
                try {
                    final AbstractJsonSchema s = parser.parse(scope, this, getJsonPointer(), object, val);
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
                     add(parser.parse(scope, this, getJsonPointer(), object, t));
                } catch(IllegalArgumentException ex) {
                    throw new JsonSchemaException(
                        new ParsingError(ParsingMessage.UNKNOWN_OBJECT_TYPE, val));
                }
            }            
        }
        
        return this;
    }
}
