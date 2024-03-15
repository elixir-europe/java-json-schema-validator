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
import es.elixir.bsc.json.schema.model.JsonType;
import java.util.stream.Stream;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.IOException;

/**
 * @author Dmitry Repchevsky
 */

public class JsonRecursiveReferenceImpl extends AbstractJsonReferenceImpl
        implements JsonRecursiveReference {
    
    public JsonRecursiveReferenceImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public <T extends JsonSchemaElement> Stream<T> getChildren() {
        return Stream.of(); // TODO
    }

    @Override
    public JsonSchemaElement getSchema() throws JsonSchemaException {
        if (schema == null) {
            AbstractJsonSchemaElement e = this;
            try {
                while ((e = e.getParent()) != null) {
                    if ("/".equals(e.getJsonPointer())) {
                        final JsonSchemaLocator scope = e.getScope();
                        final JsonValue value = scope.getSchema("/");
                        if (value instanceof JsonObject jsubschema) {
                            final boolean anchor = jsubschema.getBoolean(RECURSIVE_ANCHOR, false);

                            // no 'type' or 'type': [] leads to JsonMultitypeSchemaWrapper wrapper.
                            // because we are in a 'root' parent either has different location
                            // or be the wrapper.
                            if (e.parent instanceof JsonMultitypeSchemaWrapper) {
                                e = e.parent;
                            }

                            if (anchor) {
                                schema = e;
                                continue;
                            } else if (schema == null) {
                                schema = e;
                            }
                            break;
                        }
                    }
                }
            } catch (IOException ex) {}
        }
        
        return schema;
    }
    @Override
    public JsonRecursiveReferenceImpl read(final JsonSubschemaParser parser,
                                           final JsonObject object, 
                                           final JsonType type) throws JsonSchemaException {

        super.read(parser, object, type);

        final JsonString jrecursive_ref = JsonSchemaUtil.check(object.get(RECURSIVE_REF), JsonValue.ValueType.STRING);
        if (!"#".equals(jrecursive_ref.getString())) {
            throw new JsonSchemaException(
                    new ParsingError(ParsingMessage.INVALID_REFERENCE, jrecursive_ref.getString()));
        }
        
        return this;
    }
}
