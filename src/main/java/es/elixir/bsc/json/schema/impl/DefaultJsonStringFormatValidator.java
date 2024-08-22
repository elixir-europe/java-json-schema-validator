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

package es.elixir.bsc.json.schema.impl;

import es.elixir.bsc.json.schema.JsonStringFormatValidator;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationException;
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.model.JsonStringSchema;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * @author Dmitry Repchevsky
 */

public class DefaultJsonStringFormatValidator implements JsonStringFormatValidator {
    
    private final static Pattern EMAIL_PATTERN = Pattern.compile("^(.+)@(.+)$");
    private final static Pattern HOST_NAME_PATTERN = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
    private final static Pattern IP4_PATTERN = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private final static Pattern IP6_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$");
    
    public static void validate(String jsonPointer, JsonStringSchema schema,
            String value) throws ValidationException {

        final String format = schema.getFormat();
        switch(format) {
            case DATE_TIME: try { 
                                ZonedDateTime.parse(value);
                            } catch(DateTimeParseException ex) {
                                throw new ValidationException(
                                        new ValidationError(schema.getId(), schema.getJsonPointer(), jsonPointer,
                                        ValidationMessage.STRING_DATE_TIME_FORMAT_CONSTRAINT_MSG, value));
                            }
                            break;
            case EMAIL:     if (!EMAIL_PATTERN.matcher(value).matches()) {
                                throw new ValidationException(
                                        new ValidationError(schema.getId(), schema.getJsonPointer(), jsonPointer,
                                        ValidationMessage.STRING_EMAIL_FORMAT_CONSTRAINT_MSG, value));
                            }
                            break;
            case HOSTNAME:  if (!HOST_NAME_PATTERN.matcher(value).matches()) {
                                throw new ValidationException(
                                        new ValidationError(schema.getId(), schema.getJsonPointer(), jsonPointer,
                                        ValidationMessage.STRING_HOSTNAME_FORMAT_CONSTRAINT_MSG, value));
                            }
                            break;
            case IP4:       if (!IP4_PATTERN.matcher(value).matches()) {
                                throw new ValidationException(
                                        new ValidationError(schema.getId(), schema.getJsonPointer(), jsonPointer,
                                        ValidationMessage.STRING_IP4_FORMAT_CONSTRAINT_MSG, value));
                            }
                            break;
            case IP6:       if (!IP6_PATTERN.matcher(value).matches()) {
                                throw new ValidationException(
                                        new ValidationError(schema.getId(), schema.getJsonPointer(), jsonPointer,
                                        ValidationMessage.STRING_IP6_FORMAT_CONSTRAINT_MSG, value));
                            }
                            break;
            case URI:       try {
                                java.net.URI.create(value);
                            } catch(IllegalArgumentException ex) {
                                throw new ValidationException(
                                        new ValidationError(schema.getId(), schema.getJsonPointer(), jsonPointer,
                                        ValidationMessage.STRING_URI_FORMAT_CONSTRAINT_MSG, value)); 
                            }
                            break;
        }
    }
}
