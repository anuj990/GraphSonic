#include "Lexer.h"

#include <cctype>
#include <stdexcept>

Lexer::Lexer(const std::string& expression)
        : expression(expression) {}

char Lexer::currentChar() const {
    if (isAtEnd()) {
        return '\0';
    }

    return expression[position];
}

bool Lexer::isAtEnd() const {
    return position >= expression.size();
}

void Lexer::skipWhitespace() {
    while (!isAtEnd() &&
           std::isspace(static_cast<unsigned char>(currentChar()))) {
        position++;
    }
}

Token Lexer::readNumber() {
    const std::size_t start = position;

    bool hasDot = false;
    bool hasExponent = false;

    while (!isAtEnd()) {
        char c = currentChar();

        if (std::isdigit(static_cast<unsigned char>(c))) {
            position++;
            continue;
        }

        if (c == '.' && !hasDot) {
            hasDot = true;
            position++;
            continue;
        }

        if ((c == 'e' || c == 'E') && !hasExponent) {
            hasExponent = true;
            position++;

            if (!isAtEnd() &&
                (currentChar() == '+' || currentChar() == '-')) {
                position++;
            }

            continue;
        }

        break;
    }

    const std::string text =
            expression.substr(start, position - start);

    try {
        return Token(TokenType::Number, std::stod(text));
    } catch (...) {
        throw std::runtime_error("Invalid number: " + text);
    }
}

Token Lexer::readIdentifier() {
    const std::size_t start = position;

    while (!isAtEnd()) {
        char c = currentChar();

        if (std::isalpha(static_cast<unsigned char>(c))) {
            position++;
        } else {
            break;
        }
    }

    const std::string identifier =
            expression.substr(start, position - start);

    if (identifier == "x") {
        return Token(TokenType::Variable, identifier);
    }

    if (
            identifier == "sin" ||
            identifier == "cos" ||
            identifier == "tan" ||
            identifier == "sqrt" ||
            identifier == "log" ||
            identifier == "ln" ||
            identifier == "abs" ||
            identifier == "exp"
            ) {
        return Token(TokenType::Function, identifier);
    }

    if (identifier == "pi") {
        return Token(TokenType::Number, 3.14159265358979323846);
    }

    if (identifier == "e") {
        return Token(TokenType::Number, 2.71828182845904523536);
    }

    throw std::runtime_error(
            "Unknown identifier: " + identifier
    );
}

std::vector<Token> Lexer::tokenize() {
    std::vector<Token> tokens;

    while (!isAtEnd()) {
        skipWhitespace();

        if (isAtEnd()) {
            break;
        }

        const char c = currentChar();

        if (std::isdigit(static_cast<unsigned char>(c)) || c == '.') {
            tokens.push_back(readNumber());
            continue;
        }

        if (std::isalpha(static_cast<unsigned char>(c))) {
            tokens.push_back(readIdentifier());
            continue;
        }

        switch (c) {
            case '+':
                tokens.emplace_back(TokenType::Plus);
                position++;
                break;

            case '-':
                tokens.emplace_back(TokenType::Minus);
                position++;
                break;

            case '*':
                tokens.emplace_back(TokenType::Multiply);
                position++;
                break;

            case '/':
                tokens.emplace_back(TokenType::Divide);
                position++;
                break;

            case '^':
                tokens.emplace_back(TokenType::Power);
                position++;
                break;

            case '(':
                tokens.emplace_back(TokenType::LeftParen);
                position++;
                break;

            case ')':
                tokens.emplace_back(TokenType::RightParen);
                position++;
                break;

            default:
                throw std::runtime_error(
                        std::string("Unexpected character: ") + c
                );
        }
    }

    tokens.emplace_back(TokenType::End);

    return tokens;
}