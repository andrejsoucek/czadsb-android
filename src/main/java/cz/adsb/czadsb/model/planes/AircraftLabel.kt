package cz.adsb.czadsb.model.planes

import android.view.View
import kotlinx.android.synthetic.main.aircraft_label.view.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class AircraftLabel(
    layoutResId: Int,
    mapView: MapView?,
    private val callsign: String?,
    private val registration: String?
) : InfoWindow(layoutResId, mapView) {
    override fun open(item: Any, position: GeoPoint, offsetX: Int, offsetY: Int) {
        super.open(item, (item as Marker).position, 0, 80)
    }

    override fun onOpen(item: Any?) {
        mView.reg_label_tv.text = registration
        if (callsign != null) {
            mView.callsign_label_tv.text = callsign
        } else {
            mView.callsign_label_tv.visibility = View.GONE
        }
    }

    override fun onClose() {
        //do nothing
    }
}