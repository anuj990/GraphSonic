#include <jni.h>
#include <limits>
#include <exception>
#include <string>
#include <vector>

#include "../math/Expression.h"
#include "../graph/GraphSampler.h"

namespace {

    void throwIllegalArgumentException(
            JNIEnv* env,
            const char* message
    ) {
        jclass exceptionClass =
                env->FindClass(
                        "java/lang/IllegalArgumentException"
                );

        if (exceptionClass != nullptr) {
            env->ThrowNew(
                    exceptionClass,
                    message
            );
        }
    }

    void throwIllegalStateException(
            JNIEnv* env,
            const char* message
    ) {
        jclass exceptionClass =
                env->FindClass(
                        "java/lang/IllegalStateException"
                );

        if (exceptionClass != nullptr) {
            env->ThrowNew(
                    exceptionClass,
                    message
            );
        }
    }

}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_createExpression(
        JNIEnv* env,
        jobject,
        jstring expression
) {
    if (expression == nullptr) {
        throwIllegalArgumentException(
                env,
                "Expression cannot be null"
        );

        return 0;
    }

    const char* chars =
            env->GetStringUTFChars(
                    expression,
                    nullptr
            );

    if (chars == nullptr) {
        return 0;
    }

    try {

        auto* nativeExpression =
                new Expression(chars);

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        return reinterpret_cast<jlong>(
                nativeExpression
        );

    } catch (
            const std::exception& exception
    ) {

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        throwIllegalArgumentException(
                env,
                exception.what()
        );

        return 0;

    } catch (...) {

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        throwIllegalArgumentException(
                env,
                "Failed to create expression"
        );

        return 0;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_validateExpression(
        JNIEnv* env,
        jobject,
        jstring expression
) {
    if (expression == nullptr) {
        return env->NewStringUTF(
                "Expression cannot be null"
        );
    }

    const char* chars =
            env->GetStringUTFChars(
                    expression,
                    nullptr
            );

    if (chars == nullptr) {
        return env->NewStringUTF(
                "Unable to read expression"
        );
    }

    try {

        Expression nativeExpression(chars);

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        return nullptr;

    } catch (
            const std::exception& exception
    ) {

        const std::string message =
                exception.what();

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        return env->NewStringUTF(
                message.c_str()
        );

    } catch (...) {

        env->ReleaseStringUTFChars(
                expression,
                chars
        );

        return env->NewStringUTF(
                "Invalid expression"
        );
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_getCanonicalExpression(
        JNIEnv* env,
        jobject,
        jlong handle
) {
    if (handle == 0) {
        throwIllegalArgumentException(
                env,
                "Expression handle is invalid"
        );

        return nullptr;
    }

    auto* expression =
            reinterpret_cast<Expression*>(
                    handle
            );

    try {

        const std::string canonical =
                expression->canonical();

        return env->NewStringUTF(
                canonical.c_str()
        );

    } catch (
            const std::exception& exception
    ) {

        throwIllegalStateException(
                env,
                exception.what()
        );

        return nullptr;

    } catch (...) {

        throwIllegalStateException(
                env,
                "Failed to canonicalize expression"
        );

        return nullptr;
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
            reinterpret_cast<Expression*>(
                    handle
            );

    try {

        return expression->evaluate(
                x
        );

    } catch (...) {

        return std::numeric_limits<double>::quiet_NaN();
    }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_isDefined(
        JNIEnv*,
        jobject,
        jlong handle,
        jdouble x
) {
    if (handle == 0) {
        return JNI_FALSE;
    }

    auto* expression =
            reinterpret_cast<Expression*>(
                    handle
            );

    try {

        return expression->isDefined(x)
                ? JNI_TRUE
                : JNI_FALSE;

    } catch (...) {

        return JNI_FALSE;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_anuj_graphsonic_engine_NativeBridge_destroyExpression(
        JNIEnv*,
        jobject,
        jlong handle
) {
    if (handle == 0) {
        return;
    }

    auto* expression =
            reinterpret_cast<Expression*>(
                    handle
            );

    try {

        delete expression;

    } catch (...) {
    }
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
        throwIllegalArgumentException(
                env,
                "Expression handle is invalid"
        );

        return nullptr;
    }

    auto* expression =
            reinterpret_cast<Expression*>(
                    handle
            );

    try {

        const std::vector<double> points =
                GraphSampler::sample(
                        *expression,
                        xMin,
                        xMax,
                        sampleCount
                );

        jdoubleArray result =
                env->NewDoubleArray(
                        static_cast<jsize>(
                                points.size()
                        )
                );

        if (result == nullptr) {
            return nullptr;
        }

        if (!points.empty()) {
            env->SetDoubleArrayRegion(
                    result,
                    0,
                    static_cast<jsize>(
                            points.size()
                    ),
                    points.data()
            );
        }

        return result;

    } catch (
            const std::exception& exception
    ) {

        throwIllegalStateException(
                env,
                exception.what()
        );

        return nullptr;

    } catch (...) {

        throwIllegalStateException(
                env,
                "Failed to generate graph"
        );

        return nullptr;
    }
}