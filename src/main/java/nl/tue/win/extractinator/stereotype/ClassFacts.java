package nl.tue.win.extractinator.stereotype;

import java.util.HashMap;

public class ClassFacts extends HashMap<ClassFacts.Type, Object> {

    enum Type {
        words, numConditionals, numLoops, numStatements, numExpressions, numUniqueWords, numAssignemnts,
        isInterface, isClass, isEnum, isAbstract, isFinal, isAnonymous, isStatic,
        numFields, numBooleanFields, numPrimitiveFields, numStringFields, numCollectionFields, numMapFields, numArrayFields,
        numHiddenFields, numVisibleFields, numStaticFields,
        numNamedGetters, numNamedSetters,
        numMethods, numHiddenMethods, numVisibleMethods, numStaticMethods,
        numAncestors,
        isCollection, isMap,
        specializesListener, specializesAdapter,
        isNamedManager, isNamedController,
        invokesIO,
        numOutboundCalls, numCtorCalls,
        numMathOperations, numBoolOperations, numComparisons,

        maxNumUniqueWords,
        maxNumConditionals, maxNumLoops, maxNumStatements, maxNumExpressions,
        maxNumVars, maxNumBooleanVars, maxNumPrimitiveVars, maxNumStringVars, maxNumCollectionVars, maxNumMapVars, maxNumArrayVars,
        maxNumParams, maxNumBooleanParams, maxNumPrimitiveParams, maxNumStringParams, maxNumCollectionParams, maxNumMapParams, maxNumArrayParams,

        roleStereotype,
    }
}
