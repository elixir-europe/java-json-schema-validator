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
import es.elixir.bsc.json.schema.model.JsonRecursiveReference;
import es.elixir.bsc.json.schema.model.JsonReference;
import es.elixir.bsc.json.schema.model.JsonSchema;
import es.elixir.bsc.json.schema.model.JsonType;
import es.elixir.bsc.json.schema.model.PrimitiveSchema;
import java.net.URI;
import java.util.ArrayList;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Primitive empty Json Schema of any type ("object", "array", "string", etc.)
 * 
 * @author Dmitry Repchevsky
 */

public class PrimitiveSchemaImpl extends JsonSchemaImpl<JsonObject>
        implements PrimitiveSchema<AbstractJsonSchema> {

    private JsonSchemaLocator scope;

    private String title;
    private String description;
    
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
    
    private boolean recursiveAnchor;
    
    public PrimitiveSchemaImpl(JsonSchemaImpl parent, JsonSchemaLocator locator, 
            String jsonPointer) {
        super(parent, locator, jsonPointer);
    }

    @Override
    public JsonSchemaLocator getCurrentScope() {
        return scope;
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
    public boolean isRecursiveAnchor() {
        return recursiveAnchor;
    }
    
    @Override
    public PrimitiveSchemaImpl read(final JsonSubschemaParser parser,
                                    final JsonObject object, 
                                    final JsonType type) throws JsonSchemaException {

        super.read(parser, object, type);
        
        JsonValue $id = object.get(JsonSchema.ID);
        if ($id == null) {
            $id = object.get("id"); // draft4
        } 

        if ($id == null) {
            scope = locator;
        } else if ($id.getValueType() != JsonValue.ValueType.STRING) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                   new Object[] {"id", $id.getValueType().name(), JsonValue.ValueType.STRING.name()}));
        } else {
            final String id = ((JsonString)$id).getString();
            try {
                scope = locator.resolve(URI.create(id));
                scope.setSchema(object);
            } catch(IllegalArgumentException ex) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_REFERENCE,
                                              new Object[] {id}));
            }
        }

        final JsonString jtitle = JsonSchemaUtil.check(object.get(TITLE), JsonValue.ValueType.STRING);
        if (jtitle != null) {
            setTitle(jtitle.getString());
        }
        
        final JsonString jdescription = JsonSchemaUtil.check(object.get(DESCRIPTION), JsonValue.ValueType.STRING);
        if (jdescription != null) {
            setDescription(jdescription.getString());
        }

        final JsonString $anchor = JsonSchemaUtil.check(object.get(JsonSchema.ANCHOR), JsonValue.ValueType.STRING);
        if ($anchor != null) {
            final String anchor = $anchor.getString();
            scope.resolve(URI.create("#" + anchor)).setSchema(object);
        }
        
        final JsonValue jrecursiveAnchor = object.get(RECURSIVE_ANCHOR);
        if (jrecursiveAnchor != null) {
            switch(jrecursiveAnchor.getValueType()) {
                case TRUE: recursiveAnchor = true;
                case FALSE: break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       new Object[] {RECURSIVE_ANCHOR, jrecursiveAnchor.getValueType().name(), "only boolean is allowed"}));
            }
        }

        final JsonArray jallOf = JsonSchemaUtil.check(object.get(ALL_OF), JsonValue.ValueType.ARRAY);
        if (jallOf != null) {
            final JsonAllOfImpl _allOf = new JsonAllOfImpl(this, scope, getJsonPointer() + "/" + ALL_OF)
                    .read(parser, jallOf, type);
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
            anyOf = new JsonAnyOfImpl(this, scope, getJsonPointer() + "/" + ANY_OF);
            anyOf.read(parser, janyOf, type);
        }
        
        final JsonArray joneOf = JsonSchemaUtil.check(object.get(ONE_OF), JsonValue.ValueType.ARRAY);
        if (joneOf != null) {
            oneOf = new JsonOneOfImpl(this, scope, getJsonPointer() + "/" + ONE_OF);
            oneOf.read(parser, joneOf, type);
        }

        final JsonValue jnot = object.get(NOT);
        if (jnot != null) {
            switch(jnot.getValueType()) {
                case OBJECT:
                case TRUE:
                case FALSE: not = new JsonNotImpl(this, scope, getJsonPointer() + "/" + NOT)
                                        .read(parser, jnot, null);
                            break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       new Object[] {NOT, jnot.getValueType().name(), "either object or boolean"}));
            }
        }

        final JsonValue jif = object.get(IF);
        if (jif != null) {
            switch(jif.getValueType()) {
                case OBJECT: _if = parser.parse(scope, this, getJsonPointer() + "/" + IF, jif, null);
                             break;
                case TRUE:
                case FALSE:  _if = new BooleanJsonSchemaImpl(this, scope, getJsonPointer()).read(parser, jif, null);
                             break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       new Object[] {IF, jif.getValueType().name(), "either object or boolean"}));                             
            }
        }

        final JsonValue jelse = object.get(ELSE);
        if (jelse != null) {
            switch(jelse.getValueType()) {
                case OBJECT: _else = parser.parse(scope, this, getJsonPointer() + "/" + ELSE, jelse, null);
                             break;
                case TRUE:
                case FALSE:  _else = new BooleanJsonSchemaImpl(this, scope, getJsonPointer()).read(parser, jelse, null);
                             break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       new Object[] {ELSE, jelse.getValueType().name(), "either object or boolean"}));                             
            }
        }

        final JsonValue jthen = object.get(THEN);
        if (jthen != null) {
            switch(jthen.getValueType()) {
                case OBJECT: _then = parser.parse(scope, this, getJsonPointer() + "/" + THEN, jthen, null);
                             break;
                case TRUE:
                case FALSE:  _then = new BooleanJsonSchemaImpl(this, scope, getJsonPointer()).read(parser, jthen, null);
                             break;
                default: throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                                       new Object[] {THEN, jthen.getValueType().name(), "either object or boolean"}));                             
            }
        }
        
        final JsonValue jref = object.get(JsonReference.REF);
        if (jref != null && JsonSchemaVersion.SCHEMA_DRAFT_2019_09.compareTo(
                parser.getJsonSchemaVersion(object)) <= 0) {
            if (JsonValue.ValueType.STRING != jref.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                       new Object[] {JsonReference.REF, jref.getValueType().name(), JsonValue.ValueType.STRING.name()}));
            }

            ref = new JsonReferenceImpl(this, scope, getJsonPointer()).read(parser, object, null);
        }

        final JsonValue jrecursive_ref = object.get(JsonRecursiveReference.RECURSIVE_REF);
        if (jrecursive_ref != null) {
            if (JsonValue.ValueType.STRING != jrecursive_ref.getValueType()) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INVALID_ATTRIBUTE_TYPE, 
                       new Object[] {JsonRecursiveReference.RECURSIVE_REF, jrecursive_ref.getValueType().name(), 
                           JsonValue.ValueType.STRING.name()}));
            }

            if (jref != null) {
                throw new JsonSchemaException(new ParsingError(ParsingMessage.INCOMPATIBLE_KEYWORDS, 
                       new Object[] {String.join(",", 
                               List.of(JsonRecursiveReference.REF, JsonRecursiveReference.RECURSIVE_REF))}));
            }
            
            ref = new JsonRecursiveReferenceImpl(this, scope, getJsonPointer()).read(parser, object, null);
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
