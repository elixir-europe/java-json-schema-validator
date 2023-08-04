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
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationException;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonSchema;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.JsonType;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.JsonValue;

/**
 * This interface is used internally and exists only for the purpose to hide
 * actual validate() method from the outside.
 * 
 * @author Dmitry Repchevsky
 * 
 * @param <T> the Json type accepted by the reader to parse this schema element.
 */

public interface AbstractJsonSchema<T extends JsonValue> extends JsonSchema {

    boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List<String> evaluated, List<ValidationError> errors, 
            JsonSchemaValidationCallback<JsonValue> callback) 
            throws ValidationException;

    @Override
    default void validate(JsonValue value, List<ValidationError> errors, 
            JsonSchemaValidationCallback<JsonValue> callback) 
            throws ValidationException {
        validate("", value, null, new ArrayList(), errors, callback);
    }
    
    AbstractJsonSchema read(JsonSubschemaParser parser, JsonSchemaLocator locator, 
            JsonSchemaElement parent, String jsonPointer, T schema, JsonType type) 
            throws JsonSchemaException;
}
