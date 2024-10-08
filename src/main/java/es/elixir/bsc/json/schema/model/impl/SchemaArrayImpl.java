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
import es.elixir.bsc.json.schema.model.SchemaArray;
import java.util.HashSet;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import javax.json.JsonArray;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 * 
 * @param <T>
 */

public abstract class SchemaArrayImpl<T extends JsonValue> 
        extends AbstractJsonSchema<T>
        implements SchemaArray<AbstractJsonSchema> {
    
    private final Set<AbstractJsonSchema> schemas;
    
    public SchemaArrayImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator locator, String jsonPointer) {
        super(parent, locator, jsonPointer);

        schemas = new HashSet();
    }

    @Override
    public Stream<AbstractJsonSchemaElement> getChildren() {
        // clone array schemas and set their parent to 'this'
        final Stream<AbstractJsonSchemaElement> children =
                schemas.stream().map(c -> c.relink(this));

        return children.flatMap(e -> Stream.concat(Stream.of(e), e.getChildren()));
    }

    @Override
    public Iterator<AbstractJsonSchema> iterator() {
        return schemas.iterator();
    }

    @Override
    public boolean add(AbstractJsonSchema schema) {
        return schemas.add(schema);
    }

    @Override
    public boolean remove(AbstractJsonSchema schema) {
        return schemas.remove(schema);
    }

    @Override
    public boolean contains(AbstractJsonSchema schema) {
        return schemas.contains(schema);
    }

    public SchemaArrayImpl read(JsonSubschemaParser parser, JsonArray array)
            throws JsonSchemaException {

        for (int i = 0, n = array.size(); i < n; i++) {
            final JsonValue value = array.get(i);
            final AbstractJsonSchema schema = parser.parse(locator, this, 
                    getJsonPointer() + "/" + Integer.toString(i), value, null);
            add(schema);
        }
        
        return this;
    }
}