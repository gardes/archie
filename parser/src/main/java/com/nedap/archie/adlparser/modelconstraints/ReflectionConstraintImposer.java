package com.nedap.archie.adlparser.modelconstraints;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CComplexObject;
import com.nedap.archie.aom.Cardinality;
import com.nedap.archie.rminfo.ArchieRMNamingStrategy;
import com.nedap.archie.rminfo.ModelInfoLookup;
import com.nedap.archie.rminfo.ModelNamingStrategy;
import com.nedap.archie.rminfo.RMAttributeInfo;
import com.nedap.archie.rminfo.RMTypeInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ModelConstraintImposer that checks the constraint with java-reflection. javax.annotation.NonNull is implemented
 * as being NonNull. Other attributes are assumed to be non-null. Collection attributes are assumed to be 0..*
 *
 * Fully thread-safe, but rather expensive to create. Caching in a static field is encouraged.
 *
 * Created by pieter.bos on 04/11/15.
 */
public class ReflectionConstraintImposer implements ModelConstraintImposer {

    /** Contains complex object structure of the specified model. Attributes NEVER will have children. Sorry bout that :)*/
    private Map<String, CComplexObject> objects = new ConcurrentHashMap<>();

    //constructed as  a field to save some object creation
    protected PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy lowerCaseWithUnderscoresStrategy = new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy();

    public ReflectionConstraintImposer(String packageName) {
        this(packageName, ReflectionConstraintImposer.class.getClassLoader());
    }

    public ReflectionConstraintImposer(String packageName, ClassLoader classLoader) {
        this(new ArchieRMNamingStrategy(), packageName, classLoader);
    }

    public ReflectionConstraintImposer(ModelNamingStrategy strategy, String packageName, ClassLoader classLoader) {
        this(new ModelInfoLookup(strategy, packageName, classLoader));
    }

    public ReflectionConstraintImposer(ModelInfoLookup classLookup) {
        List<RMTypeInfo> rmTypes = classLookup.getAllTypes();

        for(RMTypeInfo typeInfo:rmTypes) {
            CComplexObject object = new CComplexObject();
            object.setRmTypeName(typeInfo.getRmName());

            for(RMAttributeInfo attributeInfo:typeInfo.getAttributes().values()) {
                CAttribute attribute = new CAttribute();
                attribute.setCardinality(new Cardinality(1,1));
                attribute.setMultiple(false);
                attribute.setRmAttributeName(attributeInfo.getRmName());

                if(attributeInfo.isNullable()) {
                    //TODO: not correct. Should be existence?
                    attribute.setCardinality(new Cardinality(0,1));
                }

                if(attributeInfo.getType() instanceof Class && Collection.class.isAssignableFrom((Class) attributeInfo.getType())) {
                    attribute.setCardinality(Cardinality.unbounded());
                    attribute.setMultiple(true);
                }
                object.addAttribute(attribute);
            }
            objects.put(object.getRmTypeName(), object);

        }
    }

    @Override
    public CAttribute getDefaultAttribute(String typeId, String attribute) {
        CComplexObject object = objects.get(typeId);
        if(object == null) {
            return null;
        }
        return object.getAttribute(attribute);
    }
}
