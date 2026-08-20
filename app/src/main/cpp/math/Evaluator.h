#pragma once

#include "AST.h"

class Evaluator {
public:
    static double evaluate(
            const ASTNode& node,
            double x
    );

private:
    static double evaluateFunction(
            const ASTNode& node,
            double argument
    );
};