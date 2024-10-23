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

import es.elixir.bsc.json.schema.model.JsonSchema;
import java.util.List;

/**
 * Json Schema validation callback interface.
 * 
 * The implementation class may throw an ValidationException to stop validation process.
 * 
 * @author Dmitry Repchevsky
 */

public interface JsonSchemaValidationCallback<T> {
    
    /**
     * Callback method called by the validator after the value is validated.
     * Implementations may include custom validation errors into the errors list
     * or stop validation process by throwing the ValidationException.
     * Note that validation errors not necessary mean the schema is invalid -
     * this may be a part of evaluation of 'oneOf', for example.
     * 
     * @param schema json schema model to validate json value.
     * @param pointer json pointer to the validated value
     * @param value json value that was validated.
     * @param parent json value that includes the validated one
     * @param errors the list of validation errors found during the validation.
     * 
     * @throws ValidationException the exception to be thrown by the validator.
     */
    void validated(JsonSchema schema, String pointer, T value, T parent, 
            List<ValidationError> errors) throws ValidationException;
}