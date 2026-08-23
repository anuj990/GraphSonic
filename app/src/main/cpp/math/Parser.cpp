#include "Parser.h"

#include <stdexcept>

Parser::Parser(
        const std::vector<Token>& tokens
)
        : tokens(tokens) {}

const Token& Parser::current() const {

    if (position >= tokens.size()) {
        throw std::runtime_error(
                "Unexpected end of expression"
        );
    }

    return tokens[position];
}

bool Parser::check(
        TokenType type
) const {

    return current().type == type;
}

bool Parser::match(
        TokenType type
) {

    if (!check(type)) {
        return false;
    }

    position++;

    return true;
}

const Token& Parser::consume(
        TokenType type,
        const char* message
) {

    if (!check(type)) {
        throw std::runtime_error(
                message
        );
    }

    return tokens[position++];
}

std::unique_ptr<ASTNode>
Parser::parse() {

    auto result =
            parseExpression();

    if (!check(TokenType::End)) {
        throw std::runtime_error(
                "Unexpected token after expression"
        );
    }

    return result;
}

std::unique_ptr<ASTNode>
Parser::parseExpression() {

    auto node =
            parseTerm();

    while (
            check(TokenType::Plus) ||
            check(TokenType::Minus)
            ) {

        const TokenType operation =
                current().type;

        position++;

        auto right =
                parseTerm();

        auto parent =
                std::make_unique<ASTNode>(
                        NodeType::Binary
                );

        parent->op =
                operation ==
                TokenType::Plus
                ? OperatorType::Plus
                : OperatorType::Minus;

        parent->left =
                std::move(node);

        parent->right =
                std::move(right);

        node =
                std::move(parent);
    }

    return node;
}

std::unique_ptr<ASTNode>
Parser::parseTerm() {

    auto node =
            parseUnary();

    while (true) {

        if (
                check(TokenType::Multiply) ||
                check(TokenType::Divide)
                ) {

            const TokenType operation =
                    current().type;

            position++;

            auto right =
                    parseUnary();

            auto parent =
                    std::make_unique<ASTNode>(
                            NodeType::Binary
                    );

            parent->op =
                    operation ==
                    TokenType::Multiply
                    ? OperatorType::Multiply
                    : OperatorType::Divide;

            parent->left =
                    std::move(node);

            parent->right =
                    std::move(right);

            node =
                    std::move(parent);

            continue;
        }

        if (
                startsImplicitMultiplication()
                ) {

            auto right =
                    parseUnary();

            auto parent =
                    std::make_unique<ASTNode>(
                            NodeType::Binary
                    );

            parent->op =
                    OperatorType::Multiply;

            parent->left =
                    std::move(node);

            parent->right =
                    std::move(right);

            node =
                    std::move(parent);

            continue;
        }

        break;
    }

    return node;
}

bool Parser::startsImplicitMultiplication() const {

    switch (current().type) {

        case TokenType::Number:
        case TokenType::Variable:
        case TokenType::Function:
        case TokenType::LeftParen:
            return true;

        default:
            return false;
    }
}

std::unique_ptr<ASTNode>
Parser::parseUnary() {

    if (match(TokenType::Minus)) {

        auto node =
                std::make_unique<ASTNode>(
                        NodeType::Unary
                );

        node->op =
                OperatorType::Minus;

        node->right =
                parseUnary();

        return node;
    }

    if (match(TokenType::Plus)) {
        return parseUnary();
    }

    return parsePower();
}

std::unique_ptr<ASTNode>
Parser::parsePower() {

    auto node =
            parsePrimary();

    if (match(TokenType::Power)) {

        auto right =
                parseUnary();

        auto parent =
                std::make_unique<ASTNode>(
                        NodeType::Binary
                );

        parent->op =
                OperatorType::Power;

        parent->left =
                std::move(node);

        parent->right =
                std::move(right);

        return parent;
    }

    return node;
}

std::unique_ptr<ASTNode>
Parser::parsePrimary() {

    if (check(TokenType::Number)) {

        const double value =
                current().value;

        position++;

        auto node =
                std::make_unique<ASTNode>(
                        NodeType::Number
                );

        node->value =
                value;

        return node;
    }

    if (check(TokenType::Variable)) {

        position++;

        return std::make_unique<ASTNode>(
                NodeType::Variable
        );
    }

    if (check(TokenType::Function)) {

        const std::string functionName =
                current().text;

        position++;

        consume(
                TokenType::LeftParen,
                "Expected '(' after function"
        );

        auto argument =
                parseExpression();

        consume(
                TokenType::RightParen,
                "Expected ')' after function argument"
        );

        auto node =
                std::make_unique<ASTNode>(
                        NodeType::Function
                );

        node->functionName =
                functionName;

        node->left =
                std::move(argument);

        return node;
    }

    if (
            match(TokenType::LeftParen)
            ) {

        auto node =
                parseExpression();

        consume(
                TokenType::RightParen,
                "Expected ')'"
        );

        return node;
    }

    throw std::runtime_error(
            "Expected number, variable, function, or '('"
    );
}