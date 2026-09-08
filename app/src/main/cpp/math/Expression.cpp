#include "Expression.h"

#include "Lexer.h"
#include "Parser.h"
#include <iomanip>
#include <sstream>
namespace {

    std::string canonicalNumber(
            double value
    ) {

        std::ostringstream stream;

        stream << std::setprecision(15)
               << value;

        return stream.str();
    }

    std::string canonicalOperator(
            OperatorType op
    ) {

        switch (op) {

            case OperatorType::Plus:
                return "+";

            case OperatorType::Minus:
                return "-";

            case OperatorType::Multiply:
                return "*";

            case OperatorType::Divide:
                return "/";

            case OperatorType::Power:
                return "^";
        }

        throw std::runtime_error(
                "Unknown operator"
        );
    }

    std::string canonicalNode(
            const ASTNode& node
    ) {

        switch (node.type) {

            case NodeType::Number:
                return canonicalNumber(
                        node.value
                );

            case NodeType::Variable:
                return "x";

            case NodeType::Unary:

                if (
                        node.op ==
                                OperatorType::Minus
                        ) {

                    return "(-" +
                            canonicalNode(
                                    *node.right
                            ) +
                            ")";
                }

                return canonicalNode(
                        *node.right
                );

            case NodeType::Binary:

                return "(" +
                        canonicalNode(
                                *node.left
                        ) +
                        canonicalOperator(
                                node.op
                        ) +
                        canonicalNode(
                                *node.right
                        ) +
                        ")";

            case NodeType::Function:

                return node.functionName +
                        "(" +
                        canonicalNode(
                                *node.left
                        ) +
                        ")";
        }

        throw std::runtime_error(
                "Unknown AST node"
        );
    }

}
Expression::Expression(
        const std::string& expression
) {
    Lexer lexer(
            expression
    );

    const auto tokens =
            lexer.tokenize();

    Parser parser(
            tokens
    );

    root =
            parser.parse();
}

double Expression::evaluate(
        double x
) const {

    return Evaluator::evaluate(
            *root,
            x
    );
}

EvaluationResult Expression::evaluateResult(
        double x
) const {

    return Evaluator::evaluateResult(
            *root,
            x
    );
}

bool Expression::isDefined(
        double x
) const {

    return evaluateResult(x).isValid();
}
std::string Expression::canonical() const {

    return canonicalNode(
            *root
    );
}