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
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationException;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonReference;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import es.elixir.bsc.json.schema.model.JsonType;
import javax.json.JsonException;
import java.util.List;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Dmitry Repchevsky
 */

public abstract class AbstractJsonReferenceImpl extends AbstractJsonSchema<JsonObject>
        implements JsonReference {

    protected JsonSchemaElement schema;

    protected URI ref;
    protected String ref_pointer;
    protected JsonSchemaLocator ref_locator;
    protected JsonSubschemaParser parser;
    
    public AbstractJsonReferenceImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }
    
    protected void read(JsonSubschemaParser parser, JsonObject object, 
            JsonType type, String tag) throws JsonSchemaException {

        this.parser = parser;
        
        final String jref = object.getString(tag);
        try {
            ref = URI.create(jref);
            final String fragment = ref.getFragment();
            if (fragment == null) {
                ref_pointer = "/";
                ref_locator = getScope().resolve(ref);
            } else if ("#".equals(jref)) {
                ref_pointer = "/";
                ref_locator = getScope();
            } else if (fragment.startsWith("/")) {
                ref_pointer = fragment;
                if (jref.startsWith("#")) {
                    ref_locator = getScope();
                } else {
                    ref_locator = getScope().resolve(
                        new URI(ref.getScheme(), ref.getSchemeSpecificPart(), null));                        
                }
            } else {
                ref_pointer = "/";
                ref_locator = getScope().resolve(ref);
            }
        } catch(JsonException | IllegalArgumentException | URISyntaxException ex) {
            throw new JsonSchemaException(
                    new ParsingError(ParsingMessage.INVALID_REFERENCE, ref));
        }
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List evaluated, List<ValidationError> errors, 
            JsonSchemaValidationCallback<JsonValue> callback) throws ValidationException {

        try {
            final AbstractJsonSchema sch = getSchema();
            return sch.validate(jsonPointer, value, parent, evaluated, errors, callback);
        } catch (JsonSchemaException ex) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer, ex.getMessage()));
        }
        return false;
    }
}
