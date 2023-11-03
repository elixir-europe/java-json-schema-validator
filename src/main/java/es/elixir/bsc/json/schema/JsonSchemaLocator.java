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

package es.elixir.bsc.json.schema;

import java.io.IOException;
import java.net.URI;
import javax.json.JsonException;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */
    
public abstract class JsonSchemaLocator {
   
    public final URI uri;
    
    public JsonSchemaLocator(URI uri) {
        this.uri = uri;
    }
    
    /**
     * Set Json Schema Object for this location.
     * There are cases were the location URI doesn't resolve the schema, but
     * is used as an identifier ('id' or '$id").
     * 
     * @param schema the schema to associate with this locator (URI).
     */
    public abstract void setSchema(JsonValue schema);

    /**
     * Get Json Schema Object associated with this locator.
     * 
     * @param jsonPointer Json Pointer that points to the (sub)schema ("/" for the root)
     * @return Json Object that corresponds to the Json Schema found by this locator
     * 
     * @throws IOException
     * @throws JsonException 
     */
    public abstract JsonValue getSchema(String jsonPointer)
            throws IOException, JsonException;

    /**
     * Get Json Schema Object found by this and all related locators.
     * 
     * @param uri URI of the locator (not necessary resolvable URI location)
     * @param jsonPointer Json Pointer that points to the (sub)schema ("/" for the root)
     * 
     * @return found Json Schema Object or null
     * 
     * @throws IOException
     * @throws JsonException 
     */
    public abstract JsonValue getSchema(URI uri, String jsonPointer)
            throws IOException, JsonException;

    /**
     * Create a new locator derived from this one.
     * 
     * @param uri the URI to be resolved in a context of this locator.
     * @return new locator that is able to return Json Schema.
     */
    public abstract JsonSchemaLocator resolve(URI uri);
}
