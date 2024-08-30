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

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaLocator;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonRecursiveReference;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.PrimitiveSchema;
import java.util.stream.Stream;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonRecursiveReferenceImpl extends AbstractJsonReferenceImpl
        implements JsonRecursiveReference {
    
    public JsonRecursiveReferenceImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator locator, String jsonPointer) {
        super(parent, locator, jsonPointer);
        
        AbstractJsonSchemaElement e = this;
        do {
            e.setDynamicScope(true);
        } while((e = e.getParent()) != null);
    }

    @Override
    public <T extends JsonSchemaElement> Stream<T> getChildren() {
        return Stream.empty(); // TODO
    }

    @Override
    public JsonSchemaElement getSchema() throws JsonSchemaException {
        if (schema == null) {
            AbstractJsonSchemaElement e = this;
            while ((e = e.getParent()) != null) {
                if (e instanceof PrimitiveSchema element &&
                    "/".equals(e.getJsonPointer())) {
                    final Boolean anchor = element.getRecursiveAnchor();
                    
                    // no 'type' or 'type': [] leads to JsonMultitypeSchemaWrapper wrapper.
                    // because we are in a 'root' parent either has different location
                    // or be the wrapper.
                    if (e.getParent() instanceof JsonMultitypeSchemaWrapper) {
                        e = e.getParent();
                    }

                    if (Boolean.TRUE == anchor) {
                        schema = e;
                        continue;
                    } else if (schema == null) {
                        schema = e;
                    }
                    break;
                }
            }
        }
        
        return schema;
    }

    @Override
    public JsonRecursiveReferenceImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {

        super.read(parser, object);

        final JsonString jrecursive_ref = JsonSchemaUtil.check(object.get(RECURSIVE_REF), JsonValue.ValueType.STRING);
        if (!"#".equals(jrecursive_ref.getString())) {
            throw new JsonSchemaException(
                    new ParsingError(ParsingMessage.INVALID_REFERENCE, jrecursive_ref.getString()));
        }
        
        return this;
    }
}
