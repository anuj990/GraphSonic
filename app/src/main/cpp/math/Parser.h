#pragma once

#include "AST.h"
#include "Token.h"

#include <memory>
#include <vector>

class Parser {
public:
    explicit Parser(
            const std::vector<Token>& tokens
    );

    std::unique_ptr<ASTNode> parse();

private:
    const std::vector<Token>& tokens;
    std::size_t position = 0;

    const Token& current() const;

    bool check(
            TokenType type
    ) const;

    bool match(
            TokenType type
    );

    const Token& consume(
            TokenType type,
            const char* message
    );

    std::unique_ptr<ASTNode>
    parseExpression();

    std::unique_ptr<ASTNode>
    parseTerm();

    std::unique_ptr<ASTNode>
    parseUnary();

    std::unique_ptr<ASTNode>
    parsePower();

    std::unique_ptr<ASTNode>
    parsePrimary();

    bool startsImplicitMultiplication() const;
};