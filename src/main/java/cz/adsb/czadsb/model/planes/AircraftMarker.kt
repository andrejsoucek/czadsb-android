package cz.adsb.czadsb.model.planes

import cz.adsb.czadsb.R
import cz.adsb.czadsb.utils.getDrawableByName
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class AircraftMarker(mapView: MapView, private val aircraft: Aircraft) : Marker(mapView) {
    fun getAircraftId(): Number {
        return this.aircraft.id
    }

    override fun setRotation(rotation: Float) {
        if (this.aircraft.type == "BALL" || aircraft.type == "RADAR") {
            return
        }
        super.setRotation(rotation)
    }

    companion object Factory {
        fun create(map: MapView, aircraft: Aircraft): AircraftMarker {
            val marker = AircraftMarker(map, aircraft)
            marker.icon = map.context.getDrawableByName(aircraft.iconName)
            marker.setAnchor(ANCHOR_CENTER, ANCHOR_CENTER)
            marker.position = aircraft.position
            marker.isDraggable = false
            marker.infoWindow = AircraftLabel(
                R.layout.aircraft_label,
                map,
                aircraft.callsign,
                aircraft.registration
            )
            marker.setPanToView(true)
            marker.rotation = aircraft.hdg?.toFloat()?.times(-1) ?: 0f

            return marker
        }
    }
}