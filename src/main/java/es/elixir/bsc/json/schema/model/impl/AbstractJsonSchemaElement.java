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
import java.util.Objects;

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
    
    public final JsonSchemaLocator scope;
    public final JsonSchemaLocator locator;
    public final String jsonPointer;
    
    private boolean isDynamicScope;
    
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AbstractJsonSchemaElement other &&
            this.getClass() == obj.getClass()) {
            return Objects.equals(jsonPointer, other.jsonPointer) &&
                   Objects.equals(scope.uri, other.scope.uri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.scope.uri);
        hash = 31 * hash + Objects.hashCode(this.jsonPointer);
        hash = 31 * hash + Objects.hashCode(this.getClass().hashCode());
        return hash;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException { 
        return super.clone();
    }
    
    /**
     * Predicate to clone provided element.
     * Method just omits possible (impossible) CloneNotSupportedException.
     * 
     * @param <T> returned type inferred from caller
     * @param element the element to be cloned
     * 
     * @return the clone of provided element
     */
    protected <T extends AbstractJsonSchemaElement> T clone(T element) {
        if (element != null) {
            try {
                return (T)element.clone();
            } catch (CloneNotSupportedException ex) {}
        }
        return null;
    }
    
    /**
     * Predicate used in streams to replace parent for this element
     * 
     * @param parent new parent for this element
     * 
     * @return affected element (this)
     */
    protected AbstractJsonSchemaElement setParent(AbstractJsonSchemaElement parent) {
        this.parent = parent;
        return this;
    }

}
