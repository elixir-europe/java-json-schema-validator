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

package es.elixir.bsc.json.schema.model;


/**
 * Primitive empty Json Schema of any type ("object", "array", "string", etc.)
 * 
 * @author Dmitry Repchevsky
 * 
 * @param <T>
 */

public interface PrimitiveSchema<T extends JsonSchema> extends JsonSchema {

    public final static String TYPE = "type";
    public final static String TITLE = "title";
    public final static String DESCRIPTION = "description";
    public final static String DEFAULT = "default";

    public final static String ALL_OF = "allOf";
    public final static String ANY_OF = "anyOf";
    public final static String ONE_OF = "oneOf";
    public final static String NOT = "not";

    public final static String IF = "if";
    public final static String THEN = "then";
    public final static String ELSE = "else";

    String getAnchor();
    String getDynamicAnchor();
    Boolean getRecursiveAnchor();

    JsonAllOf getAllOf();
    JsonAnyOf getAnyOf();
    JsonOneOf getOneOf();
    JsonNot getNot();

    <T extends JsonSchema> T getIf();
    <T extends JsonSchema> T getThen();
    <T extends JsonSchema> T getElse();
    
    JsonReference getReference();
}
