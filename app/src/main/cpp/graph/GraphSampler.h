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

private:
    static void sampleInterval(
            const Expression& expression,
            double x1,
            double y1,
            double x2,
            double y2,
            int depth,
            int maxDepth,
            double minWidth,
            std::vector<double>& points
    );

    static void addPoint(
            double x,
            double y,
            std::vector<double>& points
    );
};