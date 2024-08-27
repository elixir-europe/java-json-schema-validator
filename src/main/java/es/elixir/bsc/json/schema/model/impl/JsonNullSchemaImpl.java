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
import es.elixir.bsc.json.schema.model.JsonNullSchema;
import java.util.List;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonNullSchemaImpl extends PrimitiveSchemaImpl
                                implements JsonNullSchema {

    public JsonNullSchemaImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator scope, JsonSchemaLocator locator, String jsonPointer) {
        super(parent, scope, locator, jsonPointer);
    }

    @Override
    public JsonNullSchemaImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {

        super.read(parser, object);

        return this;
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent,
            List evaluated, List<ValidationError> errors,
            JsonSchemaValidationCallback<JsonValue> callback) {
        
        if (JsonValue.NULL.getValueType() != value.getValueType()) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.NULL_EXPECTED_MSG, value.getValueType().name()));
            return false;
        }

        final int nerrors = errors.size();
        
        super.validate(jsonPointer, value, parent, evaluated, errors, callback);
        
        if (callback != null) {
            callback.validated(this, jsonPointer, value, parent, errors);
        }
        
        return nerrors == errors.size();
    }
}
