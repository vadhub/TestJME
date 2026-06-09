package com.abg.testjem

import android.util.Log
import com.jme3.app.AndroidHarness


class MainActivity : AndroidHarness() {
    init {
        // Указываем полный путь к вашему главному игровому классу (из модуля core)

        try {
            Class.forName("com.abg.testjem.MainGame")
            Log.d("MainActivity", "Class found!")
        } catch (e: ClassNotFoundException) {
            Log.e("MainActivity", "Class NOT found", e)
        }
        appClass = "com.abg.testjem.MainGame"

        // Можно настроить заголовок диалога выхода
        exitDialogTitle = "Exit Game"
        exitDialogMessage = "Do you want to exit?"
        // Отключаем обработку выхода системой, если хотите управлять сами
        handleExitHook = true


    }
}