package nl.tue.win.spoony;

import nl.tue.win.collections.Counter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class MethodVector {

    public static List<MethodVector> stereotypes() {
        return List.of(EMPTY,
                INVOKE_EXT, DECIDE, COMPLEX, INVOKE_INT, SET, SIMPLE_GET);
    }

    public static final MethodVector
            EMPTY = new Builder().setName("EMPTY")
                    .build(),
            INVOKE_EXT = new Builder().setName("INVOKE_EXT")
                    .setVariableRead(2)
                    .setLiteral(1)
                    .setInvocation(2)
                    .build(),
            DECIDE = new Builder().setName("DECIDE")
                    .setFieldRead(1)
                    .setVariableRead(3)
                    .setLiteral(2)
                    .setConditional(2)
                    .setConstructorCall(1)
                    .setInvocation(3)
                    .setReturn(1)
                    .setBinaryOperator(2)
                    .build(),
            COMPLEX = new Builder().setName("COMPLEX")
                    .setFieldRead(3)
                    .setFieldWrite(1)
                    .setVariableRead(3)
                    .setVariableWrite(2)
                    .setLambda(1)
                    .setLiteral(3)
                    .setConditional(2)
                    .setForLoop(1)
                    .setConstructorCall(2)
                    .setInvocation(3)
                    .setReturn(1)
                    .setBinaryOperator(3)
                    .build(),
            INVOKE_INT = new Builder().setName("INVOKE_INT")
                    .setFieldRead(1)
                    .setInvocation(1)
                    .build(),
            SET = new Builder().setName("SET")
                    .setFieldRead(2)
                    .setFieldWrite(1)
                    .setVariableRead(2)
                    .setLiteral(1)
                    .setConditional(1)
                    .setInvocation(2)
                    .setBinaryOperator(1)
                    .build(),
            SIMPLE_GET = new Builder().setName("SIMPLE_GET")
                    .setFieldRead(1)
                    .setReturn(1)
                    .build();


    private final double
            FieldRead, FieldWrite,
            VariableRead, VariableWrite,
            Lambda,
            Literal,
            Conditional,
            ForLoop, WhileLoop,
            ConstructorCall, Invocation,
            Return,
            BinaryOperator;

    private final List<Double> vector;


    MethodVector(
            double fieldRead, double fieldWrite,
            double variableRead, double variableWrite,
            double lambda,
            double literal,
            double conditional,
            double forLoop, double whileLoop,
            double constructorCall, double invocation,
            double aReturn,
            double binaryOperator) {
        FieldRead = fieldRead;
        FieldWrite = fieldWrite;
        VariableRead = variableRead;
        VariableWrite = variableWrite;
        Lambda = lambda;
        Literal = literal;
        Conditional = conditional;
        ForLoop = forLoop;
        WhileLoop = whileLoop;
        ConstructorCall = constructorCall;
        Invocation = invocation;
        Return = aReturn;
        BinaryOperator = binaryOperator;
        vector = List.of(
                FieldRead, FieldWrite,
                VariableRead, VariableWrite,
                Lambda,
                Literal,
                Conditional,
                ForLoop, WhileLoop,
                ConstructorCall, Invocation,
                Return,
                BinaryOperator
        );
    }

    public MethodVector(Counter<String> counter) {
        this(
                counter.get("FieldRead"), counter.get("FieldWrite"),
                counter.get("VariableRead"), counter.get("VariableWrite"),
                counter.get("Lambda"),
                counter.get("Literal"),
                counter.get("Conditional"),
                counter.get("ForLoop"), counter.get("WhileLoop"),
                counter.get("ConstructorCall"), counter.get("Invocation"),
                counter.get("Return"),
                counter.get("BinaryOperator")
        );
    }

    public double getFieldRead() {
        return FieldRead;
    }

    public double getFieldWrite() {
        return FieldWrite;
    }

    public double getVariableRead() {
        return VariableRead;
    }

    public double getVariableWrite() {
        return VariableWrite;
    }

    public double getLambda() {
        return Lambda;
    }

    public double getLiteral() {
        return Literal;
    }

    public double getConditional() {
        return Conditional;
    }

    public double getForLoop() {
        return ForLoop;
    }

    public double getWhileLoop() {
        return WhileLoop;
    }

    public double getConstructorCall() {
        return ConstructorCall;
    }

    public double getInvocation() {
        return Invocation;
    }

    public double getReturn() {
        return Return;
    }

    public double getBinaryOperator() {
        return BinaryOperator;
    }

    public List<Double> asList() {
        return vector;
    }

    public double distance(MethodVector other) {

        Double[] A = vector.toArray(new Double[0]);
        Double[] B = other.vector.toArray(new Double[0]);

//        // cosine similarity
//
//        double sumProduct = 0;
//        double sumASq = 0;
//        double sumBSq = 0;
//        for (double i = 0; i < A.length; i++) {
//            sumProduct += A[i]*B[i];
//            sumASq += A[i] * A[i];
//            sumBSq += B[i] * B[i];
//        }
//        if (sumASq == 0 && sumBSq == 0) {
//            return 2.0;
//        }
//        return sumProduct / (Math.sqrt(sumASq) * Math.sqrt(sumBSq));

        // euclidean distance

        double sum = IntStream.range(0, A.length)
                .parallel()
                .mapToDouble(i -> (B[i] - A[i]))
                .map(delta -> delta * delta)
                .sum();

        return Math.sqrt(sum);
    }

    public List<Double> distancesFromStereotype() {
        return stereotypes().stream()
                .parallel()
                .map(this::distance)
                .toList();
    }

    public List<MethodVector> likelyStereotypes() {
        List<Double> distances = distancesFromStereotype();
        List<MethodVector> sorted = IntStream.range(0, distances.size())
                .boxed()
                .sorted(Comparator.comparing(distances::get))
                .map(idx -> stereotypes().get(idx))
                .toList();
        distances = distances.stream().sorted().toList();
        List<MethodVector> likeliest = new ArrayList<>();
        double delta = 0;
        int i = 0;
        while (delta < 0.01 && i < sorted.size()) {
            likeliest.add(sorted.get(i));
            i++;
            delta = distances.get(i) - distances.get(i-1);
        }
        return likeliest.stream().toList();
    }

    public static class Builder {

        private double
                FieldRead, FieldWrite,
                VariableRead, VariableWrite,
                Lambda,
                Literal,
                Conditional,
                ForLoop, WhileLoop,
                ConstructorCall, Invocation,
                Return,
                BinaryOperator;
        private String name;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFieldRead(double fieldRead) {
            FieldRead = fieldRead;
            return this;
        }

        public Builder setFieldWrite(double fieldWrite) {
            FieldWrite = fieldWrite;
            return this;
        }

        public Builder setVariableRead(double variableRead) {
            VariableRead = variableRead;
            return this;
        }

        public Builder setVariableWrite(double variableWrite) {
            VariableWrite = variableWrite;
            return this;
        }

        public Builder setLambda(double lambda) {
            Lambda = lambda;
            return this;
        }

        public Builder setLiteral(double literal) {
            Literal = literal;
            return this;
        }

        public Builder setConditional(double conditional) {
            Conditional = conditional;
            return this;
        }

        public Builder setForLoop(double forLoop) {
            ForLoop = forLoop;
            return this;
        }

        public Builder setWhileLoop(double whileLoop) {
            WhileLoop = whileLoop;
            return this;
        }

        public Builder setConstructorCall(double constructorCall) {
            ConstructorCall = constructorCall;
            return this;
        }

        public Builder setInvocation(double invocation) {
            Invocation = invocation;
            return this;
        }

        public Builder setReturn(double aReturn) {
            Return = aReturn;
            return this;
        }

        public Builder setBinaryOperator(double binaryOperator) {
            BinaryOperator = binaryOperator;
            return this;
        }

        public MethodVector build() {
            return new MethodVector(
                    FieldRead, FieldWrite,
                    VariableRead, VariableWrite,
                    Lambda,
                    Literal,
                    Conditional,
                    ForLoop, WhileLoop,
                    ConstructorCall, Invocation,
                    Return,
                    BinaryOperator
            ) {
                @Override
                public String toString() {
                    return name != null
                            ? name
                            : super.toString();
                }
            };
        }
    }
}
