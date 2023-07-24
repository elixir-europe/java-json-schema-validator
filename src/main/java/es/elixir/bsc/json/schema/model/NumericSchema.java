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

package es.elixir.bsc.json.schema.model;

import java.math.BigDecimal;

/**
 * The interface for Numeric Json Schema types.
 * (Json Schema defines "number" and "integer") types.
 * 
 * @author Dmitry Repchevsky
 * 
 * @param <T>
 */

public interface NumericSchema<T extends Number> extends AbstractJsonSchema {
    
    public final static String MULTIPLE_OF = "multipleOf";
    
    public final static String MINIMUM = "minimum";
    public final static String MAXIMUM = "maximum";
    
    public final static String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    public final static String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";

    public BigDecimal getMultipleOf();
    public void setMultipleOf(BigDecimal multipleOf);

    Boolean isExclusiveMinimum();
    void setExclusiveMinimum(Boolean exclusiveMinimum);

    Number getExclusiveMinimum();
    void setExclusiveMinimum(Number exclusiveMinimum);
    
    Boolean isExclusiveMaximum();
    void setExclusiveMaximum(Boolean exclusiveMaximum);

    Number getExclusiveMaximum();
    void setExclusiveMaximum(Number exclusiveMaximum);
    
    T getMinimum();
    void setMinimum(T minimum);
    
    T getMaximum();
    void setMaximum(T maximum);
}
