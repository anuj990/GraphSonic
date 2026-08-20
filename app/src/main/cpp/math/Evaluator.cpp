#include "Evaluator.h"

#include <cmath>
#include <stdexcept>
#include <limits>

double Evaluator::evaluate(
        const ASTNode& node,
        double x
) {
    switch (node.type) {

        case NodeType::Number:
            return node.value;

        case NodeType::Variable:
            return x;

        case NodeType::Unary:
            return -evaluate(*node.right, x);

        case NodeType::Binary: {
            const double left =
                    evaluate(*node.left, x);

            const double right =
                    evaluate(*node.right, x);

            switch (node.op) {

                case OperatorType::Plus:
                    return left + right;

                case OperatorType::Minus:
                    return left - right;

                case OperatorType::Multiply:
                    return left * right;

                case OperatorType::Divide:
                    if (right == 0.0) {
                        return std::numeric_limits<double>::quiet_NaN();
                    }

                    return left / right;

                case OperatorType::Power:
                    return std::pow(left, right);
            }
        }

        case NodeType::Function: {
            const double argument =
                    evaluate(*node.left, x);

            return evaluateFunction(
                    node,
                    argument
            );
        }
    }

    return std::numeric_limits<double>::quiet_NaN();
}

double Evaluator::evaluateFunction(
        const ASTNode& node,
        double argument
) {
    if (node.functionName == "sin") {
        return std::sin(argument);
    }

    if (node.functionName == "cos") {
        return std::cos(argument);
    }

    if (node.functionName == "tan") {
        return std::tan(argument);
    }

    if (node.functionName == "sqrt") {
        return std::sqrt(argument);
    }

    if (node.functionName == "log") {
        return std::log10(argument);
    }

    if (node.functionName == "ln") {
        return std::log(argument);
    }

    if (node.functionName == "abs") {
        return std::abs(argument);
    }

    if (node.functionName == "exp") {
        return std::exp(argument);
    }

    throw std::runtime_error(
            "Unknown function: " + node.functionName
    );
}