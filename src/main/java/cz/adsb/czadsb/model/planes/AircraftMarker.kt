package cz.adsb.czadsb.model.planes

import androidx.core.content.res.ResourcesCompat
import cz.adsb.czadsb.R
import cz.adsb.czadsb.utils.getDrawableIdByName
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AircraftMarker(mapView: MapView, private val id: Number) : Marker(mapView) {
    fun getAircraftId(): Number
    {
        return this.id
    }

    companion object Factory {
        fun create(map: MapView, aircraft: Aircraft): AircraftMarker
        {
            val airlinerIcon = ResourcesCompat.getDrawable(map.context.resources, map.context.getDrawableIdByName(aircraft.iconName), null)
            val marker = AircraftMarker(map, aircraft.id)
            marker.setIcon(airlinerIcon)
            marker.setAnchor(ANCHOR_CENTER, ANCHOR_CENTER)
            marker.position = aircraft.position
            marker.isDraggable = false
            marker.infoWindow = AircraftLabel(R.layout.aircraft_label, map, aircraft.callsign, aircraft.registration)
            marker.setPanToView(true)

            // do not rotate balloon and radar icon
            if (!(aircraft.type == "BALL" || aircraft.type == "RADAR")) {
                marker.rotation = aircraft.hdg?.toFloat()?.times(-1) ?: 0f;
            }

            return marker
        }
    }
}