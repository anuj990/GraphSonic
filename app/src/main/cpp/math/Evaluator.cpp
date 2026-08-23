#include "Evaluator.h"

#include <cmath>
#include <limits>
#include <stdexcept>

namespace {

    double nanValue() {
        return std::numeric_limits<double>::quiet_NaN();
    }

    bool nearZero(
            double value
    ) {
        return std::abs(value) < 1e-12;
    }

}

double Evaluator::evaluate(
        const ASTNode& node,
        double x
) {

    switch (node.type) {

        case NodeType::Number:
            return node.value;

        case NodeType::Variable:
            return x;

        case NodeType::Unary: {

            const double value =
                    evaluate(
                            *node.right,
                            x
                    );

            if (!std::isfinite(value)) {
                return nanValue();
            }

            switch (node.op) {

                case OperatorType::Minus:
                    return -value;

                default:
                    return nanValue();
            }
        }

        case NodeType::Binary: {

            const double left =
                    evaluate(
                            *node.left,
                            x
                    );

            const double right =
                    evaluate(
                            *node.right,
                            x
                    );

            if (
                    !std::isfinite(left) ||
                    !std::isfinite(right)
                    ) {
                return nanValue();
            }

            switch (node.op) {

                case OperatorType::Plus:
                    return left + right;

                case OperatorType::Minus:
                    return left - right;

                case OperatorType::Multiply:
                    return left * right;

                case OperatorType::Divide:

                    if (nearZero(right)) {
                        return nanValue();
                    }

                    return left / right;

                case OperatorType::Power: {

                    const double result =
                            std::pow(
                                    left,
                                    right
                            );

                    if (!std::isfinite(result)) {
                        return nanValue();
                    }

                    return result;
                }
            }

            return nanValue();
        }

        case NodeType::Function: {

            const double argument =
                    evaluate(
                            *node.left,
                            x
                    );

            if (!std::isfinite(argument)) {
                return nanValue();
            }

            return evaluateFunction(
                    node,
                    argument
            );
        }
    }

    return nanValue();
}

double Evaluator::evaluateFunction(
        const ASTNode& node,
        double argument
) {

    const std::string& name =
            node.functionName;

    if (name == "sin") {

        return std::sin(
                argument
        );
    }

    if (name == "cos") {

        return std::cos(
                argument
        );
    }

    if (name == "tan") {

        const double cosine =
                std::cos(argument);

        if (nearZero(cosine)) {
            return nanValue();
        }

        return std::sin(argument) /
               cosine;
    }

    if (name == "cot") {

        const double sine =
                std::sin(argument);

        if (nearZero(sine)) {
            return nanValue();
        }

        return std::cos(argument) /
               sine;
    }

    if (name == "sec") {

        const double cosine =
                std::cos(argument);

        if (nearZero(cosine)) {
            return nanValue();
        }

        return 1.0 /
               cosine;
    }

    if (name == "csc") {

        const double sine =
                std::sin(argument);

        if (nearZero(sine)) {
            return nanValue();
        }

        return 1.0 /
               sine;
    }
    if (name == "asin") {

        if (
                argument < -1.0 ||
                argument > 1.0
                ) {
            return nanValue();
        }

        return std::asin(argument);
    }

    if (name == "acos") {

        if (
                argument < -1.0 ||
                argument > 1.0
                ) {
            return nanValue();
        }

        return std::acos(argument);
    }

    if (name == "atan") {

        return std::atan(argument);
    }

    if (name == "sinh") {

        const double result =
                std::sinh(argument);

        if (!std::isfinite(result)) {
            return nanValue();
        }

        return result;
    }

    if (name == "cosh") {

        const double result =
                std::cosh(argument);

        if (!std::isfinite(result)) {
            return nanValue();
        }

        return result;
    }

    if (name == "tanh") {

        return std::tanh(argument);
    }
    if (name == "sqrt") {

        if (argument < 0.0) {
            return nanValue();
        }

        return std::sqrt(
                argument
        );
    }

    if (name == "log") {

        if (argument <= 0.0) {
            return nanValue();
        }

        return std::log10(
                argument
        );
    }

    if (name == "ln") {

        if (argument <= 0.0) {
            return nanValue();
        }

        return std::log(
                argument
        );
    }

    if (name == "abs") {

        return std::abs(
                argument
        );
    }

    if (name == "exp") {

        const double result =
                std::exp(
                        argument
                );

        if (!std::isfinite(result)) {
            return nanValue();
        }

        return result;
    }

    throw std::runtime_error(
            "Unknown function: " +
            name
    );
}