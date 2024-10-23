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

package es.elixir.bsc.json.schema;

import es.elixir.bsc.json.schema.model.JsonSchema;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Dmitry Repchevsky
 */

public interface JsonSchemaReader {
    
    /**
     * Read JSON Schema located by the provided URL.
     * The reader uses a new instance of JsonSchemaLocator.
     * 
     * @param url the URL to read a JSON Schema from.
     * @return parsed JsonSchema object
     * 
     * @throws JsonSchemaException 
     */
    JsonSchema read(URL url) throws JsonSchemaException;
    
    /**
     * Read JSON Schema located by the provided JsonSchemaLocator.
     * This allows reuse the same JsonSchemaLocator instance between calls.
     * 
     * @param locator JsonSchemaLocator to locate parsed JSON Schema.
     * @return parsed JsonSchema object
     * 
     * @throws JsonSchemaException 
     */    
    JsonSchema read(JsonSchemaLocator locator) throws JsonSchemaException;
    
    /**
     * Creates new instance of default implementation of the JsonSchemaLocator.
     * 
     * @param uri the JSON Schema location URI
     * 
     * @return JsonSchemaLocator
     */
    JsonSchemaLocator getJsonSchemaLocator(URI uri);

    /**
     * Set configuration property for this JsonSchemaReader.
     * 
     * @param name configuration property name
     * @param property configuration property value
     */
    void setJsonSchemaParserProperty(String name, Object property);
        
    /**
     * Create JsonSchemaReader with no configuration parameters.
     * 
     * @return new instance of the JsonSchemaReader
     */
    static JsonSchemaReader getReader() {
        return getReader(Collections.EMPTY_MAP);
    }
    
    /**
     * Create JsonSchemaReader with provided configuration properties.
     * 
     * <pre>
     * example:
     * {@code
     * JsonSchemaParserConfig config = 
     *     new JsonSchemaParserConfig()
     *         .setJsonSchemaVersion(JsonSchemaVersion.SCHEMA_DRAFT_2020_12);
     * JsonSchemaReader reader = JsonSchemaReader.getReader(config);
     * }</pre>
     * 
     * @param config the map of configuration properties
     * 
     * @return new instance of the JsonSchemaReader
     */
    static JsonSchemaReader getReader(Map<String, Object> config) {
        ServiceLoader<JsonSchemaReader> loader = ServiceLoader.load(JsonSchemaReader.class);
        Iterator<JsonSchemaReader> iterator = loader.iterator();

        if (iterator.hasNext()) {
            final JsonSchemaReader reader = iterator.next();
            for (Map.Entry<String, Object> p : config.entrySet()) {
                reader.setJsonSchemaParserProperty(p.getKey(), p.getValue());
            }
            return reader;
        }
        return null;
    }
}
