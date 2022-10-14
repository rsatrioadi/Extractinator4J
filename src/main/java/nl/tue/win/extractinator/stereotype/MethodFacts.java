package nl.tue.win.extractinator.stereotype;

import java.util.HashMap;

public class MethodFacts extends HashMap<MethodFacts.Type, Object> {

    enum Type {
        words, numConditionals, numLoops, numStatements, numExpressions, numUniqueWords,
        isAbstract, isFinal, isStatic,
        isNamedGetter, isNamedSetter,
        numVars, numBooleanVars, numPrimitiveVars, numStringVars, numCollectionVars, numMapVars, numArrayVars,
        numParams, numBooleanParams, numPrimitiveParams, numStringParams, numCollectionParams, numMapParams, numArrayParams,

        isGetter, isPredicate, isProperty, isSetter, isCommand,
        isFactory, numCtorCalls,
        numAssignmentsToField, numCallsToField, numFieldsInvolved, numInternalCalls, numExternalCalls,
        numCallsToParam, numCallsToVars,
        numMathOperations, numBoolOperations, numComparisons, numAssignments
    }
}
