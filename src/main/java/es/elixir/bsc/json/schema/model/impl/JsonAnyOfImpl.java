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

package es.elixir.bsc.json.schema.model.impl;

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.model.JsonAnyOf;
import java.util.ArrayList;
import java.util.List;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ValidationException;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonType;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 * 
 * @param <T>
 */

public class JsonAnyOfImpl<T extends JsonValue> extends SchemaArrayImpl<T>
                           implements JsonAnyOf<AbstractJsonSchema> {
    
    public JsonAnyOfImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }
    
    @Override
    public JsonAnyOfImpl read(final JsonSubschemaParser parser,
                              final T value,
                              final JsonType type) throws JsonSchemaException {
        super.read(parser, value, type);
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
