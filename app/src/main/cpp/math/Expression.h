#pragma once

#include "AST.h"

#include <memory>
#include <string>

class Expression {
public:
    explicit Expression(const std::string& expression);

    double evaluate(double x) const;

private:
    std::unique_ptr<ASTNode> root;
};