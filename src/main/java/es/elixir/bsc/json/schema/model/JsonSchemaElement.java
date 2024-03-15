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

package es.elixir.bsc.json.schema.model;
import java.net.URI;
import java.util.stream.Stream;

/**
 * Any parsable Json Schema element (i.g. Schema, Schema properties, etc)
 * 
 * @author Dmitry Repchevsky
 */

public interface JsonSchemaElement {

    URI getId();

    /**
     * Returns Json Pointer to locate Json Schema object in the Json Schema document.
     * The pointer is relative to the schema id.
     * 
     * @return Json Pointer to this schema
     */
    String getJsonPointer();

     /**
     * Get the enclosing Json Schema element.
     * 
     * @param <T>
     * 
     * @return immediate parent node (element) 
     */
    <T extends JsonSchemaElement> T getParent();
    
    /**
     * Get the stream of all child schemas.
     * 
     * @param <T>
     * 
     * @return stream of child schemas
     */
    <T extends JsonSchemaElement> Stream<T> getChildren();
}
