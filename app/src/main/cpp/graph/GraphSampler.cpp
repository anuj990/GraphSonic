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

    if (
            sampleCount < 2 ||
                    !std::isfinite(xMin) ||
                    !std::isfinite(xMax) ||
                    xMin >= xMax
            ) {
        return points;
    }

    points.reserve(
            static_cast<std::size_t>(
                    sampleCount
            ) * 2
    );

    const double step =
            (xMax - xMin) /
                    static_cast<double>(
                            sampleCount - 1
                    );

    for (
            int i = 0;
            i < sampleCount;
            ++i
            ) {

        const double x =
                xMin +
                        static_cast<double>(i) *
                                step;

        const EvaluationResult result =
                expression.evaluateResult(
                        x
                );

        if (!result.isValid()) {

            points.push_back(
                    std::numeric_limits<double>::quiet_NaN()
            );

            points.push_back(
                    std::numeric_limits<double>::quiet_NaN()
            );

            continue;
        }

        points.push_back(
                x
        );

        points.push_back(
                result.value
        );
    }

    return points;
}