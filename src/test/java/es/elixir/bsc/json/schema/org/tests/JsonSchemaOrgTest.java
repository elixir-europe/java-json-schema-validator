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
 * *****************************************************************************
 */

package es.elixir.bsc.json.schema.org.tests;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaParserConfig;
import es.elixir.bsc.json.schema.JsonSchemaReader;
import es.elixir.bsc.json.schema.JsonSchemaVersion;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.impl.DefaultJsonSchemaLocator;
import es.elixir.bsc.json.schema.model.JsonSchema;
import es.elixir.bsc.json.schema.model.JsonTest;
import java.io.Closeable;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.AfterClass;
/**
 * @author Dmitry Repchevsky
 */

public class JsonSchemaOrgTest {
    
    private static RemotesServer server;

    @BeforeClass
    public static void init() throws IOException {
        server = new RemotesServer();
    }
    
    @AfterClass
    public static void close() throws IOException {
        server.close();
    }
    
    public void test(String file) {
        test(file, null);
    }

    public void test(String file, JsonSchemaVersion version) {
        
        final JsonSchemaParserConfig config = 
                new JsonSchemaParserConfig()
                        .setJsonSchemaVersion(version);
        
        final URL url = JsonTest.class.getClassLoader().getResource(file);
        
        try (InputStream in = url.openStream();
             JsonParser parser = Json.createParser(in)) {
            final URI uri = url.toURI();
            final StringBuilder out = new StringBuilder();
            
            if (parser.hasNext() && parser.next() == JsonParser.Event.START_ARRAY) {
                JsonArray array = parser.getArray();
                for (int j = 0, n = array.size(); j < n; j++) {
                    JsonObject obj = array.getJsonObject(j);
                    JsonValue sch = obj.get("schema");
                    DefaultJsonSchemaLocator locator = new DefaultJsonSchemaLocator(uri.resolve(Integer.toString(j)));
                    locator.setSchema(sch);
                    JsonSchemaReader reader = JsonSchemaReader.getReader(config);
                    JsonSchema schema = reader.read(locator);
                    JsonArray tests = obj.getJsonArray("tests");
                    for (int i = 0; i < tests.size(); i++) {
                        JsonObject test = tests.getJsonObject(i);
                        JsonValue data = test.get("data");
                        boolean valid = test.getBoolean("valid");
                        
                        List<ValidationError> errors = new ArrayList<>();
                        schema.validate(data, errors);
                        
                        if (valid != errors.isEmpty()) {
                            out.append(String.format("%s : %s\n", obj.getString("description", ""), 
                                test.getString("description", "")));
                        }
                    }
                }
                if (out.length() > 0) {
                    Assert.fail("\n" + out.toString());
                }
            }
        } catch (IOException | JsonSchemaException | URISyntaxException ex) {
            Logger.getLogger(JsonSchemaOrgTest.class.getName()).log(Level.SEVERE, null, ex);
            Assert.fail(ex.getMessage());
        }
    }

    public static class RemotesServer implements HttpHandler, Closeable {

        private HttpServer server;

        public RemotesServer() {
            try {
                server = HttpServer.create(new InetSocketAddress(1234), 0);
                server.createContext("/", this);
                server.start();
            } catch (IOException ex) {
                Logger.getLogger(JsonSchemaOrgTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final URI uri = exchange.getRequestURI();
            try (InputStream in = JsonSchemaOrgTest.class.getClassLoader().getResourceAsStream("json-schema-org/remotes/" + uri.getPath())) {
                if (in != null) {
                    final byte[] file = in.readAllBytes();
                    exchange.sendResponseHeaders(200, file.length);
                    exchange.getResponseBody().write(file);
                } else {
                    exchange.sendResponseHeaders(404, 0);
                }
            } catch (IOException ex) {
                exchange.sendResponseHeaders(500, 0);
            } finally {
                exchange.close();
            }
        }        

        @Override
        public void close() throws IOException {
            server.stop(0);
        }
    }
}
