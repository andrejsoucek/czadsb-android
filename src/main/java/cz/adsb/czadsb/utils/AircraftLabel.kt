package cz.adsb.czadsb.utils

import android.view.View
import android.widget.TextView
import cz.adsb.czadsb.R
import org.jetbrains.anko.find
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.infowindow.InfoWindow

class AircraftLabel(layoutResId: Int,
                    mapView: MapView?,
                    private val callsign: String?,
                    private val registration: String?) : InfoWindow(layoutResId, mapView) {

    override fun onOpen(item: Any?) {
        mView.find<TextView>(R.id.reg_label_tv).text = registration
        if (callsign != null) {
            mView.find<TextView>(R.id.callsign_label_tv).text = callsign
        } else {
            mView.find<TextView>(R.id.callsign_label_tv).visibility = View.GONE
        }
    }

    override fun onClose() {
        //do nothing
    }
}