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

package es.elixir.bsc.json.schema.impl;

import es.elixir.bsc.json.schema.JsonSchemaLocator;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonPointer;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Repchevsky
 */
    
public class DefaultJsonSchemaLocator extends JsonSchemaLocator {

    private final static HttpClient http_client = 
            HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

    protected final Map<URI, JsonValue> schemas;

    public DefaultJsonSchemaLocator(URI uri) {
        this(uri, new HashMap<>());
    }
    
    protected DefaultJsonSchemaLocator(URI uri, Map<URI, JsonValue> schemas) {
        super(uri);
        this.schemas = schemas;
    }

    @Override
    public void setSchema(JsonValue schema) {
        schemas.put(uri, schema);
    }

    @Override
    public JsonValue getSchema(String jsonPointer) 
            throws IOException, JsonException {
        return getSchema(this.uri, jsonPointer);
    }

    @Override
    public JsonValue getSchema(URI uri, String jsonPointer)
            throws IOException, JsonException {
        
        JsonValue schema = schemas.get(uri);
        if (schema == null) {
            try {
                uri = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
                schema = schemas.get(uri);
                if (schema == null) {
                    InputStream in = null;
                    final String scheme = uri.getScheme();
                    if ("http".equals(scheme) || "https".equals(scheme)) {
                        try {
                            final HttpResponse<InputStream> response = 
                                    http_client.send(HttpRequest.newBuilder(uri).build(), 
                                            HttpResponse.BodyHandlers.ofInputStream());
                            if (response == null) {
                                throw new IOException(String.format("no respnse from %s", uri));
                            }

                            if (response.statusCode() >= 300) {
                                throw new IOException(String.format("error reading from %s %d", uri, response.statusCode()));
                            }
                            in = response.body();
                        } catch (InterruptedException ex) {
                            throw new IOException(String.format("no respnse from %s", uri));
                        }
                    } else {
                        // not http schemas like "file" etc...
                        in = uri.toURL().openStream();
                    }

                    try {
                        final JsonReaderFactory factory = Json.createReaderFactory(Collections.EMPTY_MAP);
                        final JsonReader reader = factory.createReader(in);
                        schema = reader.readValue();
                        setSchema(schema);
                    } finally {
                        in.close();
                    }
                }
            } catch (URISyntaxException ex) {}
        }

        if ("/".endsWith(jsonPointer)) {
            return schema;
        }
        
        if (JsonValue.ValueType.OBJECT == schema.getValueType()) {
            final JsonPointer pointer = Json.createPointer(jsonPointer);

            // there is a bug as containsValue() rises an exception when not found. 
            if (pointer.containsValue(schema.asJsonObject())) {
                return pointer.getValue(schema.asJsonObject());
            }
        }

        return null;
    }

    @Override
    public JsonSchemaLocator resolve(URI uri) {
        // fix wrong (?) uri.resove() where base uri is opaque and child has no schema
        if (super.uri.isOpaque() && uri.getSchemeSpecificPart().isEmpty() && uri.getFragment() != null) {
            try {
                return new DefaultJsonSchemaLocator(new URI(super.uri.getScheme(), 
                        super.uri.getSchemeSpecificPart(), uri.getFragment()), schemas);
            } catch(URISyntaxException ex) {}
        }
        return new DefaultJsonSchemaLocator(super.uri.resolve(uri), schemas);
    }
}
