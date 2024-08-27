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

import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import java.net.URI;

/**
 * This is an root class that any JSON Schema element inherits from.
 * It contains minimum properties to identify and locate the element in 
 * parsed JSON Schema tree.
 * 
 * @author Dmitry Repchevsky
 */

public abstract class AbstractJsonSchemaElement implements JsonSchemaElement {
    
    public final AbstractJsonSchemaElement parent;
    
    public final JsonSchemaLocator scope;
    public final JsonSchemaLocator locator;
    public final String jsonPointer;
    
    /**
     * Constructor of the object.
     * It only sets an essential properties to identify and locate the element in
     * the JSON Schema.
     * 
     * @param parent a parent element that encloses this one
     * @param scope current element scope (may or may not be equal to the location)
     * @param locator the locator that was used to load this document
     * @param jsonPointer JSON Pointer to the parsed JSON Value that represents this element.
     */
    public AbstractJsonSchemaElement(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator scope, JsonSchemaLocator locator, String jsonPointer) {

        this.parent = parent;

        this.scope = scope;
        this.locator = locator;
        this.jsonPointer = jsonPointer.startsWith("//") ? jsonPointer.substring(1) : jsonPointer;
    }

    @Override
    public final URI getId() {
        return scope.uri;
    }

    @Override
    public String getJsonPointer() {
        // when scope != locator (new scope) jsonPointer is 'root'
        return scope == locator ? jsonPointer : "/";
    }

    @Override
    public AbstractJsonSchemaElement getParent() {
        return parent;
    }
}
