package ru.bmstu.fclient;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // Объявление нативных методов
    public native String stringFromJNI();
    public static native int initRng();
    public static native byte[] randomBytes(int no);
    public static native byte[] encrypt(byte[] key, byte[] data);
    public static native byte[] decrypt(byte[] key, byte[] data);

    static {
        // Загружаем основную нативную библиотеку (fclient) и mbedcrypto (shared object)
        System.loadLibrary("fclient");
        System.loadLibrary("mbedcrypto");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Предполагается, что в activity_main.xml есть TextView с id sample_text
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.sample_text);

        // Вызываем функцию stringFromJNI() из C++
        tv.setText(stringFromJNI());

        // Инициализируем RNG
        int rngStatus = initRng();
        tv.append("\ninitRng status: " + rngStatus);

        // Генерируем случайные байты
        byte[] randBytes = randomBytes(16);
        tv.append("\nRandom bytes: " + bytesToHex(randBytes));

        // Пример: шифрование и дешифрование
        byte[] key = new byte[16];
        // Для теста ключ можно заполнить произвольными данными
        for (int i = 0; i < 16; i++) {
            key[i] = (byte)i;
        }
        byte[] data = "Hello 3DES!".getBytes();

        byte[] encrypted = encrypt(key, data);
        tv.append("\nEncrypted: " + bytesToHex(encrypted));

        byte[] decrypted = decrypt(key, encrypted);
        tv.append("\nDecrypted: " + new String(decrypted));
    }

    // Вспомогательный метод для преобразования массива байт в шестнадцатеричную строку
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
