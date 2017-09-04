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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map)

        val map = find<MapView>(R.id.map)
        configureMap(map)

        val markersOverlay = createMarkersOverlay(map)
        timer(null, false, 0, 5000, {initAirplanes(map, markersOverlay)})
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }

    private fun configureMap(map: MapView) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
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
            mOverlay.items.forEach {
                mOverlay.remove(it)
            }
            aircraftList = PlanesFetcher.fetchAircrafts(aircraftList, north, south, west, east)
            val airlinerIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_airliner_icon, null)
            aircraftList.acList.forEach {
                if (it.lat !== null && it.long !== null && it.amslAlt !== null && it.hdg !== null) {
                    val aMarker = Marker(map)
                    val pos = GeoPoint(it.lat!!.toDouble(), it.long!!.toDouble(), it.amslAlt!!.toDouble())
                    aMarker.setIcon(airlinerIcon)
                    aMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    aMarker.position = pos
                    aMarker.rotation = it.hdg!!.toFloat()
                    aMarker.title = it.callsign
                    aMarker.isDraggable = false
                    mOverlay.add(aMarker)
                }
            }
            uiThread {
                map.invalidate()
            }
        }
    }
}
