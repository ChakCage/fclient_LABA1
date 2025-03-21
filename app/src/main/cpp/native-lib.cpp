#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstring>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/des.h>
#include <spdlog/spdlog.h>
#include <spdlog/sinks/android_sink.h>

// Глобальные переменные для mbedtls
mbedtls_entropy_context entropy;
mbedtls_ctr_drbg_context ctr_drbg;
const char *personalization = "fclient-sample-app";

// Макрос для вывода в системный лог Android
#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, "fclient_ndk", __VA_ARGS__)

// Создаем объект android_logger для spdlog
auto android_logger = spdlog::android_logger_mt("android", "fclient_ndk");

extern "C" JNIEXPORT jstring JNICALL
Java_ru_bmstu_fclient_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++ with mbedtls and spdlog!";
    LOG_INFO("Hello from C++: %s", hello.c_str());
    android_logger->info("Hello from spdlog: {}", hello);
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jint JNICALL
Java_ru_bmstu_fclient_MainActivity_initRng(JNIEnv *env, jclass) {
    mbedtls_entropy_init(&entropy);
    mbedtls_ctr_drbg_init(&ctr_drbg);

    int ret = mbedtls_ctr_drbg_seed(&ctr_drbg,
                                    mbedtls_entropy_func,
                                    &entropy,
                                    (const unsigned char *)personalization,
                                    strlen(personalization));
    if (ret != 0) {
        LOG_INFO("RNG initialization failed: %d", ret);
    } else {
        LOG_INFO("RNG initialized successfully");
    }
    return ret;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_bmstu_fclient_MainActivity_randomBytes(JNIEnv *env, jclass, jint no) {
    uint8_t *buf = new uint8_t[no];
    int ret = mbedtls_ctr_drbg_random(&ctr_drbg, buf, no);
    if (ret != 0) {
        LOG_INFO("Random generation failed: %d", ret);
        delete[] buf;
        return env->NewByteArray(0);
    }

    jbyteArray rnd = env->NewByteArray(no);
    env->SetByteArrayRegion(rnd, 0, no, (jbyte *)buf);
    delete[] buf;
    return rnd;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_bmstu_fclient_MainActivity_encrypt(JNIEnv *env, jclass,
                                           jbyteArray key, jbyteArray data) {
    jsize ksz = env->GetArrayLength(key);
    jsize dsz = env->GetArrayLength(data);
    if ((ksz != 16) || (dsz <= 0)) {
        LOG_INFO("Invalid key or data length for encryption");
        return env->NewByteArray(0);
    }

    mbedtls_des3_context ctx;
    mbedtls_des3_init(&ctx);

    jbyte *pkey = env->GetByteArrayElements(key, nullptr);
    jbyte *pdata = env->GetByteArrayElements(data, nullptr);

    // PKCS#5 padding
    int pad = 8 - (dsz % 8);
    int sz = dsz + pad;
    uint8_t *buf = new uint8_t[sz];
    std::copy(pdata, pdata + dsz, buf);
    for (int i = 0; i < pad; i++) {
        buf[dsz + i] = pad;
    }

    mbedtls_des3_set2key_enc(&ctx, (uint8_t *)pkey);

    int blocks = sz / 8;
    for (int i = 0; i < blocks; i++) {
        mbedtls_des3_crypt_ecb(&ctx, buf + i * 8, buf + i * 8);
    }

    jbyteArray dout = env->NewByteArray(sz);
    env->SetByteArrayRegion(dout, 0, sz, (jbyte *)buf);

    delete[] buf;
    env->ReleaseByteArrayElements(key, pkey, 0);
    env->ReleaseByteArrayElements(data, pdata, 0);
    return dout;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_ru_bmstu_fclient_MainActivity_decrypt(JNIEnv *env, jclass,
                                           jbyteArray key, jbyteArray data) {
    jsize ksz = env->GetArrayLength(key);
    jsize dsz = env->GetArrayLength(data);
    if ((ksz != 16) || (dsz <= 0) || (dsz % 8 != 0)) {
        LOG_INFO("Invalid key or data length for decryption");
        return env->NewByteArray(0);
    }

    mbedtls_des3_context ctx;
    mbedtls_des3_init(&ctx);

    jbyte *pkey = env->GetByteArrayElements(key, nullptr);
    jbyte *pdata = env->GetByteArrayElements(data, nullptr);

    uint8_t *buf = new uint8_t[dsz];
    std::copy(pdata, pdata + dsz, buf);

    mbedtls_des3_set2key_dec(&ctx, (uint8_t *)pkey);

    int blocks = dsz / 8;
    for (int i = 0; i < blocks; i++) {
        mbedtls_des3_crypt_ecb(&ctx, buf + i * 8, buf + i * 8);
    }

    // Упрощенное удаление паддинга PKCS#5 (в реальном коде необходимо проверить каждый байт)
    int pad = buf[dsz - 1];
    int sz = dsz - pad;

    jbyteArray dout = env->NewByteArray(sz);
    env->SetByteArrayRegion(dout, 0, sz, (jbyte *)buf);

    delete[] buf;
    env->ReleaseByteArrayElements(key, pkey, 0);
    env->ReleaseByteArrayElements(data, pdata, 0);
    return dout;
}
