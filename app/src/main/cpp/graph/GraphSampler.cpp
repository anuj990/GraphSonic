#include "GraphSampler.h"

#include <algorithm>
#include <cmath>
#include <limits>

namespace {

    constexpr int MIN_INITIAL_SAMPLES = 128;
    constexpr int MAX_INITIAL_SAMPLES = 2048;
    constexpr int MAX_ADAPTIVE_DEPTH = 16;

    constexpr double CURVATURE_THRESHOLD = 0.008;
    constexpr double STRONG_CURVATURE_THRESHOLD = 0.04;

    constexpr double MAX_ABSOLUTE_VALUE = 1.0e8;
    constexpr double ASYMPTOTE_RATIO = 32.0;

    constexpr double MIN_INTERVAL_WIDTH = 1.0e-9;

    bool valid(const EvaluationResult& result) {
        return result.isValid() &&
                std::isfinite(result.value) &&
                std::abs(result.value) <= MAX_ABSOLUTE_VALUE;
    }

    bool signChanged(double a, double b) {
        return (
                (a < 0.0 && b > 0.0) ||
                        (a > 0.0 && b < 0.0)
        );
    }

    double scaleOf(
            double a,
            double b,
            double c,
            double d,
            double e
    ) {
        return std::max(
                {
                        std::abs(a),
                        std::abs(b),
                        std::abs(c),
                        std::abs(d),
                        std::abs(e),
                        1.0
                }
        );
    }

    double normalizedError(
            double y1,
            double ym,
            double y2
    ) {
        const double linearMiddle =
                y1 +
                        (y2 - y1) * 0.5;

        const double error =
                std::abs(
                        ym -
                                linearMiddle
                );

        const double scale =
                std::max(
                        {
                                std::abs(y1),
                                std::abs(ym),
                                std::abs(y2),
                                1.0
                        }
                );

        return error / scale;
    }

    double slope(
            double x1,
            double y1,
            double x2,
            double y2
    ) {
        const double width = x2 - x1;

        if (
                !std::isfinite(width) ||
                        std::abs(width) <= MIN_INTERVAL_WIDTH
                ) {
            return 0.0;
        }

        return (y2 - y1) / width;
    }

    bool sameDirection(
            double a,
            double b
    ) {
        if (a == 0.0 || b == 0.0) {
            return true;
        }

        return (
                (a > 0.0 && b > 0.0) ||
                        (a < 0.0 && b < 0.0)
        );
    }

    bool likelyAsymptote(
            double y1,
            double yQuarterLeft,
            double yMiddle,
            double yQuarterRight,
            double y2
    ) {
        const double endpointScale =
                std::max(
                        {
                                std::abs(y1),
                                std::abs(y2),
                                1.0
                        }
                );

        const double interiorScale =
                std::max(
                        {
                                std::abs(yQuarterLeft),
                                std::abs(yMiddle),
                                std::abs(yQuarterRight),
                                1.0
                        }
                );

        if (
                interiorScale >
                        endpointScale *
                                ASYMPTOTE_RATIO
                ) {
            return true;
        }

        const bool leftExplosion =
                std::abs(yQuarterLeft) >
                        std::max(
                                std::abs(y1),
                                1.0
                        ) *
                                ASYMPTOTE_RATIO;

        const bool middleExplosion =
                std::abs(yMiddle) >
                        std::max(
                                {
                                        std::abs(y1),
                                        std::abs(y2),
                                        1.0
                                }
                        ) *
                                ASYMPTOTE_RATIO;

        const bool rightExplosion =
                std::abs(yQuarterRight) >
                        std::max(
                                std::abs(y2),
                                1.0
                        ) *
                                ASYMPTOTE_RATIO;

        return (
                leftExplosion &&
                        rightExplosion
        ) ||
                (
                        middleExplosion &&
                                (
                                        signChanged(
                                                y1,
                                                y2
                                        ) ||
                                                signChanged(
                                                        y1,
                                                        yMiddle
                                                ) ||
                                                signChanged(
                                                        yMiddle,
                                                        y2
                                                )
                                )
                );
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
                    sampleCount / 2,
                    MIN_INITIAL_SAMPLES,
                    MAX_INITIAL_SAMPLES
            );

    const double range =
            xMax - xMin;

    const double step =
            range /
                    static_cast<double>(
                            initialSamples - 1
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

        xs[i] = x;

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
                    x1,
                    ys[i],
                    points
            );

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

    const double quarterLeft =
            x1 +
                    width * 0.25;

    const double quarterRight =
            x1 +
                    width * 0.75;

    const EvaluationResult middleResult =
            expression.evaluateResult(
                    midpoint
            );

    const EvaluationResult leftQuarterResult =
            expression.evaluateResult(
                    quarterLeft
            );

    const EvaluationResult rightQuarterResult =
            expression.evaluateResult(
                    quarterRight
            );

    const bool middleValid =
            valid(
                    middleResult
            );

    const bool leftQuarterValid =
            valid(
                    leftQuarterResult
            );

    const bool rightQuarterValid =
            valid(
                    rightQuarterResult
            );

    if (
            !middleValid ||
                    !leftQuarterValid ||
                    !rightQuarterValid
            ) {
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

        return;
    }

    const double yMiddle =
            middleResult.value;

    const double yQuarterLeft =
            leftQuarterResult.value;

    const double yQuarterRight =
            rightQuarterResult.value;

    const double leftCurvature =
            normalizedError(
                    y1,
                    yQuarterLeft,
                    yMiddle
            );

    const double rightCurvature =
            normalizedError(
                    yMiddle,
                    yQuarterRight,
                    y2
            );

    const double leftSlope =
            slope(
                    x1,
                    y1,
                    midpoint,
                    yMiddle
            );

    const double rightSlope =
            slope(
                    midpoint,
                    yMiddle,
                    x2,
                    y2
            );

    const double leftQuarterSlope =
            slope(
                    x1,
                    y1,
                    quarterLeft,
                    yQuarterLeft
            );

    const double rightQuarterSlope =
            slope(
                    quarterRight,
                    yQuarterRight,
                    x2,
                    y2
            );

    const double endpointSlope =
            slope(
                    x1,
                    y1,
                    x2,
                    y2
            );

    const double slopeScale =
            std::max(
                    {
                            std::abs(leftSlope),
                            std::abs(rightSlope),
                            std::abs(leftQuarterSlope),
                            std::abs(rightQuarterSlope),
                            std::abs(endpointSlope),
                            1.0
                    }
            );

    const double slopeDifference =
            std::max(
                    {
                            std::abs(
                                    leftSlope -
                                            endpointSlope
                            ),
                            std::abs(
                                    rightSlope -
                                            endpointSlope
                            ),
                            std::abs(
                                    leftQuarterSlope -
                                            leftSlope
                            ),
                            std::abs(
                                    rightQuarterSlope -
                                            rightSlope
                            )
                    }
            );

    const double normalizedSlopeDifference =
            slopeDifference /
                    slopeScale;

    const bool strongCurvature =
            leftCurvature >
                    STRONG_CURVATURE_THRESHOLD ||
                    rightCurvature >
                            STRONG_CURVATURE_THRESHOLD;

    const bool curvature =
            leftCurvature >
                    CURVATURE_THRESHOLD ||
                    rightCurvature >
                            CURVATURE_THRESHOLD;

    const bool slopeChanging =
            normalizedSlopeDifference >
                    0.03;

    const bool extremeInteriorGrowth =
            likelyAsymptote(
                    y1,
                    yQuarterLeft,
                    yMiddle,
                    yQuarterRight,
                    y2
            );

    const bool endpointSignChange =
            signChanged(
                    y1,
                    y2
            );

    const bool leftSignChange =
            signChanged(
                    y1,
                    yQuarterLeft
            );

    const bool rightSignChange =
            signChanged(
                    yQuarterRight,
                    y2
            );

    const double endpointJump =
            std::abs(
                    y2 - y1
            );

    const double interiorJump =
            std::max(
                    {
                            std::abs(
                                    yQuarterLeft -
                                            y1
                            ),
                            std::abs(
                                    yMiddle -
                                            yQuarterLeft
                            ),
                            std::abs(
                                    yQuarterRight -
                                            yMiddle
                            ),
                            std::abs(
                                    y2 -
                                            yQuarterRight
                            )
                    }
            );

    const double valueScale =
            scaleOf(
                    y1,
                    yQuarterLeft,
                    yMiddle,
                    yQuarterRight,
                    y2
            );

    const double normalizedEndpointJump =
            endpointJump /
                    valueScale;

    const double normalizedInteriorJump =
            interiorJump /
                    valueScale;

    const bool veryLargeEndpointJump =
            normalizedEndpointJump >
                    0.90;

    const bool veryLargeInteriorJump =
            normalizedInteriorJump >
                    0.65;

    const bool signReversal =
            endpointSignChange ||
                    leftSignChange ||
                    rightSignChange;

    const bool discontinuityEvidence =
            extremeInteriorGrowth &&
                    (
                            signReversal ||
                                    veryLargeInteriorJump
                    );

    if (discontinuityEvidence) {
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

        return;
    }

    const bool needsSubdivision =
            strongCurvature ||
                    curvature ||
                    slopeChanging ||
                    veryLargeEndpointJump ||
                    veryLargeInteriorJump;

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

        if (
                !std::isfinite(x) &&
                        !std::isfinite(y) &&
                        !std::isfinite(previousX) &&
                        !std::isfinite(previousY)
                ) {
            return;
        }
    }

    points.push_back(x);
    points.push_back(y);
}