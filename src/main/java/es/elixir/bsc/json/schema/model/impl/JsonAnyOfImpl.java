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

package es.elixir.bsc.json.schema.model.impl;

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.model.JsonAnyOf;
import java.util.ArrayList;
import java.util.List;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.ValidationException;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonType;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonAnyOfImpl extends SchemaArrayImpl
                           implements JsonAnyOf<AbstractJsonSchema> {
    
    private JsonArray types;
    
    public JsonAnyOfImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }
    
    public JsonAnyOfImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer, JsonArray types) {
        super(parent, locator, jsonPointer);
        
        this.types = types;
    }
    
    @Override
    public JsonAnyOfImpl read(final JsonSubschemaParser parser,
                              final JsonValue value,
                              final JsonType type) throws JsonSchemaException {
        
        if (types == null) {
            for (JsonType val : JsonType.values()) {
                try {
                    final AbstractJsonSchema s = parser.parse(getScope(), this, getJsonPointer(), value, val);
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
                     add(parser.parse(getScope(), parent, getJsonPointer(), value, t));
                } catch(IllegalArgumentException ex) {
                    throw new JsonSchemaException(
                        new ParsingError(ParsingMessage.UNKNOWN_OBJECT_TYPE, val));
                }
            }            
        }        
        return this;
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List evaluated, List errors, JsonSchemaValidationCallback callback) 
            throws ValidationException {
        
        final List<ValidationError> err = new ArrayList<>();
        final List eva = new ArrayList();
        
        // have to evaluate all schemas to collect evaluated properties
        boolean match = false;
        for (AbstractJsonSchema schema : this) {
            final List e = new ArrayList(evaluated);
            if (schema.validate(jsonPointer, value, parent, e, err, callback)) {
                e.removeAll(eva);
                eva.addAll(e);
                match = true; // found the schema that matches
            }
        }

        if (match) {
            eva.removeAll(evaluated);
            evaluated.addAll(eva);
        } else {
            errors.addAll(err);
            errors.add(new ValidationError(getId(), getJsonPointer(), 
                    jsonPointer, ValidationMessage.OBJECT_ANY_OF_CONSTRAINT_MSG));
        }
        
        return match;
    }
}
