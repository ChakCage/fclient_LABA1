cmake_minimum_required(VERSION 3.22.1)

project("fclient")

add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp)

add_library(spdlog STATIC IMPORTED)
set_target_properties(spdlog PROPERTIES IMPORTED_LOCATION
        D:/prog/RPO/libs/spdlog/build/${ANDROID_ABI}/libspdlog.a)
target_include_directories(fclient PUBLIC
        D:/prog/RPO/libs/spdlog/spdlog/include)

add_library(mbedcrypto SHARED IMPORTED)
set_target_properties(mbedcrypto PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../nativeLibs/${ANDROID_ABI}/library/libmbedcrypto.so)

target_include_directories(fclient PUBLIC
        D:/prog/RPO/libs/spdlog/spdlog/include
        D:/prog/RPO/libs/mbedtls/mbedtls/include
)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        spdlog
        mbedcrypto
        log
        ${log-lib} )