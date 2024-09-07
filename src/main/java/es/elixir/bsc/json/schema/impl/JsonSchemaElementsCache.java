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

import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.model.JsonReference;
import es.elixir.bsc.json.schema.model.impl.AbstractJsonSchema;
import es.elixir.bsc.json.schema.model.impl.AbstractJsonSchemaElement;
import es.elixir.bsc.json.schema.model.impl.JsonMultitypeSchemaWrapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple HashMap based elements storage implementation.
 * 
 * The cache doesn't store JsonMultitypeSchemaWrapper's children because all 
 * they have the same $id as a 'wrapper' an thus cached together.
 * 
 * @author Dmitry Repchevsky
 */

public class JsonSchemaElementsCache {
    private final Map<URI, AbstractJsonSchema> cache = new HashMap();
    
    /**
     * Get previously parsed JSON (sub)schema.
     * 
     * @param locator the locator to the JSON schema document
     * @param jsonPointer JSON pointer to the (sub)schema ("/" for the root)
     * 
     * @return either found JSON schema or null if not found
     */
    public AbstractJsonSchema get(JsonSchemaLocator locator, String jsonPointer) {
        return cache.get(resolve(locator.uri, jsonPointer));
    }
    
    /**
     * Get previously parsed JSON (sub)schema.
     * 
     * @param schema JSON (sub)schema template (actually we need an $id of it)
     * 
     * @return either found JSON schema or null if not found
     */
    public AbstractJsonSchema get(AbstractJsonSchema schema) {
        return schema.getParent() instanceof JsonMultitypeSchemaWrapper ? null : cache.get(schema.getId());
    }
    
    /**
     * Put the JSON (sub)schema into the cache.
     * 
     * @param schema (sub)schema to be put
     * 
     * @return the same document we passed to the method
     */
    public AbstractJsonSchema put(AbstractJsonSchema schema) {
        if (!schema.isDynamicScope() &&
            !(schema.getParent() instanceof JsonMultitypeSchemaWrapper)) {
            final URI id = schema.getId();
            cache.put(id, schema);
            
            // syntheticId is a real document path which might differ 
            // from the contextual $id
            final URI syntheticId = getSyntheticId(schema);
            if (!id.equals(syntheticId)) {
                cache.put(syntheticId, schema);
            }
        }
        return schema;
    }
    
    /**
     * Unlike {@code JsonSchemaElement.getId()}, synthetic identifier is
     * relative to the document root.
     * <pre>
     * Example:
     *   Both URI point to the same 'items' element.
     *   While first one is the Json Schema $id, second is a JSON pointer in the
     *   'scope_change_defs2.json' JSON document.
     *   'http://localhost:1234/draft2019-09/baseUriChangeFolderInSubschema/#/$defs/bar/items'
     *   'http://localhost:1234/draft2019-09/scope_change_defs2.json#/$defs/baz/$defs/bar/items'
     * </pre>
     * @return synthetic identifier used to resolve external $refs.
     */
    private URI getSyntheticId(AbstractJsonSchemaElement e) {
        final StringBuilder sb = new StringBuilder(e.jsonPointer);
        while (e.getParent() != null && !(e.getParent() instanceof JsonReference)) {
            e = e.getParent();
            if (!(e instanceof JsonMultitypeSchemaWrapper) &&
                e.jsonPointer != e.getJsonPointer() &&
                e.jsonPointer.length() > 1) {
                sb.insert(0, e.jsonPointer);
            }
        }
        return resolve(e.locator.uri, sb.toString());
    }
    
    /**
     * Resolves identifier with JSON pointer fragment part.
     * 
     * @param uri schema identifier
     * @param jsonPointer JSON pointer to the schema element
     * 
     * @return schema element identifier
     */
    private URI resolve(URI uri, String jsonPointer) {
        final String fragment = uri.getFragment();
        try {
            return new URI(uri.getScheme(), uri.getSchemeSpecificPart(), 
                    fragment == null && jsonPointer.length() > 1 ? jsonPointer : jsonPointer.length() > 1 
                            ? fragment + jsonPointer : fragment);
        } catch (URISyntaxException ex) {}
        return null;
    }
}
