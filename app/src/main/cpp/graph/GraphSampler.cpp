#include "GraphSampler.h"

#include <cmath>
#include <limits>

std::vector<double> GraphSampler::sample(
        const Expression& expression,
        double xMin,
        double xMax,
        int sampleCount
) {
    std::vector<double> points;

    if (sampleCount < 2 || xMin >= xMax) {
        return points;
    }

    points.reserve(sampleCount * 2);

    const double step =
            (xMax - xMin) /
            static_cast<double>(sampleCount - 1);

    for (int i = 0; i < sampleCount; ++i) {

        const double x =
                xMin + static_cast<double>(i) * step;

        const double y =
                expression.evaluate(x);

        /*
         * NaN marks a break in the curve.
         *
         * This is important for functions such as:
         *
         *      1 / x
         *      tan(x)
         *      log(x)
         *
         * where the curve must not be connected across
         * an invalid/discontinuous region.
         */
        if (!std::isfinite(y)) {
            points.push_back(
                    std::numeric_limits<double>::quiet_NaN()
            );

            points.push_back(
                    std::numeric_limits<double>::quiet_NaN()
            );

            continue;
        }

        points.push_back(x);
        points.push_back(y);
    }

    return points;
}