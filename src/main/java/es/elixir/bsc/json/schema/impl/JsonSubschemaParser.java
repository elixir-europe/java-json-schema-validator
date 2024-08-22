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

package es.elixir.bsc.json.schema.impl;

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.JsonSchemaParser;
import es.elixir.bsc.json.schema.JsonSchemaVersion;
import es.elixir.bsc.json.schema.model.JsonType;
import es.elixir.bsc.json.schema.model.impl.AbstractJsonSchema;
import es.elixir.bsc.json.schema.model.impl.AbstractJsonSchemaElement;
import java.util.Map;
import javax.json.JsonValue;

/**
 * <p>
 * JSON Subschema parser interface with optional JsonType parameter.
 * </p>
 * 
 * The parse() method allows to pass an optional JSON Type which may be used by the parser 
 * to parse schema elements with no type defined (as in 'oneOf' or 'anyOf').
 * 
 * @author Dmitry Repchevsky
 */

public interface JsonSubschemaParser extends JsonSchemaParser {
    
    /**
     * Get JsonSchemaParser configuration properties
     * 
     * @return the map of configuration properties;
     */
    Map<String, Object> getJsonSchemaParserProperties();
    
    JsonSchemaVersion getJsonSchemaVersion(JsonSchemaLocator locator);

    @Override
    default AbstractJsonSchema parse(JsonSchemaLocator locator, JsonValue schema)
            throws JsonSchemaException {

        return parse(locator, null, "/", schema, null);
    }
    
    <T extends AbstractJsonSchema> T parse(JsonSchemaLocator locator, AbstractJsonSchemaElement parent, String jsonPointer, JsonValue schema, JsonType type) throws JsonSchemaException;
}
