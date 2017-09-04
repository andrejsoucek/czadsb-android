package cz.adsb.czadsb

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map)

        val map = find<MapView>(R.id.map)
        configureMap(map)
        initAirplanes(map)
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

    private fun initAirplanes(map: MapView) {
        if (map.getScreenRect(null).height() == 0) {
            map.addOnFirstLayoutListener { _, _, _, _, _ -> loadAirplanesForView(map) }
        } else {
            loadAirplanesForView(map)
        }
    }

    private fun loadAirplanesForView(map: MapView) {
        val north = map.boundingBox.latNorth
        val south = map.boundingBox.latSouth
        val west = map.boundingBox.lonWest
        val east = map.boundingBox.lonEast
        doAsync {
            val aircraftList = PlanesFetcher.fetchAircrafts(north, south, west, east)
            aircraftList.acList.forEach {
                if (it.lat !== null && it.long !== null && it.amslAlt !== null && it.hdg !== null) {
                    val planeMarker = Marker(map)
                    val pos = GeoPoint(it.lat!!.toDouble(), it.long!!.toDouble(), it.amslAlt!!.toDouble())
                    planeMarker.position = pos
                    planeMarker.rotation = it.hdg!!.toFloat()
                    planeMarker.title = it.callsign
                    planeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    map.overlays.add(planeMarker)
                }
            }
            uiThread {
                map.invalidate()
            }
        }
    }
}
