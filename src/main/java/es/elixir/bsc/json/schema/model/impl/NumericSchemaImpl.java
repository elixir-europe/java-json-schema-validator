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

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.model.JsonType;
import es.elixir.bsc.json.schema.model.NumericSchema;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import java.math.BigDecimal;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 * @param <T>
 */

public abstract class NumericSchemaImpl<T extends Number> extends PrimitiveSchemaImpl
                                        implements NumericSchema<T> {
    protected BigDecimal multipleOf;
    
    protected T minimum;
    protected T maximum;

    protected Boolean isExclusiveMinimum;
    protected Boolean isExclusiveMaximum;

    protected Number exclusiveMinimum;
    protected Number exclusiveMaximum;
    
    public NumericSchemaImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public BigDecimal getMultipleOf() {
        return multipleOf;
    }
    
    @Override
    public void setMultipleOf(BigDecimal multipleOf) {
        this.multipleOf = multipleOf;
    }
    
    @Override
    public T getMinimum() {
        return minimum;
    }
    
    @Override
    public void setMinimum(T minimum) {
        this.minimum = minimum;
    }
    
    @Override
    public T getMaximum() {
        return maximum;
    }
    
    @Override
    public void setMaximum(T max) {
        this.maximum = max;
    }

    @Override
    public Boolean isExclusiveMinimum() {
        return isExclusiveMinimum;
    }

    @Override
    public void setExclusiveMinimum(Boolean isExclusiveMinimum) {
        this.isExclusiveMinimum = isExclusiveMinimum;
    }

    @Override
    public Number getExclusiveMinimum() {
        return exclusiveMinimum;
    }

    @Override
    public void setExclusiveMinimum(Number exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public Boolean isExclusiveMaximum() {
        return isExclusiveMaximum;
    }

    @Override
    public void setExclusiveMaximum(Boolean isExclusiveMaximum) {
        this.isExclusiveMaximum = isExclusiveMaximum;
    }

    @Override
    public Number getExclusiveMaximum() {
        return exclusiveMaximum;
    }

    @Override
    public void setExclusiveMaximum(Number exclusiveMaximum) {
        this.exclusiveMaximum = exclusiveMaximum;
    }
    
    @Override
    public NumericSchemaImpl read(final JsonSubschemaParser parser,
                                  final JsonObject object, 
                                  final JsonType type) throws JsonSchemaException {

        super.read(parser, object, type);
        
        final JsonNumber mul = JsonSchemaUtil.check(object.getJsonNumber(MULTIPLE_OF), JsonValue.ValueType.NUMBER);
        if (mul != null) {
            multipleOf = mul.bigDecimalValue();
        }
        
        final JsonValue jexclusiveMinimum = object.get(EXCLUSIVE_MINIMUM);
        if (jexclusiveMinimum != null) {
            switch(jexclusiveMinimum.getValueType()) {
                case NUMBER: exclusiveMinimum = ((JsonNumber)jexclusiveMinimum).doubleValue();
                             break;
                case TRUE:   isExclusiveMinimum = true;
                case FALSE:  break;
                default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                 new Object[] {EXCLUSIVE_MINIMUM, jexclusiveMinimum.getValueType().name(), "either number or boolean (draft4)"}));
            }
        }
        
        final JsonValue jexclusiveMaximum = object.get(EXCLUSIVE_MAXIMUM);
        if (jexclusiveMaximum != null) {
            switch(jexclusiveMaximum.getValueType()) {
                case NUMBER: exclusiveMaximum = ((JsonNumber)jexclusiveMaximum).doubleValue();
                             break;
                case TRUE:   isExclusiveMaximum = true;
                case FALSE:  break;
                default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                 new Object[] {EXCLUSIVE_MAXIMUM, jexclusiveMaximum.getValueType().name(), "either number or boolean (draft4)"}));
            }
        }

        return this;
    }    
}
