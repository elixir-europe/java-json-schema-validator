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

package es.elixir.bsc.json.schema;

/**
 * @author Dmitry Repchevsky
 */

public enum JsonSchemaVersion {

    SCHEMA_DRAFT_03("http://json-schema.org/draft-03/schema#"),
    SCHEMA_DRAFT_04("http://json-schema.org/draft-04/schema#"),
    SCHEMA_DRAFT_06("http://json-schema.org/draft-06/schema#"),
    SCHEMA_DRAFT_07("http://json-schema.org/draft-07/schema#"),
    SCHEMA_DRAFT_2019_09("https://json-schema.org/draft/2019-09/schema"),
    SCHEMA_DRAFT_2020_12("https://json-schema.org/draft/2020-12/schema");
    
    public final String VALUE;
    
    private JsonSchemaVersion(String value) {
        this.VALUE = value;
    }
    
    @Override
    public String toString() {
        return VALUE;
    }

    public static JsonSchemaVersion fromValue(String value) {
        for (JsonSchemaVersion type: JsonSchemaVersion.values()) {
            if (type.VALUE.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException(value);
    }

}
