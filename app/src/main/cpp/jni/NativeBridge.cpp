#include <jni.h>
#include <limits>

#include "../math/Expression.h"
#include "../graph/GraphSampler.h"
extern "C"
JNIEXPORT jlong JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_createExpression(
        JNIEnv* env,
        jobject,
        jstring expression
) {
    const char* chars =
            env->GetStringUTFChars(expression, nullptr);

    try {
        auto* nativeExpression =
                new Expression(chars);

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        return reinterpret_cast<jlong>(nativeExpression);

    } catch (const std::exception& exception) {

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        jclass exceptionClass =
                env->FindClass(
                        "java/lang/IllegalArgumentException"
                );

        env->ThrowNew(
                exceptionClass,
                exception.what()
        );

        return 0;
    }
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_evaluateExpression(
        JNIEnv*,
        jobject,
        jlong handle,
        jdouble x
) {
    if (handle == 0) {
        return std::numeric_limits<double>::quiet_NaN();
    }

    auto* expression =
            reinterpret_cast<Expression*>(handle);

    return expression->evaluate(x);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_destroyExpression(
        JNIEnv*,
        jobject,
        jlong handle
) {
    auto* expression =
            reinterpret_cast<Expression*>(handle);

    delete expression;
}
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_generateGraph(
        JNIEnv* env,
        jobject,
        jlong handle,
        jdouble xMin,
        jdouble xMax,
        jint sampleCount
) {
    if (handle == 0) {
        return nullptr;
    }

    auto* expression =
            reinterpret_cast<Expression*>(handle);

    const std::vector<double> points =
            GraphSampler::sample(
                    *expression,
                    xMin,
                    xMax,
                    sampleCount
            );

    jdoubleArray result =
            env->NewDoubleArray(
                    static_cast<jsize>(points.size())
            );

    if (result == nullptr) {
        return nullptr;
    }

    env->SetDoubleArrayRegion(
            result,
            0,
            static_cast<jsize>(points.size()),
            points.data()
    );

    return result;
}