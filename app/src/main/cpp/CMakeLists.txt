cmake_minimum_required(VERSION 3.22.1)

project("fclient1")

add_library(fclient SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        native-lib.cpp)

target_link_libraries(fclient
        # List libraries link to the target library
        android
        spdlog
        mbedcrypto
        ${log-lib}
        log)

add_library(spdlog STATIC IMPORTED)
set_target_properties(spdlog PROPERTIES IMPORTED_LOCATION
        C:/Users/Chak_74/AndroidStudioProjects/libs/spdlog/build/${ANDROID_ABI}/libspdlog.a)
target_include_directories(fclient PUBLIC
        C:/Users/Chak_74/AndroidStudioProjects/libs/spdlog/spdlog/include)


add_library(mbedcrypto SHARED IMPORTED)

set_target_properties(mbedcrypto PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/../nativeLibs/${ANDROID_ABI}/libmbedcrypto.so)

target_include_directories(fclient PUBLIC
        C:/Users/Chak_74/AndroidStudioProjects/libs/spdlog/spdlog/include
        C:/Users/Chak_74/AndroidStudioProjects/libs/mbedtls/mbedtls/include
)