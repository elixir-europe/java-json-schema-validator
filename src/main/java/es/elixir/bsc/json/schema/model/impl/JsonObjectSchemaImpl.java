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
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationMessage;
import es.elixir.bsc.json.schema.model.JsonObjectSchema;
import es.elixir.bsc.json.schema.model.JsonProperties;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import es.elixir.bsc.json.schema.model.StringArray;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonDependentProperties;
import es.elixir.bsc.json.schema.model.JsonSchemaElement;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.json.JsonNumber;

/**
 * @author Dmitry Repchevsky
 */

public class JsonObjectSchemaImpl extends PrimitiveSchemaImpl
                                  implements JsonObjectSchema {

    private JsonPropertiesImpl properties;
    private Integer minProperties;
    private Integer maxProperties;
    private JsonStringArray required;
    private JsonPropertiesImpl dependentSchemas;
    private JsonDependentProperties dependentRequired;
    private Boolean additionalProperties;
    private AbstractJsonSchema additionalPropertiesSchema;
    private JsonPropertiesImpl patternProperties;
    private Boolean unevaluatedProperties;
    private AbstractJsonSchema unevaluatedPropertiesSchema;
    private AbstractJsonSchema propertyNames;

    public JsonObjectSchemaImpl(AbstractJsonSchemaElement parent, JsonSchemaLocator locator,
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public Stream<JsonSchemaElement> getChildren() {
        return Stream.of(
                super.getChildren(),
                properties != null ? properties.getChildren() : null,
                propertyNames != null ? propertyNames.getChildren() : null,
                patternProperties != null ? patternProperties.getChildren() : null,
                unevaluatedPropertiesSchema != null ? unevaluatedPropertiesSchema.getChildren() : null,
                dependentSchemas != null ? dependentSchemas.getChildren() : null)
                .flatMap(c -> c);
    }
    
    @Override
    public JsonPropertiesImpl getProperties() {
        return properties;
    }

    @Override
    public Integer getMinProperties() {
        return minProperties;
    }
    
    @Override
    public Integer getMaxProperties() {
        return maxProperties;
    }

    @Override
    public StringArray getRequired() {
        if (required == null) {
            required = new JsonStringArray();
        }
        return required;
    }

    @Override
    public JsonProperties getDependentSchemas() {
        if (dependentSchemas == null) {
            dependentSchemas = new JsonPropertiesImpl(this, getScope(), 
                    getJsonPointer() + "/" + DEPENDENT_SCHEMAS);
        }
        return dependentSchemas;
    }

    @Override
    public JsonDependentProperties getDependentRequired() {
        if (dependentRequired == null) {
            dependentRequired = new JsonDependentPropertiesImpl(this, getScope(), 
                    getJsonPointer() + "/" + DEPENDENT_REQUIRED);
        }
        return dependentRequired;
    }

    @Override
    public Boolean isAdditionalProperties() {
        return additionalProperties;
    }

    @Override
    public AbstractJsonSchema getAdditionalProperties() {
        return additionalPropertiesSchema;
    }
    
    @Override
    public JsonProperties getPatternProperties() {
        return patternProperties;
    }
    
    @Override
    public Boolean isUnevaluatedProperties() {
        return unevaluatedProperties;
    }

    @Override
    public AbstractJsonSchema getUnevaluatedProperties() {
        return unevaluatedPropertiesSchema;
    }

    @Override
    public AbstractJsonSchema getPropertyNames() {
        return propertyNames;
    }

    @Override
    public JsonObjectSchemaImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {
        
        super.read(parser, object);
        
        final JsonObject jproperties = JsonSchemaUtil.check(object.get(PROPERTIES), ValueType.OBJECT);
        if (jproperties != null) {
            properties = new JsonPropertiesImpl(this, getScope(), getJsonPointer() + "/" + PROPERTIES)
                    .read(parser, jproperties);
        }

        final JsonNumber jminProperties = JsonSchemaUtil.check(object.get(MIN_PROPERTIES), ValueType.NUMBER);
        if (jminProperties != null) {
            minProperties = jminProperties.intValue();
        }

        final JsonNumber jmaxProperties = JsonSchemaUtil.check(object.get(MAX_PROPERTIES), ValueType.NUMBER);
        if (jmaxProperties != null) {
            maxProperties = jmaxProperties.intValue();
        }

        final JsonObject jpatternProperties = JsonSchemaUtil.check(object.get(PATTERN_PROPERTIES), ValueType.OBJECT);
        if (jpatternProperties != null) {
            patternProperties = new JsonPropertiesImpl(this, getScope(), getJsonPointer() + "/" + DEPENDENT_SCHEMAS)
                    .read(parser, jpatternProperties);
        }
        
        final JsonArray jrequired = JsonSchemaUtil.check(object.get(REQUIRED), ValueType.ARRAY);
        if (jrequired != null) {
            required = new JsonStringArray().read(jrequired);
        }

        final JsonValue jadditionalProperties = object.get(ADDITIONAL_PROPERTIES);
        if (jadditionalProperties != null) {
            switch(jadditionalProperties.getValueType()) {
                case OBJECT: additionalPropertiesSchema = parser.parse(getScope(), this, getJsonPointer() + "/" + ADDITIONAL_PROPERTIES, jadditionalProperties, null); break;
                case TRUE:   additionalProperties = true; break;
                case FALSE:  additionalProperties = false; break;
                default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                    ADDITIONAL_PROPERTIES, jadditionalProperties.getValueType().name(), "either object or boolean"));
            }
        }
        
        final JsonValue junevaluatedProperties = object.get(UNEVALUATED_PROPERTIES);
        if (junevaluatedProperties != null) {
            switch(junevaluatedProperties.getValueType()) {
                case OBJECT: unevaluatedPropertiesSchema = parser.parse(getScope(), this, getJsonPointer() + "/" + UNEVALUATED_PROPERTIES, junevaluatedProperties, null); break;
                case TRUE:   unevaluatedProperties = true; break;
                case FALSE:  unevaluatedProperties = false; break;
                default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                    UNEVALUATED_PROPERTIES, junevaluatedProperties.getValueType().name(), "either object or boolean"));
            }
        }

        final JsonValue jpropertyNames = object.get(PROPERTY_NAMES);
        if (jpropertyNames != null) {
            switch(jpropertyNames.getValueType()) {
                case OBJECT: propertyNames = new JsonStringSchemaImpl(this, getScope(), getJsonPointer() + "/" + PROPERTY_NAMES).read(parser, jpropertyNames.asJsonObject());
                             break;
                case TRUE:   
                case FALSE:  propertyNames = new BooleanJsonSchemaImpl(this, getScope(), getJsonPointer() + "/" + PROPERTY_NAMES).read(parser, jpropertyNames);
                             break;
                default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                    PROPERTY_NAMES, jpropertyNames.getValueType().name(), "either object or boolean"));
            }
        }
        
        final JsonObject jdependentSchemas = JsonSchemaUtil.check(object.get(DEPENDENT_SCHEMAS), ValueType.OBJECT);
        if (jdependentSchemas != null) {
            dependentSchemas = new JsonPropertiesImpl(this, getScope(), 
                    getJsonPointer() + "/" + DEPENDENT_SCHEMAS).read(parser, jdependentSchemas);
        }

        final JsonObject jdependentRequired = JsonSchemaUtil.check(object.get(DEPENDENT_REQUIRED), ValueType.OBJECT);
        if (jdependentRequired != null) {
            dependentRequired = new JsonDependentPropertiesImpl(this, getScope(), 
                    getJsonPointer() + "/" + DEPENDENT_REQUIRED).read(parser, jdependentRequired);
        }
        
        final JsonObject jdependencies = JsonSchemaUtil.check(object.get(DEPENDENCIES), ValueType.OBJECT);
        if (jdependencies != null) {
            for (Map.Entry<String, JsonValue> dependency : jdependencies.entrySet()) {
                final String name = dependency.getKey();
                final JsonValue value = dependency.getValue();
                
                switch(value.getValueType()) {
                    case OBJECT:
                    case TRUE:
                    case FALSE:  final AbstractJsonSchema schema = parser.parse(getScope(), this, getJsonPointer() + "/" + DEPENDENCIES + "/" + name + "/", value, null);
                                 getDependentSchemas().put(name, schema);
                                 break;
                    case ARRAY:  final StringArray arr = new JsonStringArray().read(value.asJsonArray());
                                 getDependentRequired().put(name, arr);
                                 break;
                    default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_OBJECT_TYPE, 
                                    name + " dependentRequired schema ", value.getValueType().name(), 
                                    JsonValue.ValueType.OBJECT.name() + " or " + JsonValue.ValueType.ARRAY.name()));
                }
            }
        }
        
        return this;
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List evaluated, List<ValidationError> errors,
            JsonSchemaValidationCallback<JsonValue> callback) {

        if (value.getValueType() != JsonValue.ValueType.OBJECT) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.OBJECT_EXPECTED_MSG, value.getValueType().name()));
            return false;
        }
        
        final int nerrors = errors.size();

        super.validate(jsonPointer, value, parent, evaluated, errors, callback);
        
        final JsonObject object = value.asJsonObject();
        
        if (minProperties != null && minProperties > object.size()) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.OBJECT_MIN_PROPERTIES_CONSTRAINT_MSG, minProperties, object.size()));            
        }

        if (maxProperties != null && maxProperties < object.size()) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.OBJECT_MAX_PROPERTIES_CONSTRAINT_MSG, maxProperties, object.size()));            
        }
        
        if (propertyNames != null) {
            for (String name : object.keySet()) {
                propertyNames.validate(Json.createValue(name), errors);
            }
        }
        
        final Set req = new TreeSet(getRequired());
        final ArrayList eva = new ArrayList();
        
        if (properties != null) {
            for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
                final String name = entry.getKey();
                final AbstractJsonSchema property = properties.get(name);
                if (property != null) {
                    eva.add(name);
                    req.remove(name);
                    if (property.validate(jsonPointer + "/" + name, entry.getValue(), value, new ArrayList(), errors, callback)) {
                        evaluated.add(name);
                    }
                }
            }
        }
        
        if (patternProperties != null) {
            for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
                final String name = entry.getKey();
                for (Map.Entry<String, AbstractJsonSchema> property : patternProperties) {
                    final Matcher matcher = Pattern.compile(property.getKey()).matcher(name);
                    if (matcher.find()) {
                        eva.add(name);
                        if (property.getValue().validate(jsonPointer + "/" + name, entry.getValue(), value, new ArrayList(), errors, callback)) {
                            evaluated.add(name);
                        }
                    }
                }
            }
        }
        
        if (Boolean.FALSE.equals(additionalProperties)) {
            for (String name : object.keySet()) {
                if (!eva.contains(name)) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.OBJECT_ADDITIONAL_PROPERTY_CONSTRAINT_MSG, name));
                }
            }
        } else if (additionalPropertiesSchema != null) {
            for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
                final String name = entry.getKey();
                if (!eva.contains(name) &&
                    additionalPropertiesSchema.validate(jsonPointer + "/" + name, entry.getValue(), object, new ArrayList(), errors, callback)) {
                    evaluated.add(name);
                }
            }            
        } else {
            // allowed additinal properties
            if (Boolean.TRUE.equals(additionalProperties)) {
                for (String name : object.keySet()) {
                    if (!evaluated.contains(name)) {
                        evaluated.add(name);
                    }
                }
            }
            req.removeAll(object.keySet());
        }
        
        for (Iterator<String> i = req.iterator(); i.hasNext();) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.OBJECT_REQUIRED_PROPERTY_CONSTRAINT_MSG, i.next()));
        }

        if (dependentSchemas != null) {
            for (Map.Entry<String, AbstractJsonSchema> property : dependentSchemas) {
                final String name = property.getKey();
                if (object.containsKey(name)) {
                    eva.clear();
                    final AbstractJsonSchema dependentSchema = property.getValue();
                    if (dependentSchema.validate(jsonPointer + "/" + name, value, parent, eva, errors, callback)) {
                        eva.removeAll(evaluated);
                        evaluated.addAll(eva);
                    }
                }
            }
        }

        if (dependentRequired != null) {
            for (Map.Entry<String, StringArray> property : dependentRequired) {
                final String name = property.getKey();
                if (object.containsKey(name)) {
                    final StringArray arr = property.getValue();
                    for (String dname : arr) {
                        if (!object.containsKey(dname)) {
                            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                                ValidationMessage.OBJECT_DEPENDENT_REQUIRED_CONSTRAINT_MSG, name));                            
                        }
                    }
                }
            }
        }

        if (Boolean.FALSE.equals(unevaluatedProperties)) {
            for (String name : object.keySet()) {
                if (evaluated.contains(name)) {
                    continue;
                }
                errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.OBJECT_UNEVALUATED_PROPERTY_CONSTRAINT_MSG, name));
            }
        } else if (Boolean.TRUE.equals(unevaluatedProperties)) {
            for (String name : object.keySet()) {
                if (!evaluated.contains(name)) {
                    evaluated.add(name);
                }
            }
        } else if (unevaluatedPropertiesSchema != null) {
            for (Map.Entry<String, JsonValue> entry : object.entrySet()) {
                final String name = entry.getKey();
                if (evaluated.contains(name) ||
                    unevaluatedPropertiesSchema.validate(jsonPointer + "/" + name, entry.getValue(), object, new ArrayList(), errors, callback)) {
                    continue;
                }
                errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.OBJECT_UNEVALUATED_PROPERTY_CONSTRAINT_MSG, name));
            }            
        }

        if (callback != null) {
            callback.validated(this, jsonPointer, value, parent, errors);
        }
        
        return nerrors == errors.size();
    }
}
