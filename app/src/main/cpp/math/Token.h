#pragma once
#include <string>

using namespace std;
enum class TokenType{
    Number , Variable,Plus,Minus,Multiply,Divide,Power,LeftParen,RightParen,Function,End
};

struct Token{
    TokenType type;
    double value = 0.0;
    string text;
    Token(TokenType type)
            :type(type){}
    Token(TokenType type,double value)
            :type(type), value(value){}
    Token(TokenType type, const std::string& text)
            : type(type), text(text) {}

};

