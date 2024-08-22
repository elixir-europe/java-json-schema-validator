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

import java.net.URI;

/**
 * @author Dmitry Repchevsky
 */

public class ValidationError {

    public final int code;
    public final URI id;
    public final String pointer;
    public final String path;
    public final String message;
    
    public ValidationError(String message) {
        this(null, null, null, message);
    }
    
    public ValidationError(URI id, String pointer, String message) {
        this(id, pointer, null, message);
    }

    public ValidationError(URI id, String pointer, String path, String message) {
        
        this.code = -1;
        this.id = id;
        this.pointer = pointer;
        this.path = path;
        this.message = message;
    }
    
    public ValidationError(URI id, String pointer, String path, 
            ValidationMessage message, Object... args) {
        
        this.code = message.CODE;
        this.id = id;
        this.pointer = pointer;
        this.path = path;
        this.message = String.format(message.VALUE, args);
    }
}
