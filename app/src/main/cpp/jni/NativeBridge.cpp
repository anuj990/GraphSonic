#include <jni.h>

#include <cmath>
#include <memory>
#include <string>
#include <limits>
#include "../math/Expression.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_createExpression(
        JNIEnv* env,
        jobject,
        jstring expression
) {
    const char* expressionChars =
            env->GetStringUTFChars(expression, nullptr);

    try {
        auto* nativeExpression =
                new Expression(expressionChars);

        env->ReleaseStringUTFChars(
                expression,
                expressionChars
        );

        return reinterpret_cast<jlong>(nativeExpression);

    } catch (const std::exception& exception) {

        env->ReleaseStringUTFChars(
                expression,
                expressionChars
        );

        env->ThrowNew(
                env->FindClass("java/lang/IllegalArgumentException"),
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