
cmake_minimum_required(VERSION 3.22.1)

project("graphsonic")

add_library(
        graphsonic
        SHARED
        graphsonic.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        graphsonic
        ${log-lib}
)