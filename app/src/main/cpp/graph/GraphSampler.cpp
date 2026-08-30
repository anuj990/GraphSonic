#include "GraphSampler.h"

#include <algorithm>
#include <cmath>
#include <limits>

namespace {

    constexpr int MIN_INITIAL_SAMPLES = 64;
    constexpr int MAX_ADAPTIVE_DEPTH = 12;
    constexpr double MAX_Y_JUMP_RATIO = 0.08;
    constexpr double MIN_INTERVAL_WIDTH = 1e-7;

    bool valid(const EvaluationResult& result) {
        return result.isValid() &&
                std::isfinite(result.value);
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

    const int initialSamples =
            std::clamp(
                    sampleCount / 16,
                    MIN_INITIAL_SAMPLES,
                    512
            );

    const double step =
            (xMax - xMin) /
                    static_cast<double>(
                            initialSamples - 1
                    );

    const double range =
            xMax - xMin;

    const double maxYJump =
            std::max(
                    1e-6,
                    range * MAX_Y_JUMP_RATIO
            );

    const double minWidth =
            std::max(
                    MIN_INTERVAL_WIDTH,
                    range /
                            static_cast<double>(
                                    sampleCount
                            )
            );

    std::vector<double> xs(
            initialSamples
    );

    std::vector<double> ys(
            initialSamples
    );

    std::vector<bool> defined(
            initialSamples
    );

    for (
            int i = 0;
            i < initialSamples;
            ++i
            ) {

        const double x =
                xMin +
                        static_cast<double>(i) *
                                step;

        xs[i] =
                x;

        const EvaluationResult result =
                expression.evaluateResult(
                        x
                );

        defined[i] =
                valid(result);

        ys[i] =
                defined[i]
                        ? result.value
                        : std::numeric_limits<double>::quiet_NaN();
    }

    points.reserve(
            static_cast<std::size_t>(
                    sampleCount
            ) * 2
    );

    for (
            int i = 0;
            i < initialSamples - 1;
            ++i
            ) {

        const double x1 =
                xs[i];

        const double x2 =
                xs[i + 1];

        if (
                !defined[i] ||
                        !defined[i + 1]
                ) {

            addPoint(
                    std::numeric_limits<double>::quiet_NaN(),
                    std::numeric_limits<double>::quiet_NaN(),
                    points
            );

            continue;
        }

        sampleInterval(
                expression,
                x1,
                ys[i],
                x2,
                ys[i + 1],
                0,
                MAX_ADAPTIVE_DEPTH,
                minWidth,
                maxYJump,
                points
        );
    }

    return points;
}

void GraphSampler::sampleInterval(
        const Expression& expression,
        double x1,
        double y1,
        double x2,
        double y2,
        int depth,
        int maxDepth,
        double minWidth,
        double maxYJump,
        std::vector<double>& points
) {
    if (
            !std::isfinite(x1) ||
                    !std::isfinite(x2) ||
                    !std::isfinite(y1) ||
                    !std::isfinite(y2)
            ) {

        addPoint(
                std::numeric_limits<double>::quiet_NaN(),
                std::numeric_limits<double>::quiet_NaN(),
                points
        );

        return;
    }

    const double width =
            x2 - x1;

    if (
            width <= minWidth ||
                    depth >= maxDepth
            ) {

        addPoint(
                x1,
                y1,
                points
        );

        addPoint(
                x2,
                y2,
                points
        );

        return;
    }

    const double midpoint =
            x1 +
                    width * 0.5;

    const EvaluationResult middleResult =
            expression.evaluateResult(
                    midpoint
            );

    if (!valid(middleResult)) {

        addPoint(
                x1,
                y1,
                points
        );

        addPoint(
                std::numeric_limits<double>::quiet_NaN(),
                std::numeric_limits<double>::quiet_NaN(),
                points
        );

        addPoint(
                x2,
                y2,
                points
        );

        return;
    }

    const double yMiddle =
            middleResult.value;

    const double linearMiddle =
            y1 +
                    (y2 - y1) * 0.5;

    const double curvatureError =
            std::abs(
                    yMiddle -
                            linearMiddle
            );

    const double yRange =
            std::max(
                    {
                            std::abs(y1),
                            std::abs(y2),
                            std::abs(yMiddle),
                            1.0
                    }
            );

    const double normalizedError =
            curvatureError /
                    yRange;

    const double endpointJump =
            std::abs(
                    y2 - y1
            );

    const bool needsSubdivision =
            normalizedError > 0.02 ||
                    endpointJump > maxYJump;

    if (!needsSubdivision) {

        addPoint(
                x1,
                y1,
                points
        );

        addPoint(
                x2,
                y2,
                points
        );

        return;
    }

    sampleInterval(
            expression,
            x1,
            y1,
            midpoint,
            yMiddle,
            depth + 1,
            maxDepth,
            minWidth,
            maxYJump,
            points
    );

    sampleInterval(
            expression,
            midpoint,
            yMiddle,
            x2,
            y2,
            depth + 1,
            maxDepth,
            minWidth,
            maxYJump,
            points
    );
}

void GraphSampler::addPoint(
        double x,
        double y,
        std::vector<double>& points
) {
    if (
            !points.empty()
            ) {

        const double previousX =
                points[
                        points.size() - 2
                ];

        const double previousY =
                points[
                        points.size() - 1
                ];

        if (
                std::isfinite(previousX) &&
                        std::isfinite(previousY) &&
                        std::isfinite(x) &&
                        std::isfinite(y) &&
                        previousX == x &&
                        previousY == y
                ) {
            return;
        }
    }

    points.push_back(x);
    points.push_back(y);
}