package cz.adsb.czadsb.utils

import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import cz.adsb.czadsb.R
import java.util.*

/****** Context *********/
fun Context.getDrawableIdByName(resName: String) : Int {
    return try {
        this.resources.getIdentifier("marker_${resName}", "drawable", packageName)
    } catch (e: Exception) {
        R.drawable.marker_generic
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

fun Number?.toAltitude() : String {
    if (this == null) {
        return "N/A"
    }
    return this.toString() + " ft"
}

fun Number?.toHeading() : String {
    if (this == null) {
        return "N/A"
    }
    return this.toString() + "°"
}

fun Number?.toSpeed() : String {
    if (this == null) {
        return "N/A"
    }
    return this.toString() + " km/h"
}

/****** Bottom sheet states *********/
fun BottomSheetBehavior<View>.hide() {
    this.state = BottomSheetBehavior.STATE_HIDDEN
}

fun BottomSheetBehavior<View>.collapse() {
    this.state = BottomSheetBehavior.STATE_COLLAPSED
}

fun BottomSheetBehavior<View>.expand() {
    this.state = BottomSheetBehavior.STATE_EXPANDED
}

fun BottomSheetBehavior<View>.isHidden() : Boolean = this.state == BottomSheetBehavior.STATE_HIDDEN

fun BottomSheetBehavior<View>.isCollapsed() : Boolean = this.state == BottomSheetBehavior.STATE_COLLAPSED

fun BottomSheetBehavior<View>.isExpanded() : Boolean = this.state == BottomSheetBehavior.STATE_EXPANDED
