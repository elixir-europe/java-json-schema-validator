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
 * *****************************************************************************
 */

package es.elixir.bsc.json.schema.model.impl;

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.JsonSchemaParser;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.model.JsonDependentProperties;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.StringArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * @author Dmitry Repchevsky
 */

public class JsonDependentPropertiesImpl extends AbstractJsonSchemaElement
        implements JsonDependentProperties {

    private final Map<String, StringArray> properties = new LinkedHashMap();
    
    public JsonDependentPropertiesImpl(AbstractJsonSchema parent, 
            JsonSchemaLocator locator, String jsonPointer) {
        super(parent, locator, jsonPointer);
    }
    
    @Override
    public <T extends JsonSchemaElement> Stream<T> getChildren() {
        return Stream.empty();
    }

    @Override
    public boolean contains(String name) {
        return properties.containsKey(name);
    }

    @Override
    public StringArray get(String name) {
        return properties.get(name);
    }

    @Override
    public StringArray put(String name, StringArray schema) {
        return properties.put(name, schema);
    }
    @Override
    public StringArray remove(String name) {
        return properties.remove(name);
    }

    @Override
    public Iterator<Entry<String, StringArray>> iterator() {
        return properties.entrySet().iterator();
    }
    
    public JsonDependentProperties read(JsonSchemaParser parser, JsonObject object) 
            throws JsonSchemaException {
        
        for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
            final String name = entry.getKey();
            final JsonValue value = entry.getValue();            
            if (JsonValue.ValueType.ARRAY != value.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_OBJECT_TYPE, 
                    name + " dependentRequired schema ", value.getValueType().name(), JsonValue.ValueType.ARRAY.name()));

            }
            
            final JsonStringArray arr = new JsonStringArray().read(value.asJsonArray());
 
            put(name, arr);
        }
        
        return this;
    }
}
