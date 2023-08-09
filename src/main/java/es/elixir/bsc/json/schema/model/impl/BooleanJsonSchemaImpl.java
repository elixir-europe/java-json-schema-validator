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
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationException;
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.BooleanJsonSchema;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.JsonType;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import java.util.List;

/**
 * @author Dmitry Repchevsky
 */

public class BooleanJsonSchemaImpl extends JsonSchemaImpl<JsonValue>
        implements BooleanJsonSchema {

    private boolean evaluation;
    
    @Override
    public BooleanJsonSchemaImpl read(final JsonSubschemaParser parser, 
                                      final JsonSchemaLocator locator,
                                      final JsonSchemaElement parent,
                                      final String jsonPointer, 
                                      final JsonValue schema,
                                      final JsonType type) throws JsonSchemaException {

        super.read(parser, locator, parent, jsonPointer, schema, type);

        if (schema.getValueType() != ValueType.TRUE &&
            schema.getValueType() != ValueType.FALSE) {
            throw new JsonSchemaException(new ParsingError(ParsingMessage.SCHEMA_OBJECT_ERROR, 
                   new Object[] {schema.getValueType()}));
        }
        
        evaluation = schema.getValueType() == ValueType.TRUE;
        
        return this;
    }
    
    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, List evaluated, List<ValidationError> errors, JsonSchemaValidationCallback<JsonValue> callback) throws ValidationException {
        if (!evaluation) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.UNEVALUATED_BOOLEAN_SCHEMA_MSG));
        }
        return evaluation;
    }
}
