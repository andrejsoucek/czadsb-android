package cz.adsb.czadsb

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.widget.TextView
import cz.adsb.czadsb.model.Aircraft
import cz.adsb.czadsb.model.AircraftList
import cz.adsb.czadsb.utils.PlanesFetcher
import cz.adsb.czadsb.utils.collapse
import cz.adsb.czadsb.utils.firstChars
import cz.adsb.czadsb.utils.hide
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import kotlin.concurrent.timer


class MapActivity : AppCompatActivity(), MapEventsReceiver {

    private var aircraftList: AircraftList = AircraftList()
    private var aircraftMarkersMap: MutableMap<Number, Marker> = mutableMapOf()
    private var selectedAircraft: Aircraft? = null
    private lateinit var bSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map)

        val bSheet = find<View>(R.id.bottom_sheet)
        bSheetBehavior = BottomSheetBehavior.from(bSheet)
        bSheetBehavior.hide()

        val map = find<MapView>(R.id.map)
        configureMap(map)

        val mOverlay = createMarkersOverlay(map)
        timer(null, false, 0, 5000, {
            refreshAirplanes(map, mOverlay)
            refreshAircraftInfo()
        })
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
        val eventsOverlay = MapEventsOverlay(this)
        map.overlays.add(eventsOverlay)
    }

    private fun createMarkersOverlay(map: MapView) : FolderOverlay {
        val markersOverlay = FolderOverlay()
        map.overlays.add(markersOverlay)
        return markersOverlay
    }

    private fun refreshAirplanes(map: MapView, mOverlay: FolderOverlay) {
        if (map.getScreenRect(null).height() == 0) {
            map.addOnFirstLayoutListener { _, _, _, _, _ -> loadAirplanesForView(map, mOverlay) }
        } else {
            loadAirplanesForView(map, mOverlay)
        }
    }

    private fun selectAircraft(aircraft: Aircraft) : Boolean {
        selectedAircraft = aircraft
        bSheetBehavior.collapse()
        fillStaticAircraftInfo(aircraft)
        refreshAircraftInfo()
        return true
    }

    private fun fillStaticAircraftInfo(aircraft:Aircraft) {
        find<TextView>(R.id.callsign).text = aircraft.callsign
        find<TextView>(R.id.operator).text = aircraft.operator
        find<TextView>(R.id.from).text = aircraft.from?.firstChars(3) ?: "N/A"
        find<TextView>(R.id.to).text = aircraft.to?.firstChars(3) ?: "N/A"
        find<TextView>(R.id.ac_type).text = aircraft.manufacturer + " " + aircraft.type
        find<TextView>(R.id.registration).text = aircraft.registration ?: "Reg. N/A"
    }

    private fun refreshAircraftInfo() {
        if (selectedAircraft != null) {
            val x = selectedAircraft!!.callsign
            println("refreshing dynamic data for: $x")
            //TODO refresh dynamic data
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
                        aMarker.setOnMarkerClickListener { marker, mapView ->
                            mapView.controller.animateTo(marker.position)
                            selectAircraft(it.value)
                        }
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

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return true
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        if (selectedAircraft != null) {
            selectedAircraft = null
            bSheetBehavior.hide()
        }
        return false
    }
}
