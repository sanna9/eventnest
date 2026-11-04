package com.example.eventnest.util;

import android.graphics.Insets;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class UiUtils {

    public UiUtils() {
    }

    /**
     * Pads the root view so content avoids status bar, navigation bar and keyboard.
     *
     * @param root layout's root view that need for padding
     * @see <a href="https://developer.android.com/develop/ui/views/layout/edge-to-edge">Android Developers: Edge-to-edge display</a>
     * @see <a href="https://developer.android.com/reference/androidx/core/view/WindowInsetsCompat">Android Developers: WindowInsetsCompat</a>
     */
    public static void applySystemBarPadding(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v,insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()).toPlatformInsets();
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
