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

package es.elixir.bsc.json.schema.org.tests;

import org.junit.Test;

/**
 * @author Dmitry Repchevsky
 */

public class JsonSchemaDependentSchemasTest extends JsonSchemaOrgTest {

    private final static String JSON_DRAFT201909_TEST_FILE = "json-schema-org/tests/draft2019-09/dependentSchemas.json";
    private final static String JSON_DRAFT202012_TEST_FILE = "json-schema-org/tests/draft2020-12/dependentSchemas.json";
    
    @Test
    public void test_draft201909() {
        test(JSON_DRAFT201909_TEST_FILE);
    }

    @Test
    public void test_draft202012() {
        test(JSON_DRAFT202012_TEST_FILE);
    }
}
