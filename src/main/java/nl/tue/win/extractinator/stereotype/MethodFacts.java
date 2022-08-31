package nl.tue.win.extractinator.stereotype;

import java.util.HashMap;

public class MethodFacts extends HashMap<MethodFacts.Type, Object> {

    enum Type {
        words, numConditionals, numLoops, numStatements, numExpressions,
        isAbstract, isFinal, isStatic,
        numVars, numBooleanVars, numPrimitiveVars, numStringVars, numCollectionVars, numMapVars, numArrayVars,
        numParams, numBooleanParams, numPrimitiveParams, numStringParams, numCollectionParams, numMapParams, numArrayParams
    }
}
