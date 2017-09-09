package cz.adsb.czadsb.utils

import android.widget.TextView
import cz.adsb.czadsb.R
import org.jetbrains.anko.find
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CallsignLabel(layoutResId: Int, mapView: MapView?) : InfoWindow(layoutResId, mapView) {

    override fun onOpen(item: Any?) {
        val marker = item as OverlayWithIW
        val title = marker.title
        mView.find<TextView>(R.id.callsign_label_tv).text = title
    }

    override fun onClose() {
        //do nothing
    }
}