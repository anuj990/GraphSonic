#include "Evaluator.h"

#include <cmath>
#include <limits>

namespace {

    constexpr double PI =
            3.141592653589793238462643383279502884;

    constexpr double TAN_DOMAIN_EPSILON =
            1e-12;

    bool isFinite(double value) {
        return std::isfinite(value);
    }

    bool isNearZero(double value) {
        return std::abs(value) < TAN_DOMAIN_EPSILON;
    }

}

double Evaluator::evaluate(
        const ASTNode& node,
        double x
) {
    const EvaluationResult result =
            evaluateResult(
                    node,
                    x
            );

    if (!result.isValid()) {
        return std::numeric_limits<double>::quiet_NaN();
    }

    return result.value;
}

EvaluationResult Evaluator::evaluateResult(
        const ASTNode& node,
        double x
) {
    if (!isFinite(x)) {
        return EvaluationResult::undefined();
    }

    return evaluateNode(
            node,
            x
    );
}

EvaluationResult Evaluator::evaluateNode(
        const ASTNode& node,
        double x
) {
    switch (node.type) {

        case NodeType::Number: {

            if (!isFinite(node.value)) {
                return EvaluationResult::overflow();
            }

            return EvaluationResult::valid(
                    node.value
            );
        }

        case NodeType::Variable:

            return EvaluationResult::valid(
                    x
            );

        case NodeType::Unary: {

            const EvaluationResult value =
                    evaluateNode(
                            *node.right,
                            x
                    );

            if (!value.isValid()) {
                return value;
            }

            const double result =
                    -value.value;

            if (!isFinite(result)) {
                return EvaluationResult::overflow();
            }

            return EvaluationResult::valid(
                    result
            );
        }

        case NodeType::Binary: {

            const EvaluationResult left =
                    evaluateNode(
                            *node.left,
                            x
                    );

            if (!left.isValid()) {
                return left;
            }

            const EvaluationResult right =
                    evaluateNode(
                            *node.right,
                            x
                    );

            if (!right.isValid()) {
                return right;
            }

            return evaluateBinary(
                    node.op,
                    left.value,
                    right.value
            );
        }

        case NodeType::Function: {

            const EvaluationResult argument =
                    evaluateNode(
                            *node.left,
                            x
                    );

            if (!argument.isValid()) {
                return argument;
            }

            return evaluateFunction(
                    node,
                    argument.value
            );
        }
    }

    return EvaluationResult::undefined();
}

EvaluationResult Evaluator::evaluateBinary(
        OperatorType operation,
        double left,
        double right
) {
    double result = 0.0;

    switch (operation) {

        case OperatorType::Plus:

            result =
                    left + right;

            break;

        case OperatorType::Minus:

            result =
                    left - right;

            break;

        case OperatorType::Multiply:

            result =
                    left * right;

            break;

        case OperatorType::Divide:

            if (isNearZero(right)) {
                return EvaluationResult::undefined();
            }

            result =
                    left / right;

            break;

        case OperatorType::Power:

            /*
             * 0^negative = undefined
             */
            if (
                    left == 0.0 &&
                            right < 0.0
                    ) {
                return EvaluationResult::undefined();
            }

            /*
             * Negative base with a non-integer
             * exponent is not real-valued.
             */
            if (
                    left < 0.0 &&
                            std::floor(right) != right
                    ) {
                return EvaluationResult::undefined();
            }

            /*
             * 0^0 is mathematically indeterminate.
             */
            if (
                    left == 0.0 &&
                            right == 0.0
                    ) {
                return EvaluationResult::undefined();
            }

            result =
                    std::pow(
                            left,
                            right
                    );

            break;
    }

    if (std::isnan(result)) {
        return EvaluationResult::undefined();
    }

    if (std::isinf(result)) {
        return EvaluationResult::overflow();
    }

    return EvaluationResult::valid(
            result
    );
}

EvaluationResult Evaluator::evaluateFunction(
        const ASTNode& node,
        double argument
) {
    const std::string& name =
            node.functionName;

    double result = 0.0;

    /*
     * Trigonometric functions
     */

    if (name == "sin") {

        result =
                std::sin(argument);

    } else if (name == "cos") {

        result =
                std::cos(argument);

    } else if (name == "tan") {

        /*
         * tan(x) is undefined when:
         *
         * cos(x) = 0
         *
         * Detect this before calling tan()
         * because std::tan() can return a very
         * large finite value near an asymptote.
         */
        const double cosine =
                std::cos(argument);

        if (isNearZero(cosine)) {
            return EvaluationResult::undefined();
        }

        result =
                std::tan(argument);

    } else if (name == "cot") {

        const double sine =
                std::sin(argument);

        if (isNearZero(sine)) {
            return EvaluationResult::undefined();
        }

        result =
                std::cos(argument) /
                        sine;

    } else if (name == "sec") {

        const double cosine =
                std::cos(argument);

        if (isNearZero(cosine)) {
            return EvaluationResult::undefined();
        }

        result =
                1.0 /
                        cosine;

    } else if (name == "csc") {

        const double sine =
                std::sin(argument);

        if (isNearZero(sine)) {
            return EvaluationResult::undefined();
        }

        result =
                1.0 /
                        sine;

    }

        /*
         * Inverse trigonometric functions
         */

    else if (name == "asin") {

        if (
                argument < -1.0 ||
                        argument > 1.0
                ) {
            return EvaluationResult::undefined();
        }

        result =
                std::asin(argument);

    } else if (name == "acos") {

        if (
                argument < -1.0 ||
                        argument > 1.0
                ) {
            return EvaluationResult::undefined();
        }

        result =
                std::acos(argument);

    } else if (name == "atan") {

        result =
                std::atan(argument);
    }

        /*
         * Hyperbolic functions
         */

    else if (name == "sinh") {

        result =
                std::sinh(argument);

    } else if (name == "cosh") {

        result =
                std::cosh(argument);

    } else if (name == "tanh") {

        result =
                std::tanh(argument);
    }

        /*
         * Square / cube root
         */

    else if (name == "sqrt") {

        if (argument < 0.0) {
            return EvaluationResult::undefined();
        }

        result =
                std::sqrt(argument);

    } else if (name == "cbrt") {

        result =
                std::cbrt(argument);
    }

        /*
         * Logarithms
         */

    else if (name == "ln") {

        if (argument <= 0.0) {
            return EvaluationResult::undefined();
        }

        result =
                std::log(argument);

    } else if (name == "log") {

        if (argument <= 0.0) {
            return EvaluationResult::undefined();
        }

        result =
                std::log10(argument);
    }

        /*
         * Exponential
         */

    else if (name == "exp") {

        result =
                std::exp(argument);
    }

        /*
         * Absolute value
         */

    else if (name == "abs") {

        result =
                std::abs(argument);
    }

        /*
         * Unknown function
         */

    else {

        return EvaluationResult::undefined();
    }

    if (std::isnan(result)) {
        return EvaluationResult::undefined();
    }

    if (std::isinf(result)) {
        return EvaluationResult::overflow();
    }

    return EvaluationResult::valid(
            result
    );
}