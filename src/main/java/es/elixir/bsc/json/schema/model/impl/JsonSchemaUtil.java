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
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public interface JsonSchemaUtil {
    
    static boolean compare(JsonValue value1, JsonValue value2) throws JsonSchemaException {
        return value1 == null ? value2 == null : 
               value1.getValueType() == value2.getValueType() &&
               value1.equals(value2);
    }
    
    static <U extends JsonValue> U check(JsonValue value, JsonValue.ValueType type) throws JsonSchemaException {
        return value == null ? null : require(value, type);
    }
    
    static <U extends JsonValue> U require(JsonValue value, JsonValue.ValueType type) throws JsonSchemaException {
        if (value.getValueType() != type) {
            throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                    "type", value.getValueType().name(), "either a string or an array"));
        }
        return (U)value;
    }    
}
