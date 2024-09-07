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
import es.elixir.bsc.json.schema.model.JsonStringSchema;
import java.util.List;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ValidationException;
import es.elixir.bsc.json.schema.impl.DefaultJsonStringFormatValidator;
import java.util.regex.Pattern;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonStringSchemaImpl extends PrimitiveSchemaImpl
                                  implements JsonStringSchema {
    
    private Long minLength;
    private Long maxLength;
    
    private String format;
    private Pattern pattern;

    public JsonStringSchemaImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator locator, String jsonPointer) {
        super(parent, locator, jsonPointer);
    }
    
    @Override
    public Long getMinLength() {
        return minLength;
    }
    
    @Override
    public void setMinLength(Long minLength) {
        this.minLength = minLength;
    }
    
    @Override
    public Long getMaxLength() {
        return maxLength;
    }
    
    @Override
    public void setMaxLength(Long maxLength) {
        this.maxLength = maxLength;
    }
    
    @Override
    public String getFormat() {
        return format;
    }
    
    @Override
    public String getPattern() {
        return pattern == null ? null : pattern.pattern();
    }

    @Override
    public void setPattern(String pattern) {
        this.pattern = pattern == null ? null : Pattern.compile(pattern);
        
    }
    
    @Override
    public void setFormat(String format) {
        this.format = format;
    }
    
    @Override
    public JsonStringSchemaImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {

        super.read(parser, object);
        
        final JsonNumber min = JsonSchemaUtil.check(object.getJsonNumber(MIN_LENGTH), JsonValue.ValueType.NUMBER);
        if (min != null) {
            minLength = min.longValue();
        }
        final JsonNumber max = JsonSchemaUtil.check(object.getJsonNumber(MAX_LENGTH), JsonValue.ValueType.NUMBER);
        if (max != null) {
            maxLength = max.longValue();
        }
        
        final JsonString jformat = JsonSchemaUtil.check(object.getJsonString(FORMAT), JsonValue.ValueType.STRING);
        if (jformat != null) {
            setFormat(jformat.getString());
        }

        final JsonString jpattern = JsonSchemaUtil.check(object.getJsonString(PATTERN), JsonValue.ValueType.STRING);
        if (jpattern != null) {
            setPattern(jpattern.getString());
        }

        return this;
    }
    
    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent,
            List evaluated, List<ValidationError> errors,
            JsonSchemaValidationCallback<JsonValue> callback) {
        
        if (value.getValueType() != JsonValue.ValueType.STRING) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.STRING_EXPECTED_MSG, value.getValueType().name()));
            return false;
        }
        
        final int nerrors = errors.size();
        
        validate(jsonPointer, ((JsonString)value).getString(), errors);
        
        super.validate(jsonPointer, value, parent, evaluated, errors, callback);
        
        if (callback != null) {
            callback.validated(this, jsonPointer, value, parent, errors);
        }
        
        return nerrors == errors.size();
    }
    
    private void validate(String jsonPointer, String string, List<ValidationError> errors) {
        
        if (minLength != null && string.codePointCount(0, string.length()) < minLength) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.STRING_MIN_LENGTH_CONSTRAINT_MSG, string.length(), minLength));
        }
        
        if (maxLength != null && string.codePointCount(0, string.length()) > maxLength) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.STRING_MAX_LENGTH_CONSTRAINT_MSG, string.length(), maxLength));

        }
        
        if (pattern != null && !pattern.matcher(string).find()) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.STRING_PATTERN_CONSTRAINT_MSG, pattern, string));
            
        }
        
        if (format != null && !format.isEmpty()) {
            try {
                DefaultJsonStringFormatValidator.validate(jsonPointer, this, string);
            } catch (ValidationException ex) {
                errors.add(ex.error);
            }
        }        
    }
}
