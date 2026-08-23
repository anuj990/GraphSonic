#include "GraphSampler.h"

#include <algorithm>
#include <cmath>
#include <limits>

namespace {

    double nanValue() {
        return std::numeric_limits<double>::quiet_NaN();
    }

}

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

    double previousY =
            nanValue();

    bool previousValid =
            false;

    for (int i = 0;
         i < sampleCount;
         ++i) {

        const double x =
                xMin +
                static_cast<double>(i) *
                step;

        const double y =
                expression.evaluate(x);

        const bool valid =
                std::isfinite(y);

        if (!valid) {

            points.push_back(
                    nanValue()
            );

            points.push_back(
                    nanValue()
            );

            previousValid =
                    false;

            continue;
        }

        if (previousValid) {

            const double jump =
                    std::abs(
                            y -
                            previousY
                    );

            const double scale =
                    std::max(
                            {
                                    1.0,
                                    std::abs(y),
                                    std::abs(previousY)
                            }
                    );

            if (
                    jump >
                    scale * 100.0
                    ) {

                points.push_back(
                        nanValue()
                );

                points.push_back(
                        nanValue()
                );
            }
        }

        points.push_back(x);
        points.push_back(y);

        previousY =
                y;

        previousValid =
                true;
    }

    return points;
}