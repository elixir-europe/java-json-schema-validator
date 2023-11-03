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
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonReference;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.JsonType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonReferenceImpl extends AbstractJsonReferenceImpl implements JsonReference {

    private JsonSchemaElement schema;

    private String ref;
    private String ref_pointer;
    private JsonSchemaLocator ref_locator;
    private JsonSubschemaParser parser;
    
    public JsonReferenceImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public <T extends JsonSchemaElement> Stream<T> getChildren() {
        if (schema == null) {
            JsonSchemaElement s = getParent();
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
        
        return schema.getChildren();
    }

    @Override
    public JsonSchemaElement getSchema() throws JsonSchemaException {
        if (schema == null) {
            try {
                JsonValue jsubschema = ref_locator.getSchema(ref_pointer);
                if (jsubschema == null) {
                    throw new JsonSchemaException(
                            new ParsingError(ParsingMessage.UNRESOLVABLE_REFERENCE, ref));
                }

                schema = parser.parse(ref_locator, getParent(), ref_pointer, jsubschema, null);
            } catch(IOException | JsonException | IllegalArgumentException ex) {
                throw new JsonSchemaException(
                    new ParsingError(ParsingMessage.INVALID_REFERENCE, ref));
            }
        }
        return schema;
    }

    @Override
    public JsonReferenceImpl read(final JsonSubschemaParser parser,
                                  final JsonObject object, 
                                  final JsonType type) throws JsonSchemaException {

        super.read(parser, object, type);

        this.parser = parser;

        ref = object.getString(REF);
        try {
            final URI uri = URI.create(ref);
            final String fragment = uri.getFragment();
            if (fragment == null) {
                ref_pointer = "/";
                ref_locator = getScope().resolve(uri);
            } else if ("#".equals(ref)) {
                ref_pointer = "/";
                ref_locator = getScope();
            } else if (fragment.startsWith("/")) {
                ref_pointer = fragment;
                if (ref.startsWith("#")) {
                    ref_locator = getScope();
                } else {
                    ref_locator = getScope().resolve(
                        new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null));                        
                }
            } else {
                ref_pointer = "/";
                ref_locator = getScope().resolve(uri);
            }
        } catch(JsonException | IllegalArgumentException | URISyntaxException ex) {
            throw new JsonSchemaException(
                    new ParsingError(ParsingMessage.INVALID_REFERENCE, ref));
        }
        
        return this;
    }
}
