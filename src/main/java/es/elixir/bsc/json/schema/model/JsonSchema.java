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

package es.elixir.bsc.json.schema.model;

import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationException;
import java.util.List;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 * 
 */

public interface JsonSchema extends JsonSchemaElement {
    
    public final static String ID = "$id";
    public final static String SCHEMA = "$schema";
    public final static String DEFS = "$defs";
    public final static String ANCHOR = "$anchor";
    public final static String RECURSIVE_ANCHOR = "$recursiveAnchor";
    public final static String VOCABULARY = "$vocabulary";
    
    boolean validate(JsonValue value, List<ValidationError> errors, JsonSchemaValidationCallback<JsonValue> callback) throws ValidationException;

    default boolean validate(JsonValue value, List<ValidationError> errors) {
        return validate(value, errors, null);
    }
}
