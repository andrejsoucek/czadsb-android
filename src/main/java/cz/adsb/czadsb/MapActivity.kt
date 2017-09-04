package cz.adsb.czadsb

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.res.ResourcesCompat
import cz.adsb.czadsb.model.AircraftList
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import kotlin.concurrent.timer


class MapActivity : AppCompatActivity() {

    private var aircraftList: AircraftList = AircraftList()
    private var aircraftMarkersMap: MutableMap<Number, Marker> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map)

        val map = find<MapView>(R.id.map)
        configureMap(map)

        val mOverlay = createMarkersOverlay(map)
        timer(null, false, 0, 5000, {initAirplanes(map, mOverlay)})
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }

    private fun configureMap(map: MapView) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(false)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(9)
        val startPoint = GeoPoint(50.0755381, 14.4378005)
        mapController.setCenter(startPoint)
    }

    private fun createMarkersOverlay(map: MapView) : FolderOverlay {
        val markersOverlay = FolderOverlay()
        map.overlays.add(markersOverlay)
        return markersOverlay
    }

    private fun initAirplanes(map: MapView, mOverlay: FolderOverlay) {
        if (map.getScreenRect(null).height() == 0) {
            map.addOnFirstLayoutListener { _, _, _, _, _ -> loadAirplanesForView(map, mOverlay) }
        } else {
            loadAirplanesForView(map, mOverlay)
        }
    }

    private fun loadAirplanesForView(map: MapView, mOverlay: FolderOverlay) {
        val north = map.boundingBox.latNorth
        val south = map.boundingBox.latSouth
        val west = map.boundingBox.lonWest
        val east = map.boundingBox.lonEast
        doAsync {
            aircraftList = PlanesFetcher.fetchAircrafts(aircraftList, north, south, west, east)
            val airlinerIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_airliner_icon, null)
            aircraftList.aircrafts.forEach {
                if (it.value.willShowOnMap()) {
                    if (aircraftMarkersMap.containsKey(it.value.id)) {
                        aircraftMarkersMap[it.value.id]?.position = it.value.position
                        aircraftMarkersMap[it.value.id]?.rotation = it.value.hdg!!.toFloat()
                    } else {
                        val aMarker = Marker(map)
                        aMarker.setIcon(airlinerIcon)
                        aMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        aMarker.position = it.value.position
                        aMarker.rotation = it.value.hdg!!.toFloat()
                        aMarker.title = it.value.callsign
                        aMarker.isDraggable = false
                        aircraftMarkersMap.put(it.value.id, aMarker)
                        mOverlay.add(aMarker)
                    }
                }
            }
            val toDelete = aircraftMarkersMap.keys.minus(aircraftList.aircrafts.keys)
            toDelete.forEach {
                mOverlay.items.remove(aircraftMarkersMap[it])
                aircraftMarkersMap.remove(it)
            }

            uiThread {
                map.invalidate()
            }
        }
    }
}
