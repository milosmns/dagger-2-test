package me.angrybyte.dagger2test;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.disposables.Disposable;
import me.angrybyte.dagger2test.components.QuickComponent;
import me.angrybyte.dagger2test.components.SlowComponent;

/**
 * Utilities for this test
 */
public class Utils {

    /**
     * Generates a random color, at least as bright as the given parameter.
     *
     * @param smallestBrightness The smallest brightness possible
     * @return A random, fully opaque color
     */
    @ColorInt
    public static int randomColor(@IntRange(from = 0, to = 255) final int smallestBrightness) {
        final int randomR = (int) Math.floor((double) smallestBrightness + (Math.random() * (255d - smallestBrightness)));
        final int randomG = (int) Math.floor((double) smallestBrightness + (Math.random() * (255d - smallestBrightness)));
        final int randomB = (int) Math.floor((double) smallestBrightness + (Math.random() * (255d - smallestBrightness)));
        return Color.argb(255, randomR, randomG, randomB);
    }

    /**
     * Removes the package name for {@link SlowComponent} and {@link QuickComponent}; returns {@link Object#toString()} value for other objects. Null
     * generates the "null" string.
     *
     * @param object  Which object to convert
     * @param context Which Context to use for the package name
     * @return The shortened String, if possible
     */
    @NonNull
    public static String toStringShort(@Nullable final Object object, @NonNull final Context context) {
        if (object instanceof SlowComponent) {
            return ((SlowComponent) object).getWebClient().toString();
        } else if (object instanceof QuickComponent) {
            return ((QuickComponent) object).getWebClient().toString();
        } else {
            return String.valueOf(object).replace(context.getPackageName() + ".", "");
        }
    }

    /**
     * Disposes the given disposable list. If {@code null} or already disposed, nothing happens.
     *
     * @param disposables The disposable list
     */
    public static void dispose(@Nullable final Disposable... disposables) {
        if (disposables == null || disposables.length == 0) {
            return;
        }
        for (@Nullable final Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

}
