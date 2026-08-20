#pragma once

#include <memory>
#include <string>
#include <vector>

enum class NodeType {
    Number,
    Variable,
    Unary,
    Binary,
    Function
};

enum class OperatorType {
    Plus,
    Minus,
    Multiply,
    Divide,
    Power
};

struct ASTNode {
    NodeType type;

    double value = 0.0;

    OperatorType op;

    std::string functionName;

    std::unique_ptr<ASTNode> left;
    std::unique_ptr<ASTNode> right;

    explicit ASTNode(NodeType type)
            : type(type) {}
};