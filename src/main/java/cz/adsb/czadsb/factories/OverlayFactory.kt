package cz.adsb.czadsb.factories

import androidx.core.content.res.ResourcesCompat
import cz.adsb.czadsb.R
import cz.adsb.czadsb.model.Aircraft
import cz.adsb.czadsb.utils.AircraftLabel
import cz.adsb.czadsb.utils.getDrawableIdByName
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker

object OverlayFactory {

    fun createMarkersOverlay(map: MapView): FolderOverlay {
        val markersOverlay = FolderOverlay()
        map.overlays.add(markersOverlay)
        return markersOverlay
    }

    fun createAircraftMarker(map: MapView, aircraft: Aircraft) : Marker {
        val airlinerIcon = ResourcesCompat.getDrawable(map.context.resources, map.context.getDrawableIdByName(aircraft.iconName), null)
        val aMarker = Marker(map)
        aMarker.setIcon(airlinerIcon)
        aMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        aMarker.position = aircraft.position
        // do not rotate balloon and radar icon
        if (!(aircraft.type == "BALL" || aircraft.type == "RADAR")) {
            aMarker.rotation = aircraft.hdg!!.toFloat()
        }
        aMarker.isDraggable = false
        aMarker.infoWindow = AircraftLabel(R.layout.aircraft_label, map, aircraft.callsign, aircraft.registration)
        return aMarker
    }
}