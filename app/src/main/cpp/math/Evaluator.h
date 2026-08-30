#pragma once

#include "AST.h"

enum class EvaluationStatus {
    Valid,
    Undefined,
    Overflow
};

struct EvaluationResult {
    double value;
    EvaluationStatus status;

    static EvaluationResult valid(double value) {
        return {
                value,
                EvaluationStatus::Valid
        };
    }

    static EvaluationResult undefined() {
        return {
                0.0,
                EvaluationStatus::Undefined
        };
    }

    static EvaluationResult overflow() {
        return {
                0.0,
                EvaluationStatus::Overflow
        };
    }

    bool isValid() const {
        return status == EvaluationStatus::Valid;
    }
};

class Evaluator {
public:

    static double evaluate(
            const ASTNode& node,
            double x
    );

    static EvaluationResult evaluateResult(
            const ASTNode& node,
            double x
    );

private:

    static EvaluationResult evaluateNode(
            const ASTNode& node,
            double x
    );

    static EvaluationResult evaluateFunction(
            const ASTNode& node,
            double argument
    );

    static EvaluationResult evaluateBinary(
            OperatorType operation,
            double left,
            double right
    );
};