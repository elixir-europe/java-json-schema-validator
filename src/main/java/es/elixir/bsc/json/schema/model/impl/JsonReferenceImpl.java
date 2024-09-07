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
import es.elixir.bsc.json.schema.model.JsonReference;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonReferenceImpl extends AbstractJsonReferenceImpl implements JsonReference {
    
    public JsonReferenceImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator locator, String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public Stream<AbstractJsonSchemaElement> getChildren() {
        if (schema == null) {
            AbstractJsonSchemaElement s = getParent();
            while (s != null) {
                if (ref_locator.uri.equals(s.getId()) &&
                    ref_pointer.equals(s.getJsonPointer())) {
                    return Stream.of(); // cyclic ref
                }
                s = s.getParent();
            }
            try {
                schema = getSchema();
            } catch(JsonSchemaException ex) {
                return Stream.of(); // unresolvable ref
            }
        }
        
        return schema.relink(this).getChildren();
    }

    @Override
    public AbstractJsonSchemaElement getSchema() throws JsonSchemaException {
        if (schema == null) {
            try {
                JsonValue jsubschema = ref_locator.getSchema(ref_pointer);
                if (jsubschema == null) {
                    throw new JsonSchemaException(
                            new ParsingError(ParsingMessage.UNRESOLVABLE_REFERENCE, ref));
                }
                
                AbstractJsonSchemaElement root = this;

                if (!locator.uri.equals(ref_locator.uri)) {
                    // if the reference goes inside other document or points to tha 'anchor',
                    // parse this document as a 'parent'
                    if (ref_pointer.length() > 1) {
                        final JsonValue val = ref_locator.getSchema("/");
                        root = parser.parse(ref_locator, this, "/", val, null);              
                    } else if (ref_locator.uri.getFragment() != null) {
                        final JsonValue val = ref_locator.getSchema("/");
                        root = parser.parse(ref_locator.resolve(
                                new URI(ref_locator.uri.getScheme(), ref_locator.uri.getSchemeSpecificPart(), null)), 
                                this, "/", val, null);
                    }
                }
                schema = parser.parse(ref_locator, root, ref_pointer, jsubschema, null);
            } catch(IOException | JsonException | IllegalArgumentException | URISyntaxException ex) {
                throw new JsonSchemaException(
                    new ParsingError(ParsingMessage.INVALID_REFERENCE, ref));
            }
        }
        return schema;
    }

    @Override
    public JsonReferenceImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {
        
        super.read(parser, object, REF);
        return this;
    }
}
