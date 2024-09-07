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
import es.elixir.bsc.json.schema.JsonSchemaValidationCallback;
import es.elixir.bsc.json.schema.JsonSchemaVersion;
import es.elixir.bsc.json.schema.ParsingError;
import es.elixir.bsc.json.schema.ParsingMessage;
import es.elixir.bsc.json.schema.ValidationError;
import es.elixir.bsc.json.schema.ValidationException;
import java.util.List;
import es.elixir.bsc.json.schema.impl.JsonSubschemaParser;
import es.elixir.bsc.json.schema.model.JsonDynamicReference;
import es.elixir.bsc.json.schema.model.JsonRecursiveReference;
import es.elixir.bsc.json.schema.model.JsonReference;
import es.elixir.bsc.json.schema.model.JsonSchema;
import es.elixir.bsc.json.schema.model.PrimitiveSchema;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Stream;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.util.Map;
import java.util.Objects;

/**
 * Primitive empty Json Schema of any type ("object", "array", "string", etc.)
 * 
 * @author Dmitry Repchevsky
 */

public class PrimitiveSchemaImpl extends AbstractJsonSchema<JsonObject>
        implements PrimitiveSchema<AbstractJsonSchema> {

    private String title;
    private String description;
    
    private String anchor;
    private String dynamic_anchor;
    private Boolean recursive_anchor;
    
    private JsonAllOfImpl allOf;
    private JsonAnyOfImpl anyOf;
    private JsonOneOfImpl oneOf;
    private JsonNotImpl not;
    
    private AbstractJsonSchema _if;
    private AbstractJsonSchema _then;
    private AbstractJsonSchema _else;
    
    /*
     * Starting from 2019-09 $ref may not substitute the enclosing schema 
     * ("Other keywords are now allowed alongside of it") and is modeled
     * as a property.
     */
    private AbstractJsonReferenceImpl ref;
    
    public PrimitiveSchemaImpl(AbstractJsonSchemaElement parent, 
            JsonSchemaLocator locator, String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public Stream<AbstractJsonSchemaElement> getChildren() {
        // clone immediate children and set their parent to 'this'
        final Stream<AbstractJsonSchemaElement> children =
                Stream.of(allOf, anyOf, oneOf, not, _if, _then, _else, ref)
                        .filter(Objects::nonNull)
                        .map(c -> c.relink(this));
        
        return children.flatMap(AbstractJsonSchemaElement::getChildren);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getAnchor() {
        return anchor;
    }

    @Override
    public String getDynamicAnchor() {
        return dynamic_anchor;
    }

    @Override
    public Boolean getRecursiveAnchor() {
        return recursive_anchor;
    }

    @Override
    public JsonAllOfImpl getAllOf() {
        return allOf;
    }
    
    @Override
    public JsonAnyOfImpl getAnyOf() {
        return anyOf;
    }
    
    @Override
    public JsonOneOfImpl getOneOf() {
        return oneOf;
    }
    
    @Override
    public JsonNotImpl getNot() {
        return not;
    }

    @Override
    public AbstractJsonSchema getIf() {
        return _if;
    }

    @Override
    public AbstractJsonSchema getThen() {
        return _then;
    }

    @Override
    public AbstractJsonSchema getElse() {
        return _else;
    }

    @Override
    public JsonReference getReference() {
        return ref;
    }
    
    @Override
    public PrimitiveSchemaImpl read(JsonSubschemaParser parser, JsonObject object)
            throws JsonSchemaException {

        final JsonString jtitle = JsonSchemaUtil.check(object.get(TITLE), JsonValue.ValueType.STRING);
        if (jtitle != null) {
            setTitle(jtitle.getString());
        }
        
        final JsonString jdescription = JsonSchemaUtil.check(object.get(DESCRIPTION), JsonValue.ValueType.STRING);
        if (jdescription != null) {
            setDescription(jdescription.getString());
        }

        final JsonString janchor = JsonSchemaUtil.check(object.get(JsonSchema.ANCHOR), JsonValue.ValueType.STRING);
        if (janchor != null) {
            anchor = janchor.getString();
            locator.resolve(URI.create("#" + anchor)).setSchema(object);
        }

        final JsonValue jrecursiveanchor = object.get(RECURSIVE_ANCHOR);
        if (jrecursiveanchor != null) {
            switch(jrecursiveanchor.getValueType()) {
                case TRUE:  recursive_anchor = true; break;
                case FALSE: recursive_anchor = false; break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       RECURSIVE_ANCHOR, jrecursiveanchor.getValueType().name(), "must be boolean"));
            }
        }

        final JsonString jdynamicanchor = JsonSchemaUtil.check(object.get(JsonSchema.DYNAMIC_ANCHOR), JsonValue.ValueType.STRING);
        if (jdynamicanchor != null) {
            dynamic_anchor = jdynamicanchor.getString();
            locator.resolve(URI.create("#" + dynamic_anchor)).setSchema(object);
        }

        JsonValue jdefs = object.get(JsonSchema.DEFS);
        if (jdefs != null) {
            if (JsonValue.ValueType.OBJECT != jdefs.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                        JsonSchema.DEFS, jdefs.getValueType().name(), JsonValue.ValueType.OBJECT.name()));
            }
            for (Map.Entry<String, JsonValue> entry : jdefs.asJsonObject().entrySet()) {
                parser.parse(locator, this, getJsonPointer() + "/" + JsonSchema.DEFS + "/" + entry.getKey(), entry.getValue(), null);
            }
        }

        JsonValue jdefinitions = object.get("definitions");
        if (jdefinitions != null) {
            if (JsonValue.ValueType.OBJECT != jdefinitions.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                       "definitions", jdefinitions.getValueType().name(), JsonValue.ValueType.OBJECT.name()));
            }
            for (Map.Entry<String, JsonValue> entry : jdefinitions.asJsonObject().entrySet()) {
                parser.parse(locator, this, getJsonPointer() + "/definitions/" + entry.getKey(), entry.getValue(), null);
            }
        }

        final JsonArray jallOf = JsonSchemaUtil.check(object.get(ALL_OF), JsonValue.ValueType.ARRAY);
        if (jallOf != null) {
            final JsonAllOfImpl _allOf = new JsonAllOfImpl(this, locator, getJsonPointer() + "/" + ALL_OF)
                    .read(parser, jallOf);
            if (allOf == null) {
                allOf = _allOf;
            } else {
                for (AbstractJsonSchema schema : _allOf) {
                    allOf.add(schema);
                }
            }
        }
        
        final JsonArray janyOf = JsonSchemaUtil.check(object.get(ANY_OF), JsonValue.ValueType.ARRAY);
        if (janyOf != null) {
            anyOf = new JsonAnyOfImpl(this, locator, getJsonPointer() + "/" + ANY_OF);
            anyOf.read(parser, janyOf);
        }
        
        final JsonArray joneOf = JsonSchemaUtil.check(object.get(ONE_OF), JsonValue.ValueType.ARRAY);
        if (joneOf != null) {
            oneOf = new JsonOneOfImpl(this, locator, getJsonPointer() + "/" + ONE_OF);
            oneOf.read(parser, joneOf);
        }

        final JsonValue jnot = object.get(NOT);
        if (jnot != null) {
            switch(jnot.getValueType()) {
                case OBJECT:
                case TRUE:
                case FALSE: not = new JsonNotImpl(this, locator, getJsonPointer() + "/" + NOT)
                                        .read(parser, jnot);
                            break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       NOT, jnot.getValueType().name(), "either object or boolean"));
            }
        }

        final JsonValue jif = object.get(IF);
        if (jif != null) {
            _if = parser.parse(locator, this, getJsonPointer() + "/" + IF, jif, null);
        }

        final JsonValue jelse = object.get(ELSE);
        if (jelse != null) {
            _else = parser.parse(locator, this, getJsonPointer() + "/" + ELSE, jelse, null);
        }

        final JsonValue jthen = object.get(THEN);
        if (jthen != null) {
            _then = parser.parse(locator, this, getJsonPointer() + "/" + THEN, jthen, null);
        }
        
        final JsonValue jref = object.get(JsonReference.REF);
        if (jref != null && JsonSchemaVersion.SCHEMA_DRAFT_2019_09.compareTo(
                parser.getJsonSchemaVersion(locator)) <= 0) {
            if (JsonValue.ValueType.STRING != jref.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                       JsonReference.REF, jref.getValueType().name(), JsonValue.ValueType.STRING.name()));
            }

            ref = new JsonReferenceImpl(this, locator, jsonPointer).read(parser, object);
        }

        final JsonValue jdynamic_ref = object.get(JsonDynamicReference.DYNAMIC_REF);
        if (jdynamic_ref != null) {
            if (JsonValue.ValueType.STRING != jdynamic_ref.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                       JsonDynamicReference.DYNAMIC_REF, jdynamic_ref.getValueType().name(), 
                           JsonValue.ValueType.STRING.name()));
            }

            if (jref != null) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INCOMPATIBLE_KEYWORDS, 
                            String.join(",", List.of(JsonRecursiveReference.REF, JsonDynamicReference.DYNAMIC_REF))));
            }
            ref = new JsonDynamicReferenceImpl(this, locator, jsonPointer).read(parser, object);
        }

        final JsonValue jrecursive_ref = object.get(JsonRecursiveReference.RECURSIVE_REF);
        if (jrecursive_ref != null) {
            if (JsonValue.ValueType.STRING != jrecursive_ref.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                        JsonRecursiveReference.RECURSIVE_REF, jrecursive_ref.getValueType().name(), 
                           JsonValue.ValueType.STRING.name()));
            }

            if (jref != null) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INCOMPATIBLE_KEYWORDS, 
                        String.join(",", List.of(JsonRecursiveReference.REF, JsonRecursiveReference.RECURSIVE_REF))));
            }
            
            ref = new JsonRecursiveReferenceImpl(this, locator, jsonPointer).read(parser, object);
        }
        
        return this;
    }

    @Override
    public boolean validate(String jsonPointer, JsonValue value, JsonValue parent, 
            List evaluated, List<ValidationError> errors,
            JsonSchemaValidationCallback<JsonValue> callback) throws ValidationException {

        final int nerrors = errors.size();
        
        final List eva = new ArrayList();
        if (allOf != null) {
            final List e = new ArrayList(evaluated);
            if (allOf.validate(jsonPointer, value, parent, e, errors, callback)) {
                e.removeAll(eva);
                eva.addAll(e);
            }
        }
        
        if (anyOf != null) {
            final List e = new ArrayList(evaluated);
            if (anyOf.validate(jsonPointer, value, parent, e, errors, callback)) {
                e.removeAll(eva);
                eva.addAll(e);
            }
        }

        if (oneOf != null) {
            final List e = new ArrayList(evaluated);
            if (oneOf.validate(jsonPointer, value, parent, e, errors, callback)) {
                e.removeAll(eva);
                eva.addAll(e);                
            }
        }

        if (not != null) {
            final List e = new ArrayList(evaluated);
            if (not.validate(jsonPointer, value, parent, e, errors, callback)) {
                e.removeAll(eva);
                eva.addAll(e);                
            }
        }
        
        if (_if != null) {
            final List e = new ArrayList(evaluated);
            final AbstractJsonSchema choice;
            if (_if.validate(jsonPointer, value, parent, e, new ArrayList(), callback)) {
                choice = _then;
                e.removeAll(eva);
                eva.addAll(e);
            } else {
                choice = _else;
            }
            if (choice != null) {
                if (choice.validate(jsonPointer, value, parent, e, errors, callback)) {
                    e.removeAll(eva);
                    eva.addAll(e);
                }
            }
        }

        if (ref != null) {
            final List e = new ArrayList(evaluated);
            if (ref.validate(jsonPointer, value, parent, e, errors, callback)) {
                e.removeAll(eva);
                eva.addAll(e);
            }
        }

        if (nerrors == errors.size()) {
            eva.removeAll(evaluated);
            evaluated.addAll(eva);
        }

        return nerrors == errors.size();
    }
}
