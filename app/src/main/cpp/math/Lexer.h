#pragma once

#include "Token.h"

#include <string>
#include <vector>

using namespace std;
class Lexer {
public:
    explicit Lexer(const string& expression);

    vector<Token> tokenize();

private:
    string expression;
    size_t position = 0;

    char currentChar() const;
    void skipWhitespace();
    Token readSuperscript();
    Token readNumber();
    Token readIdentifier();
    Token readMathSymbol();

    bool isAtEnd() const;
    bool startsWith(const char* value) const;
};