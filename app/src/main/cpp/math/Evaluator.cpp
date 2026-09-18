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
    bool isOddInteger(double value) {
        if (!std::isfinite(value)) {
            return false;
        }

        const double rounded =
                std::round(value);

        if (std::abs(value - rounded) > 1e-12) {
            return false;
        }

        const double remainder =
                std::fmod(
                        std::abs(rounded),
                        2.0
                );

        return remainder == 1.0;
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

            if (
                    node.functionName == "log" &&
                            node.right
                    ) {

                const EvaluationResult base =
                        evaluateNode(
                                *node.right,
                                x
                        );

                if (!base.isValid()) {
                    return base;
                }

                if (
                        argument.value <= 0.0 ||
                                base.value <= 0.0 ||
                                base.value == 1.0
                        ) {
                    return EvaluationResult::undefined();
                }

                const double result =
                        std::log(argument.value) /
                                std::log(base.value);

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

            if (
                    left == 0.0 &&
                            right == 0.0
                    ) {
                return EvaluationResult::undefined();
            }

            if (
                    left == 0.0 &&
                            right < 0.0
                    ) {
                return EvaluationResult::undefined();
            }

            if (left < 0.0) {

                const double rounded =
                        std::round(right);

                if (
                        std::abs(right - rounded) >
                                1e-12
                        ) {

                    const double reciprocal =
                            1.0 / right;

                    if (!isOddInteger(reciprocal)) {
                        return EvaluationResult::undefined();
                    }

                    const double magnitude =
                            std::pow(
                                    -left,
                                    right
                            );

                    result =
                            -magnitude;

                } else {

                    result =
                            std::pow(
                                    left,
                                    right
                            );
                }

            } else {

                result =
                        std::pow(
                                left,
                                right
                        );
            }

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

    if (name == "sin") {

        result =
                std::sin(argument);

    } else if (name == "cos") {

        result =
                std::cos(argument);

    } else if (name == "tan") {

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

    } else if (name == "asin") {

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

    } else if (name == "sinh") {

        result =
                std::sinh(argument);

    } else if (name == "cosh") {

        result =
                std::cosh(argument);

    } else if (name == "tanh") {

        result =
                std::tanh(argument);

    } else if (name == "sqrt") {

        if (argument < 0.0) {
            return EvaluationResult::undefined();
        }

        result =
                std::sqrt(argument);

    } else if (name == "cbrt") {

        result =
                std::cbrt(argument);

    } else if (name == "ln") {

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

    } else if (name == "exp") {

        result =
                std::exp(argument);

    } else if (name == "abs") {

        result =
                std::abs(argument);

    } else if (name == "floor") {

        result =
                std::floor(argument);

    } else if (name == "ceil") {

        result =
                std::ceil(argument);

    } else {

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