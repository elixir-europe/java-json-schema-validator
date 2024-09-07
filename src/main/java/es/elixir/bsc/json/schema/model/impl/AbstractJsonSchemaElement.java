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

import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This is an root class that any JSON Schema element inherits from.
 * It contains minimum properties to identify and locate the element in 
 * parsed JSON Schema tree.
 * 
 * @author Dmitry Repchevsky
 */

public abstract class AbstractJsonSchemaElement 
        implements JsonSchemaElement, Cloneable {
    
    private AbstractJsonSchemaElement parent;
    
    public final JsonSchemaLocator locator;
    public final String jsonPointer;
    
    private boolean isDynamicScope;
    
    /**
     * Constructor of the object.
     * It only sets an essential properties to identify and locate the element in
     * the JSON Schema.
     * 
     * @param parent a parent element that encloses this one
     * @param locator current element scope (may or may not be equal to the location)
     * @param jsonPointer JSON Pointer to the parsed JSON Value that represents this element.
     */
    public AbstractJsonSchemaElement(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator locator, String jsonPointer) {

        this.parent = parent;

        this.locator = locator;
        this.jsonPointer = jsonPointer.startsWith("//") ? jsonPointer.substring(1) : jsonPointer;
    }

    @Override
    public final URI getId() {
        final String pointer = getJsonPointer();
        final String fragment = locator.uri.getFragment();
        try {
            return new URI(locator.uri.getScheme(), locator.uri.getSchemeSpecificPart(), 
                    fragment == null && pointer.length() > 1 ? pointer : pointer.length() > 1 
                            ? fragment + pointer : fragment);
        } catch (URISyntaxException ex) {}
        return null;
    }

    @Override
    public String getJsonPointer() {
        if (parent instanceof JsonMultitypeSchemaWrapper) {
            return parent.getJsonPointer();
        }
        return parent != null && parent.locator.uri.equals(locator.uri) ? jsonPointer : "/";
    }

    @Override
    public AbstractJsonSchemaElement getParent() {
        return parent;
    }
    
    /**
     * This is a marker whether this element is in the dynamic scope and 
     * must not be cached.
     * 
     * @return 'true' if in dynamic scope, 'false' otherwise.
     */
    public boolean isDynamicScope() {
        return isDynamicScope;
    }

    protected void setDynamicScope(boolean isDynamicScope) {
        this.isDynamicScope = isDynamicScope;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException { 
        return super.clone();
    }
    
    /**
     * Predicate to create a clone of this element linked to another parent.
     * If the element's parent is the same as provided parent, no clone is created.
     * 
     * @param parent - the parent to be assigned to the cloned element
     * 
     * @return the clone of this element or element itself if parents are the same
     */
    protected AbstractJsonSchemaElement relink(AbstractJsonSchemaElement parent) {
        if (this.parent != parent) {
            try {
                AbstractJsonSchemaElement e = (AbstractJsonSchemaElement)this.clone();
                e.parent = parent;
                return e;
            } catch (CloneNotSupportedException ex) {
                return null;
            }
        }
        return this;
    }
}
