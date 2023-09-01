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
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonProperties;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonPropertiesImpl extends LinkedHashMap<String, AbstractJsonSchema>
                                implements JsonProperties<AbstractJsonSchema> {


    @Override
    public boolean contains(String name) {
        return super.containsKey(name);
    }
    
    @Override
    public AbstractJsonSchema get(String name) {
        return super.get(name);
    }

    @Override
    public AbstractJsonSchema put(String name, AbstractJsonSchema schema) {
        return super.put(name, schema);
    }
    
    @Override
    public AbstractJsonSchema remove(String name) {
        return super.remove(name);
    }
    
    @Override
    public Iterator<Entry<String, AbstractJsonSchema>> iterator() {
        return entrySet().iterator();
    }
    
    public JsonPropertiesImpl read(final JsonSubschemaParser parser, 
                                   final JsonSchemaLocator locator, 
                                   final JsonSchemaImpl parent,
                                   final String jsonPointer,
                                   final JsonObject object) throws JsonSchemaException {

        for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
            final JsonValue value = entry.getValue();
            final AbstractJsonSchema schema = parser.parse(locator, parent, jsonPointer + "/" + entry.getKey(), value, null);
            put(entry.getKey(), schema);
        }
        
        return this;
    }
}
