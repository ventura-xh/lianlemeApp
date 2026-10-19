# ✅ 必须有：CMake 最低版本
cmake_minimum_required(VERSION 3.22)

# ✅ 必须有：项目名
project("native-lib")

# 你的库
add_library(
        native-lib
        SHARED
        native-test.cpp
)

# 链接日志库（如果你用了 __android_log_print）
find_library(
        log-lib
        log
)

target_link_libraries(
        native-lib
        ${log-lib}
)