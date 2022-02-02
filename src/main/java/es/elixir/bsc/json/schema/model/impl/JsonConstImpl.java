/**
 * *****************************************************************************
 * Copyright (C) 2022 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonConst;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.JsonType;
import java.util.List;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonConstImpl extends PrimitiveSchemaImpl implements JsonConst {

    private JsonValue value;
    
    @Override
    public JsonValue getValue() {
        return value;
    }

    @Override
    public void setValue(JsonValue value) {
        this.value = value;
    }
    
    @Override
    public JsonConstImpl read(final JsonSubschemaParser parser, 
                             final JsonSchemaLocator locator,
                             final JsonSchemaElement parent,
                             final String jsonPointer, 
                             final JsonObject object,
                             final JsonType type) throws JsonSchemaException {

        super.read(parser, locator, parent, jsonPointer, object, type);
        
        value = object.get(CONST);
        
        return this;
    }

    @Override
    public void validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List<ValidationError> errors, JsonSchemaValidationCallback<JsonValue> callback) {
        
        if (this.value == null || !this.value.equals(value)) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.CONST_CONSTRAINT_MSG, value.toString(), 
                    this.value == null ? "" : this.value.toString()));
        }
        
        super.validate(jsonPointer, value, parent, errors, callback);
    }
}