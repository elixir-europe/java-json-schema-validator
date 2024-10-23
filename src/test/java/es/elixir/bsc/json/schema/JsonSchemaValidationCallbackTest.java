/**
 * *****************************************************************************
 * Copyright (C) 2022 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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

import es.elixir.bsc.json.schema.model.JsonSchema;
import javax.json.Json;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dmitry Repchevsky
 */

public class JsonSchemaValidationCallbackTest {

    private final static String JSON_SCHEMA_FILE = "draft4/schemas/object.json";
    private final static String JSON_FILE_01 = "draft4/data/object_01.json";
    
    @Test
    public void test_01() {
        
        try (InputStream in = JsonSchemaValidationCallbackTest.class.getClassLoader().getResourceAsStream(JSON_FILE_01)) {
            
            URL url = JsonSchemaValidationCallbackTest.class.getClassLoader().getResource(JSON_SCHEMA_FILE);
            
            JsonSchema schema = JsonSchemaReader.getReader().read(url);
            JsonStructure json = Json.createReader(in).read();
            
            final AtomicInteger counter = new AtomicInteger();
            List<ValidationError> errors = new ArrayList<>();
            schema.validate(json, errors, (JsonSchema model, String pointer, 
                    JsonValue value, JsonValue parent, List<ValidationError> err) -> {
                counter.incrementAndGet();
            });

            Assert.assertTrue(errors.isEmpty());
            Assert.assertTrue(counter.get() > 0);
            
        } catch (IOException | JsonSchemaException ex) {
            Logger.getLogger(JsonSchemaValidationCallbackTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
