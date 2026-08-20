cmake_minimum_required(VERSION 3.22.1)

project("graphsonic")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
        graphsonic
        SHARED

        jni/NativeBridge.cpp

        math/Lexer.cpp
        math/Parser.cpp
        math/Evaluator.cpp
        math/Expression.cpp
        graph/GraphSampler.cpp
)

target_include_directories(
        graphsonic
        PRIVATE
        ${CMAKE_CURRENT_LIST_DIR}
)

find_library(
        log-lib
        log
)

target_link_libraries(
        graphsonic
        ${log-lib}
)