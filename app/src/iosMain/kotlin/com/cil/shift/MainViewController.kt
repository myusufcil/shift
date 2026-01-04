package com.cil.shift

import androidx.compose.ui.window.ComposeUIViewController
import com.cil.shift.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()

    return ComposeUIViewController {
        App()
    }
}
