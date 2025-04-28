
::set ABI=armaebi-v7a
::set ABI=x86
::set ABI=arm64-v8a
set ABI=x86_64

set ANDROID_NDK=D:\ProgramFiles\Android\sdk\ndk\27.0.12077973
set TOOL_CHAIN=%ANDROID_NDK%\build\cmake\android.toolchain.cmake
set CMAKE=D:\ProgramFiles\Android\sdk\cmake\3.22.1\bin\cmake.exe
set PATH=%PATH%;D:\ProgramFiles\Android\sdk\cmake\3.22.1\bin

mkdir %ABI%
cd %ABI%

%CMAKE% -GNinja ../../mbedtls -DCMAKE_SYSTEM_NAME=Android -DCMAKE_SYSTEM_VERSION=21 -DANDROID_ABI=%ABI% -DCMAKE_TOOLCHAIN_FILE=%TOOL_CHAIN% -DUSE_SHARED_MBEDTLS_LIBRARY=On -DENABLE_TESTING=Off

%CMAKE% --build .