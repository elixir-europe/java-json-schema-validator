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
import es.elixir.bsc.json.schema.model.JsonArraySchema;
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import java.util.ArrayList;
import java.util.List;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author Dmitry Repchevsky
 */

public class JsonArraySchemaImpl extends PrimitiveSchemaImpl
                                 implements JsonArraySchema {

    private final List<AbstractJsonSchema> items;
    
    // the items are actually a 'prefixItems'
    private boolean prefixItems;
    
    private Boolean additionalItems;
    private Boolean uniqueItems;
    private AbstractJsonSchema additionalItemsSchema;
    private Boolean unevaluatedItems;
    private AbstractJsonSchema unevaluatedItemsSchema;
    
    private Long minItems;
    private Long maxItems;
    
    private AbstractJsonSchema contains;

    private Long minContains;
    private Long maxContains;

    public JsonArraySchemaImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator locator, String jsonPointer) {
        super(parent, locator, jsonPointer);
        
        items = new ArrayList();
    }

    @Override
    public Stream<AbstractJsonSchemaElement> getChildren() {

        // clone children and set their parent to 'this'
        final Stream<AbstractJsonSchemaElement> children = Stream.concat(
                Optional.ofNullable(items).map(Collection::stream).orElseGet(Stream::empty),
                Stream.of(additionalItemsSchema, unevaluatedItemsSchema, contains)
                        .filter(Objects::nonNull))
                        .map(c -> c.relink(this));
                        
        return Stream.concat(
                super.getChildren(),
                children.flatMap(e -> Stream.concat(Stream.of(e), e.getChildren())));
    }

    @Override
    public List<AbstractJsonSchema> getItems() {
        // in 2020-12 'items' is a schema
        return prefixItems ? additionalItemsSchema != null ? List.of(additionalItemsSchema) : null : items;
    }

    @Override
    public List<AbstractJsonSchema> getPrefixItems() {
        // 2019-09 'items' == 2020-12 'prefixItems'
        // 2019-09 'additionalItemsSchema' == 2020-12 'items'
        return prefixItems ? items : null;
    }

    @Override
    public Long getMinItems() {
        return minItems;
    }
    
    @Override
    public void setMinItems(Long minItems) {
        this.minItems = minItems;
    }

    @Override
    public Long getMaxItems() {
        return maxItems;
    }
    
    @Override
    public void setMaxItems(Long maxItems) {
        this.maxItems = maxItems;
    }
    
    @Override
    public Boolean isUniqueItems() {
        return uniqueItems;
    }

    @Override
    public AbstractJsonSchema getAdditionalItems() {
        return additionalItemsSchema;
    }

    @Override
    public AbstractJsonSchema getUnevaluatedItems() {
        return unevaluatedItemsSchema;
    }
    
    @Override
    public AbstractJsonSchema getContains() {
        return contains;
    }
    
    @Override
    public Long getMinContains() {
        return minContains;
    }
    
    @Override
    public Long getMaxContains() {
        return maxContains;
    }
    
    @Override
    public JsonArraySchemaImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {

        super.read(parser, object);

        final JsonNumber min = JsonSchemaUtil.check(object.getJsonNumber(MIN_ITEMS), JsonValue.ValueType.NUMBER);
        if (min != null) {
            minItems = min.longValue();
        }
        
        final JsonNumber max = JsonSchemaUtil.check(object.getJsonNumber(MAX_ITEMS), JsonValue.ValueType.NUMBER);
        if (max != null) {
            maxItems = max.longValue();
        }

        final JsonValue juniqueItems = object.get(UNIQUE_ITEMS);
        if (juniqueItems != null) {
            switch(juniqueItems.getValueType()) {
                case TRUE: uniqueItems = true; break;
                case FALSE: uniqueItems = false; break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       UNIQUE_ITEMS, juniqueItems.getValueType().name(), "must be boolean"));
            }
        }

        final JsonValue jcontains = object.get(CONTAINS);
        if (jcontains != null) {
            contains = parser.parse(locator, this, getJsonPointer() + "/" + CONTAINS, jcontains, null);
        }

        final JsonNumber jminContains = JsonSchemaUtil.check(object.getJsonNumber(MIN_CONTAINS), JsonValue.ValueType.NUMBER);
        if (jminContains != null) {
            minContains = jminContains.longValue();
        }

        final JsonNumber jmaxContains = JsonSchemaUtil.check(object.getJsonNumber(MAX_CONTAINS), JsonValue.ValueType.NUMBER);
        if (jmaxContains != null) {
            maxContains = jmaxContains.longValue();
        }

        JsonValue jitems = object.get(ITEMS);
        JsonValue jadditionalItems = object.get(ADDITIONAL_ITEMS);
        JsonArray jprefixitems = JsonSchemaUtil.check(object.get(PREFIX_ITEMS), JsonValue.ValueType.ARRAY);
        if (jprefixitems != null) {
            // in 2020-12 when 'prefixItems' is defined 'items' are 'additionalItems'
            prefixItems = true;
            jadditionalItems = jitems;
        } else if (jitems != null) {
            switch(jitems.getValueType()) {
                case OBJECT:
                case TRUE:
                case FALSE: final AbstractJsonSchema schema = parser.parse(locator, this, getJsonPointer() + "/" + ITEMS, jitems, null);
                            items.add(schema);
                            break;
                case ARRAY: jprefixitems = jitems.asJsonArray();
                            break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                ITEMS, jitems.getValueType().name(), "either an object, boolean or an array"));
            }
        }
        
        if (jprefixitems != null) {
            additionalItems = true;
            
            final String propertyName = prefixItems ? PREFIX_ITEMS : ITEMS;
            
            for (int i = 0, n = jprefixitems.size(); i < n; i++) {
                final JsonValue value = jprefixitems.get(i);
                switch(value.getValueType()) {
                    case OBJECT:
                    case TRUE:
                    case FALSE: final AbstractJsonSchema arr = parser.parse(locator, this, getJsonPointer() + "/" + propertyName + "/" + i, value, null);
                                items.add(arr);
                                break;
                    default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                 propertyName + "/" + i, value.getValueType().name(), "either an object or boolean"));
                }
            }
        }

        if (additionalItems != null && jadditionalItems != null) {
            final String propertyName = prefixItems ? ITEMS : ADDITIONAL_ITEMS;
            
            switch(jadditionalItems.getValueType()) {
                case OBJECT: break;
                case FALSE: additionalItems = false;
                case TRUE: break;
                default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                        propertyName, jadditionalItems.getValueType().name(), "either object or boolean"));
            }
            additionalItemsSchema = parser.parse(locator, this, getJsonPointer() + "/" + propertyName, jadditionalItems, null);
        }

        final JsonValue junevaluatedItems = object.get(UNEVALUATED_ITEMS);
        if (junevaluatedItems != null) {
            switch(junevaluatedItems.getValueType()) {
                case OBJECT: unevaluatedItems = null;
                             unevaluatedItemsSchema = parser.parse(locator, this, getJsonPointer() + "/" + UNEVALUATED_ITEMS, junevaluatedItems, null);
                             break;
                case TRUE:   unevaluatedItems = true; break;
                case FALSE:  unevaluatedItems = false; break;
                default:     throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                    UNEVALUATED_ITEMS, junevaluatedItems.getValueType().name(), "either object or boolean"));
            }
        }

        return this;
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List evaluated, List<ValidationError> errors,
            JsonSchemaValidationCallback<JsonValue> callback) {

        if (value.getValueType() != JsonValue.ValueType.ARRAY) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer, 
                    ValidationMessage.ARRAY_EXPECTED_MSG, value.getValueType().name()));
            return false;
        }
        
        final int nerrors = errors.size();
        
        super.validate(jsonPointer, value, parent, evaluated, errors, callback);

        final JsonArray array = value.asJsonArray();
        
        if (minItems != null && array.size() < minItems) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.ARRAY_MIN_ITEMS_CONSTRAINT_MSG, minItems, items == null ? 0 : array.size()));
        }

        if (maxItems != null && array.size() > maxItems) {
            errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                    ValidationMessage.ARRAY_MAX_ITEMS_CONSTRAINT_MSG, maxItems, items == null ? 0 : items.size()));
        }

        if (contains != null) {
            final List eva = new ArrayList();
            final List<ValidationError> err = new ArrayList<>();
            int cnt = 0;
            for (int i = 0, n = array.size(); i < n; i++) {
                final JsonValue val = array.get(i);
                if (contains.validate(jsonPointer, val, parent, eva, err, callback)) {
                    evaluated.add(i);
                    cnt++;
                }
            }
            
            if (cnt == 0) {
                if (minContains == null) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.ARRAY_CONTAINS_CONSTRAINT_MSG));                    
                } else if (minContains > 0) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.ARRAY_MIN_CONTAINS_CONSTRAINT_MSG, cnt, minContains));                    
                }
            } else {
                if (minContains != null && cnt < minContains) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.ARRAY_MIN_CONTAINS_CONSTRAINT_MSG, cnt, minContains));                    
                }
                if (maxContains != null && cnt > maxContains) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.ARRAY_MAX_CONTAINS_CONSTRAINT_MSG, cnt, maxContains));                    
                }
            }
        }

        if (items != null) {
            if (items.size() == 1 && additionalItems == null && additionalItemsSchema == null) {
                // items is a json object - all values must match the schema
                final AbstractJsonSchema schema = items.get(0);
                for (int i = 0, n = array.size(); i < n; i++) {
                    final JsonValue val = array.get(i);
                    if (schema.validate(jsonPointer + "/" + i, 
                            val, value, new ArrayList(), errors, callback) &&
                        !evaluated.contains(i)) {
                        evaluated.add(i);
                    }
                }
            } else if (array.size() <= items.size()) {
                for (int i = 0, n = array.size(); i < n; i++) {
                    final JsonValue val = array.get(i);
                    if (items.get(i).validate(jsonPointer + "/" + i, 
                            val, value, new ArrayList(), errors, callback) &&
                        !evaluated.contains(i)) {
                        evaluated.add(i);
                    }
                }
            } else if (Boolean.FALSE.equals(additionalItems)) {
                errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                        ValidationMessage.ARRAY_LENGTH_MISMATCH_MSG, array.size(), items.size()));
            } else {
                for (int i = 0, n = items.size(); i < n; i++) {
                    final JsonValue val = array.get(i);
                    if (items.get(i).validate(jsonPointer + "/" + i, 
                            val, value, new ArrayList(), errors, callback) &&
                        !evaluated.contains(i)) {
                        evaluated.add(i);
                    }
                }

                if (additionalItemsSchema != null) {
                    for (int i = items.size(), n = array.size(); i < n; i++) {
                        final JsonValue val = array.get(i);
                        if (additionalItemsSchema.validate(jsonPointer + "/" + i, 
                                    val, value, new ArrayList(), errors, callback) &&
                            !evaluated.contains(i)) {
                            evaluated.add(i);
                        }
                    }
                }

                if (unevaluatedItemsSchema != null) {
                    for (int i = items.size(), n = array.size(); i < n; i++) {
                        if (!evaluated.contains(i)) {
                            final JsonValue val = array.get(i);
                            if (unevaluatedItemsSchema.validate(jsonPointer + "/" + i, 
                                        val, value, new ArrayList(), errors, callback)) {
                                evaluated.add(i);
                            }
                        }
                    }
                }
            }
        }
        
        if (Boolean.TRUE.equals(unevaluatedItems)) {
            final List eva = IntStream.range(0, array.size()).boxed()
                    .collect(Collectors.toList());
            eva.removeAll(evaluated);
            evaluated.addAll(eva);
        } else if (unevaluatedItemsSchema != null || unevaluatedItems != null) {
            for (int i = 0, n = array.size(); i < n; i++) {
                if (!evaluated.contains(i)) {
                    final JsonValue val = array.get(i);
                    if ((additionalItemsSchema != null &&
                        additionalItemsSchema.validate(jsonPointer + "/" + i, 
                                val, value, new ArrayList(), errors, callback)) ||
                        (unevaluatedItemsSchema != null &&
                        unevaluatedItemsSchema.validate(jsonPointer + "/" + i, 
                                val, value, new ArrayList(), errors, callback))) {
                        evaluated.add(i);
                        continue;
                    }
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.ARRAY_UNEVALUATED_ITEM_CONSTRAINT_MSG, i));
                }
            }
        }

        if (Boolean.TRUE.equals(uniqueItems)) {
            final Set values = new HashSet();
            for (int i = 0, n = array.size(); i < n; i++) {
                final JsonValue val = array.get(i);
                final Object o;
                switch(val.getValueType()) {
                    case NUMBER: o = ((JsonNumber)val).doubleValue(); break;
                    default: o = val;
                }
                if (values.contains(o)) {
                    errors.add(new ValidationError(getId(), getJsonPointer(), jsonPointer,
                            ValidationMessage.ARRAY_UNIQUE_ITEMS_CONSTRAINT_MSG, val.toString()));
                } else {
                    values.add(o);
                }
            }
        }

        if (callback != null) {
            callback.validated(this, jsonPointer, value, parent, errors);
        }
        
        return nerrors == errors.size();
    }
}
