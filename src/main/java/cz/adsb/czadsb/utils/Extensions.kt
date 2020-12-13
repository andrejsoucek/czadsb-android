package cz.adsb.czadsb.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import cz.adsb.czadsb.R
import java.util.*

/****** Context *********/
fun Context.getDrawableByName(resName: String): Drawable? {
    return try {
        ResourcesCompat.getDrawable(
            this.resources,
            this.resources.getIdentifier("marker_${resName}", "drawable", this.packageName),
            null
        )
    } catch (e: Resources.NotFoundException) {
        ResourcesCompat.getDrawable(
            this.resources,
            R.drawable.marker_generic,
            null
        )
    }
}

fun Context.getProperty(key: String): String {
    val p = Properties()
    p.load(this.assets.open("config.properties"))
    return p.getProperty(key)
}

/****** String fns *********/
/**
 * Returns first x chars based on the param given.
 *
 * @param chars the number of characters wanted.
 */
fun String.firstChars(chars: Int): String = this.substring(0, chars)

/**
 * Concats 2 Strings with given separator.
 * If one of the strings is null, then the not-null one is returned.
 *
 * @param add string to be added to the first one
 * @param separator string which is used to connect the two strings
 */
fun String?.concatenate(add: String?, separator: String = ""): String? {
    if (this == null) {
        return add
    }
    return if (add != null) {
        this.plus(separator).plus(add)
    } else {
        this
    }
}

fun Number?.toAltitude(): String {
    if (this == null) {
        return "N/A"
    }
    return "$this ft"
}

fun Number?.toHeading(): String {
    if (this == null) {
        return "N/A"
    }
    return "$this°"
}

fun Number?.toSpeed(): String {
    if (this == null) {
        return "N/A"
    }
    return "$this km/h"
}

/****** Observer *******/
fun <V, E : Event<V>> LiveData<E>.observeEvent(
    lifecycleOwner: LifecycleOwner,
    observer: (V) -> Unit
) {
    observe(lifecycleOwner) { event ->
        event.getContentIfNotHandled()?.let { observer(it) }
    }
}