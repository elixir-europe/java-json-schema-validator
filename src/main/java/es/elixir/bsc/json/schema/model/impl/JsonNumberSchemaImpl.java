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
import es.elixir.bsc.json.schema.model.JsonNumberSchema;
import java.math.BigDecimal;
import java.util.List;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.model.JsonType;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonNumberSchemaImpl extends NumericSchemaImpl<BigDecimal>
                                  implements JsonNumberSchema {

    public JsonNumberSchemaImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public JsonNumberSchemaImpl read(final JsonSubschemaParser parser,
                                     final JsonObject object,
                                     final JsonType type) throws JsonSchemaException {

        super.read(parser, object, type);
        
        final JsonNumber min = JsonSchemaUtil.check(object.getJsonNumber(MINIMUM), JsonValue.ValueType.NUMBER);
        if (min != null) {
            minimum = min.bigDecimalValue();
        }
        
        final JsonNumber max = JsonSchemaUtil.check(object.getJsonNumber(MAXIMUM), JsonValue.ValueType.NUMBER);
        if (max != null) {
            maximum = max.bigDecimalValue();
        }
        
        return this;
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List evaluated, List<ValidationError> errors,
            JsonSchemaValidationCallback<JsonValue> callback) {

        if (value.getValueType() != JsonValue.ValueType.NUMBER) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.NUMBER_EXPECTED_MSG, value.getValueType().name()));
            return false;
        }
        
        final int nerrors = errors.size();
        
        validate(jsonPointer, ((JsonNumber)value).bigDecimalValue(), errors);

        super.validate(jsonPointer, value, parent, evaluated, errors, callback);

        if (callback != null) {
            callback.validated(this, jsonPointer, value, parent, errors);
        }
        
        return nerrors == errors.size();
    }
    
    private void validate(String jsonPointer, BigDecimal dec, List<ValidationError> errors) {

        if (minimum != null) {
            if (isExclusiveMinimum != null && isExclusiveMinimum) {
                if (dec.compareTo(minimum) <= 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MIN_CONSTRAINT_MSG, dec, "<=", minimum));
                }
            } else if (dec.compareTo(minimum) < 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MIN_CONSTRAINT_MSG, dec, "<", minimum));
            }
        }
        
        if (maximum != null) {
            if (isExclusiveMaximum != null && isExclusiveMaximum) {
                if (dec.compareTo(maximum) >= 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MAX_CONSTRAINT_MSG, dec.toPlainString(), ">=", maximum));
                }
            } else if (dec.compareTo(maximum) > 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.NUMBER_MAX_CONSTRAINT_MSG, dec.toPlainString(), ">", maximum));
            }
        }
        
        if (exclusiveMinimum != null && dec.compareTo(BigDecimal.valueOf(exclusiveMinimum.doubleValue())) <= 0) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.NUMBER_MIN_CONSTRAINT_MSG, dec, "<=", exclusiveMinimum));
        }

        if (exclusiveMaximum != null && dec.compareTo(BigDecimal.valueOf(exclusiveMaximum.doubleValue())) >= 0) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.NUMBER_MAX_CONSTRAINT_MSG, dec, ">=", exclusiveMaximum));
        }

        if (multipleOf != null && dec.divideAndRemainder(multipleOf)[1].compareTo(BigDecimal.ZERO) != 0) {
                errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.NUMBER_MULTIPLE_OF_CONSTRAINT_MSG, dec, multipleOf));
        }
    }
}
