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
import es.elixir.bsc.json.schema.model.JsonDynamicReference;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import java.util.stream.Stream;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Dmitry Repchevsky
 */

public class JsonDynamicReferenceImpl extends JsonReferenceImpl
        implements JsonDynamicReference {

    public JsonDynamicReferenceImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator scope, JsonSchemaLocator locator, String jsonPointer) {
        super(parent, scope, locator, jsonPointer);
    }

    @Override
    public <T extends JsonSchemaElement> Stream<T> getChildren() {
        return Stream.of(); // TODO
    }

    @Override
    public JsonSchemaElement getSchema() throws JsonSchemaException {
        if (schema == null) {
            final String fragment = ref.getFragment();
            if (fragment != null) {
                try {
                    AbstractJsonSchemaElement e = getSchema(this, ref);
                    if (e == null) {
                        final JsonValue value = ref_locator.getSchema("/");
                        e = parser.parse(ref_locator, null, "/", value, null);
                    }
                    schema = getSchema(e, ref);
                    if (schema != null) {
                        final URI uri = new URI(null, null, fragment);
                        while ((e = e.getParent()) != null) {
                            final JsonSchemaElement s = getSchema(e, uri);
                            if (s != null) {
                                schema = s;
                            }

                        }
                    }
                } catch (IOException | URISyntaxException ex) {}
            }
        }
        
        if (schema == null && super.getSchema() == null) {
            throw new JsonSchemaException(
                new ParsingError(ParsingMessage.UNRESOLVABLE_REFERENCE, ref));
        }
        
        return schema;
    }

    private AbstractJsonSchemaElement getSchema(AbstractJsonSchemaElement e, URI uri)
            throws IOException, JsonSchemaException {
        final String fragment = uri.getFragment();
        final JsonSchemaLocator scope = e.scope.resolve(uri);
        final JsonValue value = scope.getSchema("/");
        if (value instanceof JsonObject jsubschema) {
            final String anchor = jsubschema.getString(DYNAMIC_ANCHOR, null);
            if (fragment.equals(anchor)) {
                return parser.parse(scope, this, e.getJsonPointer(), jsubschema, null);
            }
        }
        return null;
    }
    
    @Override
    public JsonDynamicReferenceImpl read(JsonSubschemaParser parser, 
            JsonObject object) throws JsonSchemaException {
        
        super.read(parser, object, DYNAMIC_REF);
        
        return this;
    }
}
