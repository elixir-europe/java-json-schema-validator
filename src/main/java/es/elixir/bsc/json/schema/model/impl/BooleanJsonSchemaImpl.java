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
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Dmitry Repchevsky
 */

public class BooleanJsonSchemaImpl extends AbstractJsonSchema<JsonValue>
        implements BooleanJsonSchema {

    private boolean evaluation;
    
    public BooleanJsonSchemaImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }
    
    @Override
    public Stream<JsonSchemaElement> getChildren() {
        return Stream.of();
    }

    @Override
    public BooleanJsonSchemaImpl read(final JsonSubschemaParser parser,
                                      final JsonValue schema,
                                      final JsonType type) throws JsonSchemaException {

        if (schema.getValueType() != ValueType.TRUE &&
            schema.getValueType() != ValueType.FALSE) {
            throw new JsonSchemaException(new ParsingError(
                    ParsingMessage.SCHEMA_OBJECT_ERROR, schema.getValueType()));
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
