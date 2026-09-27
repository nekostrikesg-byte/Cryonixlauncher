cmake_minimum_required(VERSION 3.22.1)

project("cryonix_native")

add_library(
    cryonix_native
    SHARED
    cryonix_native.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    cryonix_native
    ${log-lib}
)
