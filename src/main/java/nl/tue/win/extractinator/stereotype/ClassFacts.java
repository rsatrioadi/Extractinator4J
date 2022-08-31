package nl.tue.win.extractinator.stereotype;

import java.util.HashMap;

public class ClassFacts extends HashMap<ClassFacts.Type, Object> {

    enum Type {
        words, numConditionals, numLoops, numStatements, numExpressions,
        isInterface, isClass, isEnum, isAbstract, isFinal, isAnonymous, isStatic,
        numFields, numBooleanFields, numPrimitiveFields, numStringFields, numCollectionFields, numMapFields, numArrayFields,
        numHiddenFields, numVisibleFields, numStaticFields,
        numGetters, numSetters,
        numHiddenMethods, numVisibleMethods, numStaticMethods,
        isCollection, isMap
    }
}
