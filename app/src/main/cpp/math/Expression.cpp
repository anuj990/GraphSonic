#include "Expression.h"

#include "Evaluator.h"
#include "Lexer.h"
#include "Parser.h"

#include <cmath>

Expression::Expression(
        const std::string& expression
) {
    Lexer lexer(expression);

    const auto tokens =
            lexer.tokenize();

    Parser parser(tokens);

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

bool Expression::isDefined(
        double x
) const {
    if (!std::isfinite(x)) {
        return false;
    }

    const double result =
            evaluate(x);

    return std::isfinite(result);
}