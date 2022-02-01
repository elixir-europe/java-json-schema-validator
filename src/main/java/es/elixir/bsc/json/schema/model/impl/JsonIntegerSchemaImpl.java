/**
 * *****************************************************************************
 * Copyright (C) 2021 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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
import es.elixir.bsc.json.schema.model.JsonIntegerSchema;
import static es.elixir.bsc.json.schema.model.NumericSchema.MAXIMUM;
import static es.elixir.bsc.json.schema.model.NumericSchema.MINIMUM;
import java.math.BigInteger;
import java.util.List;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.model.JsonType;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;

/**
 * Json Schema implementation for the Json Integer type.
 * 
 * @author Dmitry Rerpchevsky
 */

public class JsonIntegerSchemaImpl extends NumericSchemaImpl<BigInteger>
                                   implements JsonIntegerSchema {
    
    @Override
    public JsonIntegerSchemaImpl read(final JsonSubschemaParser parser, 
                                      final JsonSchemaLocator locator,
                                      final JsonSchemaElement parent,
                                      final String jsonPointer, 
                                      final JsonObject object,
                                      final JsonType type) throws JsonSchemaException {
        
        super.read(parser, locator, parent, jsonPointer, object, type);

        final JsonNumber min = JsonSchemaUtil.check(object.getJsonNumber(MINIMUM), JsonValue.ValueType.NUMBER);
        if (min != null) {
            minimum = min.bigIntegerValue();
        }
        
        final JsonNumber max = JsonSchemaUtil.check(object.getJsonNumber(MAXIMUM), JsonValue.ValueType.NUMBER);
        if (max != null) {
            maximum = max.bigIntegerValue();
        }
        
        return this;
    }

    @Override
    public void validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List<ValidationError> errors, JsonSchemaValidationCallback <JsonValue>callback) {

        if (value.getValueType() != JsonValue.ValueType.NUMBER) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.NUMBER_EXPECTED_MSG, value.getValueType().name()));
            return;
        }

        validate(jsonPointer, ((JsonNumber)value).bigIntegerValue(), errors);
        
        super.validate(jsonPointer, value, parent, errors, callback);

        if (callback != null) {
            callback.validated(this, jsonPointer, value, parent, errors);
        }
    }
    
    public void validate(String jsonPointer, BigInteger num, List<ValidationError> errors) {
        if (minimum != null) {
            if (exclusiveMinimum != null && exclusiveMinimum) {
                if (num.compareTo(minimum) <= 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MIN_CONSTRAINT_MSG, num, "<=", minimum));
                }
            } else if (num.compareTo(minimum) < 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MIN_CONSTRAINT_MSG, num, "<", minimum));
            }
        }
        
        if (maximum != null) {
            if (exclusiveMaximum != null && exclusiveMaximum) {
                if (num.compareTo(maximum) >= 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MAX_CONSTRAINT_MSG, num, ">=", maximum));
                }
            } else if (num.compareTo(maximum) > 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MAX_CONSTRAINT_MSG, num, ">", maximum));
            }
        }
    }
}