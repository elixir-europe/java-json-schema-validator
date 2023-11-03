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

package es.elixir.bsc.json.schema;

import static es.elixir.bsc.json.schema.ValidationErrorCode.*;

/**
 * @author Dmitry Repchevsky
 */

public enum ValidationMessage {
    
    OBJECT_EXPECTED_MSG(OBJECT_EXPECTED, "object expected, found: %s"),
    ARRAY_EXPECTED_MSG(ARRAY_EXPECTED, "array expected, found: %s"),
    STRING_EXPECTED_MSG(STRING_EXPECTED, "string expected, found: %s"),
    NUMBER_EXPECTED_MSG(NUMBER_EXPECTED, "number expected, found: %s"),
    BOOLEAN_EXPECTED_MSG(BOOLEAN_EXPECTED, "boolean expected, found: %s"),
    NULL_EXPECTED_MSG(NULL_EXPECTED, "null expected, found: %s"),
    
    UNEVALUATED_BOOLEAN_SCHEMA_MSG(UNEVALUATED_BOOLEAN_SCHEMA, "boolean schema type = false"),
    
    REFERENCE_UNRESOLVED_MSG(REFERENCE_UNRESOLVED, "unable to resolve reference. '$ref': '%s'."),
    
    OBJECT_REQUIRED_PROPERTY_CONSTRAINT_MSG(OBJECT_REQUIRED_PROPERTY_CONSTRAINT, "object missed required property: %s"),
    OBJECT_DEPENDENT_REQUIRED_CONSTRAINT_MSG(OBJECT_DEPENDENT_REQUIRED_CONSTRAINT, "object missed required dependent property: %s"),

    OBJECT_MIN_PROPERTIES_CONSTRAINT_MSG(OBJECT_MIN_PROPERTIES_CONSTRAINT, "minProperties = %d > object.properties.size = %d"),
    OBJECT_MAX_PROPERTIES_CONSTRAINT_MSG(OBJECT_MAX_PROPERTIES_CONSTRAINT, "maxProperties = %d < object.properties.size = %d"),
    
    OBJECT_ADDITIONAL_PROPERTY_CONSTRAINT_MSG(OBJECT_ADDITIONAL_PROPERTY_CONSTRAINT, "no additional properties allowed: %s"),
    OBJECT_UNEVALUATED_PROPERTY_CONSTRAINT_MSG(OBJECT_UNEVALUATED_PROPERTY_CONSTRAINT, "unevaluated property found: %s"),
    
    OBJECT_ALL_OF_CONSTRAINT_MSG(OBJECT_ALL_OF_CONSTRAINT, "object allOf constraint failed"),
    OBJECT_ANY_OF_CONSTRAINT_MSG(OBJECT_ANY_OF_CONSTRAINT, "object anyOf constraint failed"),
    OBJECT_ONE_OF_CONSTRAINT_MSG(OBJECT_ONE_OF_CONSTRAINT, "object oneOf constraint failed"),
    OBJECT_NOT_CONSTRAINT_MSG(OBJECT_NOT_CONSTRAINT, "object not constraint failed"),

    NUMBER_NOT_INTEGER_MSG(NUMBER_NOT_INTEGER, "number %s is not an integer"),
    
    ARRAY_MIN_ITEMS_CONSTRAINT_MSG(ARRAY_MIN_ITEMS_CONSTRAINT, "minItems = %d > array.items.size = %d"),
    ARRAY_MAX_ITEMS_CONSTRAINT_MSG(ARRAY_MAX_ITEMS_CONSTRAINT, "maxItems = %d < array.items.size = %d"),
    ARRAY_UNIQUE_ITEMS_CONSTRAINT_MSG(ARRAY_MAX_ITEMS_CONSTRAINT, "duplicate values in array %s"),
    ARRAY_LENGTH_MISMATCH_MSG(ARRAY_LENGTH_MISMATCH, "array.length = %d not equals items.length = %d"),
    
    ARRAY_CONTAINS_CONSTRAINT_MSG(ARRAY_CONTAINS_CONSTRAINT, "no elements match contains schema"),
    ARRAY_MIN_CONTAINS_CONSTRAINT_MSG(ARRAY_MIN_CONTAINS_CONSTRAINT, "few elements match contains schema: %d < %d"),
    ARRAY_MAX_CONTAINS_CONSTRAINT_MSG(ARRAY_MAX_CONTAINS_CONSTRAINT, "too many elements match contains schema: %d > %d"),

    ARRAY_UNEVALUATED_ITEM_CONSTRAINT_MSG(OBJECT_UNEVALUATED_PROPERTY_CONSTRAINT, "unevaluated array item: [%s]"),
    
    STRING_MIN_LENGTH_CONSTRAINT_MSG(STRING_MIN_LENGTH_CONSTRAINT, "string.length %d < minLength = %d"),
    STRING_MAX_LENGTH_CONSTRAINT_MSG(STRING_MAX_LENGTH_CONSTRAINT, "string.length %d > maxLength = %d"),
    
    STRING_PATTERN_CONSTRAINT_MSG(STRING_PATTERN_CONSTRAINT, "string.pattern constraint '%s' %s"),
    STRING_DATE_TIME_FORMAT_CONSTRAINT_MSG(STRING_DATE_TIME_FORMAT_CONSTRAINT, "invalid datetime format: '%s'"),
    STRING_EMAIL_FORMAT_CONSTRAINT_MSG(STRING_EMAIL_FORMAT_CONSTRAINT, "invalid email format: '%s'"),
    STRING_HOSTNAME_FORMAT_CONSTRAINT_MSG(STRING_HOSTNAME_FORMAT_CONSTRAINT, "invalid hostname format: '%s'"),
    STRING_IP4_FORMAT_CONSTRAINT_MSG(STRING_IP4_FORMAT_CONSTRAINT, "invalid IPv4 format: '%s'"),
    STRING_IP6_FORMAT_CONSTRAINT_MSG(STRING_IP6_FORMAT_CONSTRAINT, "invalid IPv6 format: '%s'"),
    STRING_URI_FORMAT_CONSTRAINT_MSG(STRING_URI_FORMAT_CONSTRAINT, "invalid URI format: '%s'"),
    
    NUMBER_MIN_CONSTRAINT_MSG(NUMBER_MIN_CONSTRAINT, "value = %s %s number.minimum = %s"),
    NUMBER_MAX_CONSTRAINT_MSG(NUMBER_MAX_CONSTRAINT, "value = %s %s number.maximum = %s"),
    NUMBER_MULTIPLE_OF_CONSTRAINT_MSG(NUMBER_MULTIPLE_OF_CONSTRAINT, "value = %s in not multipleOf = %s"),

    INTEGER_MIN_CONSTRAINT_MSG(INTEGER_MIN_CONSTRAINT, "value = %d %s integer.minimum = %d"),
    INTEGER_MAX_CONSTRAINT_MSG(INTEGER_MAX_CONSTRAINT, "value = %d %s integer.maximum = %d"),
    
    ENUM_INVALID_VALUE_TYPE_MSG(ENUM_INVALID_VALUE_TYPE, "invalid value type for enum: %s"),
    ENUM_INVALID_VALUE_MSG(ENUM_INVALID_VALUE, "value = %s enum.values = %s"),
    
    CONST_CONSTRAINT_MSG(CONST_CONSTRAINT, "value = %s doesn't match const %s");
    
    public final int CODE;
    public final String VALUE;
    
    private ValidationMessage(final int code, final String value) {
        CODE = code;
        VALUE = value;
    }
}
