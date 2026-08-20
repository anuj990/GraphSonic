#pragma once

#include "../math/Expression.h"

#include <vector>

class GraphSampler {
public:
    static std::vector<double> sample(
            const Expression& expression,
            double xMin,
            double xMax,
            int sampleCount
    );
};