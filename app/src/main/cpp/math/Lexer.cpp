#include "Lexer.h"

#include <cctype>
#include <cmath>
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
bool Lexer::startsWith(
        const char* value
) const {

    const std::string symbol(value);

    if (
            position + symbol.size() >
                    expression.size()
            ) {
        return false;
    }

    return expression.compare(
            position,
            symbol.size(),
            symbol
    ) == 0;
}

Token Lexer::readMathSymbol() {

    if (startsWith("π")) {

        position +=
                std::string("π").size();

        return Token(
                TokenType::Number,
                3.14159265358979323846
        );
    }

    if (startsWith("√")) {

        position +=
                std::string("√").size();

        return Token(
                TokenType::Function,
                "sqrt"
        );
    }

    if (startsWith("∛")) {

        position +=
                std::string("∛").size();

        return Token(
                TokenType::Function,
                "cbrt"
        );
    }

    if (startsWith("×")) {

        position +=
                std::string("×").size();

        return Token(
                TokenType::Multiply
        );
    }

    if (startsWith("÷")) {

        position +=
                std::string("÷").size();

        return Token(
                TokenType::Divide
        );
    }

    if (startsWith("−")) {

        position +=
                std::string("−").size();

        return Token(
                TokenType::Minus
        );
    }

    throw std::runtime_error(
            "Unsupported mathematical symbol"
    );
}
Token Lexer::readSuperscript() {

    if (startsWith("⁰")) {
        position += std::string("⁰").size();
        return Token(
                TokenType::SuperscriptNumber,
                0.0
        );
    }

    if (startsWith("¹")) {
        position += std::string("¹").size();
        return Token(
                TokenType::SuperscriptNumber,
                1.0
        );
    }

    if (startsWith("²")) {
        position += std::string("²").size();
        return Token(
                TokenType::SuperscriptNumber,
                2.0
        );
    }

    if (startsWith("³")) {
        position += std::string("³").size();
        return Token(
                TokenType::SuperscriptNumber,
                3.0
        );
    }

    if (startsWith("⁴")) {
        position += std::string("⁴").size();
        return Token(
                TokenType::SuperscriptNumber,
                4.0
        );
    }

    if (startsWith("⁵")) {
        position += std::string("⁵").size();
        return Token(
                TokenType::SuperscriptNumber,
                5.0
        );
    }

    if (startsWith("⁶")) {
        position += std::string("⁶").size();
        return Token(
                TokenType::SuperscriptNumber,
                6.0
        );
    }

    if (startsWith("⁷")) {
        position += std::string("⁷").size();
        return Token(
                TokenType::SuperscriptNumber,
                7.0
        );
    }

    if (startsWith("⁸")) {
        position += std::string("⁸").size();
        return Token(
                TokenType::SuperscriptNumber,
                8.0
        );
    }

    if (startsWith("⁹")) {
        position += std::string("⁹").size();
        return Token(
                TokenType::SuperscriptNumber,
                9.0
        );
    }

    if (startsWith("⁺")) {
        position += std::string("⁺").size();
        return Token(
                TokenType::SuperscriptPlus
        );
    }

    if (startsWith("⁻")) {
        position += std::string("⁻").size();
        return Token(
                TokenType::SuperscriptMinus
        );
    }

    throw std::runtime_error(
            "Invalid superscript"
    );
}
void Lexer::skipWhitespace() {
    while (
            !isAtEnd() &&
            std::isspace(
                    static_cast<unsigned char>(
                            currentChar()
                    )
            )
            ) {
        position++;
    }
}

Token Lexer::readNumber() {
    const std::size_t start =
            position;

    bool hasDigitsBeforeDot = false;
    bool hasDigitsAfterDot = false;

    while (
            !isAtEnd() &&
            std::isdigit(
                    static_cast<unsigned char>(
                            currentChar()
                    )
            )
            ) {
        hasDigitsBeforeDot = true;
        position++;
    }

    if (
            !isAtEnd() &&
            currentChar() == '.'
            ) {
        position++;

        while (
                !isAtEnd() &&
                std::isdigit(
                        static_cast<unsigned char>(
                                currentChar()
                        )
                )
                ) {
            hasDigitsAfterDot = true;
            position++;
        }
    }

    if (
            !hasDigitsBeforeDot &&
            !hasDigitsAfterDot
            ) {
        throw std::runtime_error(
                "Invalid number"
        );
    }

    if (
            !isAtEnd() &&
            (
                    currentChar() == 'e' ||
                    currentChar() == 'E'
            )
            ) {

        const std::size_t exponentStart =
                position;

        position++;

        if (
                !isAtEnd() &&
                (
                        currentChar() == '+' ||
                        currentChar() == '-'
                )
                ) {
            position++;
        }

        const std::size_t exponentDigitsStart =
                position;

        while (
                !isAtEnd() &&
                std::isdigit(
                        static_cast<unsigned char>(
                                currentChar()
                        )
                )
                ) {
            position++;
        }

        if (
                exponentDigitsStart ==
                position
                ) {
            throw std::runtime_error(
                    "Invalid exponent near position " +
                    std::to_string(
                            exponentStart
                    )
            );
        }
    }

    const std::string text =
            expression.substr(
                    start,
                    position - start
            );

    try {
        const double value =
                std::stod(text);

        if (!std::isfinite(value)) {
            throw std::runtime_error(
                    "Number is not finite"
            );
        }

        return Token(
                TokenType::Number,
                value
        );

    } catch (
            const std::exception&
    ) {
        throw std::runtime_error(
                "Invalid number: " +
                text
        );
    }
}

Token Lexer::readIdentifier() {
    const std::size_t start =
            position;

    while (!isAtEnd()) {

        const char c =
                currentChar();

        if (
                std::isalpha(
                        static_cast<unsigned char>(
                                c
                        )
                )
                ) {
            position++;
        } else {
            break;
        }
    }

    std::string identifier =
            expression.substr(
                    start,
                    position - start
            );

    for (char& c : identifier) {
        c = static_cast<char>(
                std::tolower(
                        static_cast<unsigned char>(
                                c
                        )
                )
        );
    }

    if (identifier == "x") {
        return Token(
                TokenType::Variable,
                identifier
        );
    }

    if (
            identifier == "sin" ||
                    identifier == "cos" ||
                    identifier == "tan" ||
                    identifier == "cot" ||
                    identifier == "sec" ||
                    identifier == "csc" ||
                    identifier == "asin" ||
                    identifier == "acos" ||
                    identifier == "atan" ||
                    identifier == "sinh" ||
                    identifier == "cosh" ||
                    identifier == "tanh" ||
                    identifier == "sqrt" ||
                    identifier == "cbrt" ||
                    identifier == "log" ||
                    identifier == "ln" ||
                    identifier == "abs" ||
                    identifier == "exp"
            ) {

        return Token(
                TokenType::Function,
                identifier
        );
    }

    if (
            identifier == "pi" ||
            identifier == "π"
            ) {

        return Token(
                TokenType::Number,
                3.14159265358979323846
        );
    }

    if (identifier == "e") {

        return Token(
                TokenType::Number,
                2.71828182845904523536
        );
    }

    throw std::runtime_error(
            "Unknown identifier: " +
            identifier
    );
}

std::vector<Token> Lexer::tokenize() {

    std::vector<Token> tokens;

    while (!isAtEnd()) {

        skipWhitespace();

        if (isAtEnd()) {
            break;
        }
        if (
                static_cast<unsigned char>(
                        currentChar()
                ) >= 128
                ) {

            if (
                    startsWith("⁰") ||
                            startsWith("¹") ||
                            startsWith("²") ||
                            startsWith("³") ||
                            startsWith("⁴") ||
                            startsWith("⁵") ||
                            startsWith("⁶") ||
                            startsWith("⁷") ||
                            startsWith("⁸") ||
                            startsWith("⁹") ||
                            startsWith("⁺") ||
                            startsWith("⁻")
                    ) {

                tokens.push_back(
                        readSuperscript()
                );

            } else {

                tokens.push_back(
                        readMathSymbol()
                );
            }

            continue;
        }
        const char c =
                currentChar();

        if (
                std::isdigit(
                        static_cast<unsigned char>(
                                c
                        )
                ) ||
                c == '.'
                ) {

            tokens.push_back(
                    readNumber()
            );

            continue;
        }

        if (
                std::isalpha(
                        static_cast<unsigned char>(
                                c
                        )
                )
                ) {

            tokens.push_back(
                    readIdentifier()
            );

            continue;
        }

        switch (c) {

            case '+':
                tokens.emplace_back(
                        TokenType::Plus
                );
                position++;
                break;

            case '-':
                tokens.emplace_back(
                        TokenType::Minus
                );
                position++;
                break;

            case '*':
                tokens.emplace_back(
                        TokenType::Multiply
                );
                position++;
                break;

            case '/':
                tokens.emplace_back(
                        TokenType::Divide
                );
                position++;
                break;

            case '^':
                tokens.emplace_back(
                        TokenType::Power
                );
                position++;
                break;

            case '(':
                tokens.emplace_back(
                        TokenType::LeftParen
                );
                position++;
                break;

            case ')':
                tokens.emplace_back(
                        TokenType::RightParen
                );
                position++;
                break;

            default:
                throw std::runtime_error(
                        std::string(
                                "Unexpected character: "
                        ) + c
                );
        }
    }

    tokens.emplace_back(
            TokenType::End
    );

    return tokens;
}