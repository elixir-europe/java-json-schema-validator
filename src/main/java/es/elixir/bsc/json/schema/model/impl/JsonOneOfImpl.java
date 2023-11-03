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
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.model.JsonOneOf;
import java.util.ArrayList;
import java.util.List;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonType;
import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonOneOfImpl extends SchemaArrayImpl
                           implements JsonOneOf<AbstractJsonSchema> {

    public JsonOneOfImpl(AbstractJsonSchema parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public JsonOneOfImpl read(final JsonSubschemaParser parser,
                              final JsonArray schema, 
                              final JsonType type) throws JsonSchemaException {

        super.read(parser, schema, type);
        return this;
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List evaluated, List errors, JsonSchemaValidationCallback callback) {

        final List<String> matched = new ArrayList();
        
        final List eva = new ArrayList();
        final List<ValidationError> err = new ArrayList<>();
        for (AbstractJsonSchema schema : this) {
            final List e = new ArrayList(evaluated);
            if (schema.validate(jsonPointer, value, parent, e, err, callback)) {
                matched.add(schema.getId().toString());
                eva.clear();
                eva.addAll(e);
            }
        }
        
        // An instance validates successfully if it validates against 
        // exactly one schema defined by this keyword's value
        
        final int matches = matched.size();
        switch (matches) {
            case 1:
                eva.removeAll(evaluated);
                evaluated.addAll(eva);
                return true;
            case 0:
                errors.addAll(err);
                errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.OBJECT_ONE_OF_CONSTRAINT_MSG, "no matched schemas found"));
                break;
            default:
                errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.OBJECT_ONE_OF_CONSTRAINT_MSG,
                        String.format("several schemas matches (%s)", String.join(",", matched))));
                break;
        }
        return false;
    }
}
