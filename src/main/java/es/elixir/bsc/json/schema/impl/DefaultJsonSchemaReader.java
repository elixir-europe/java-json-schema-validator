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
import es.elixir.bsc.json.schema.JsonSchemaReader;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.model.JsonSchema;
import es.elixir.bsc.json.schema.model.impl.AbstractJsonSchema;
import jakarta.json.JsonException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class DefaultJsonSchemaReader implements JsonSchemaReader {
    
    private final Map<URI, AbstractJsonSchema> schemas;
    private final Map<String, Object> properties;
    
    public DefaultJsonSchemaReader() {
        schemas = new HashMap<>();
        properties = new HashMap<>();
    }
    
    @Override
    public void setJsonSchemaParserProperty(String name, Object property) {
        properties.put(name, property);
    }
    
    @Override
    public JsonSchema read(URL url) throws JsonSchemaException {
        try {
            return read(new DefaultJsonSchemaLocator(url.toURI()));
        } catch (URISyntaxException ex) {
            throw new JsonSchemaException(
                    new ParsingError(ParsingMessage.INVALID_REFERENCE, url.toString()));
        }
    }
    
    @Override
    public JsonSchema read(JsonSchemaLocator locator) throws JsonSchemaException {
        AbstractJsonSchema schema = schemas.get(locator.uri);
        if (schema == null) {
            final JsonValue obj;
            try {
                obj = locator.getSchema("/");
            } catch (IOException ex) {
                throw new JsonSchemaException(
                        new ParsingError(ParsingMessage.UNRESOLVABLE_SCHEMA, locator.uri));
            } catch (JsonException ex) {
                throw new JsonSchemaException(
                        new ParsingError(ParsingMessage.JSON_PARSING_ERROR, ex.getMessage()));
            }
            schema = new DefaultJsonSchemaParser(properties).parse(locator, obj);
            schemas.put(locator.uri, schema);
        }
        return schema;
    }
}
