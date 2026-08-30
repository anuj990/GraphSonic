#include "Expression.h"

#include "Lexer.h"
#include "Parser.h"

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