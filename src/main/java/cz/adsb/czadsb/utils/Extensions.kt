package cz.adsb.czadsb.utils

import android.support.design.widget.BottomSheetBehavior
import android.view.View

/**
 * Returns first x chars based on the param given.
 *
 * @param number the number of characters wanted.
 */
fun String.firstChars(chars: Int): String = this.substring(0, chars)


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
