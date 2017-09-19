package cz.adsb.czadsb

import android.content.Context
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetBehavior
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import cz.adsb.czadsb.factories.DialogFactory
import cz.adsb.czadsb.factories.OverlayFactory
import cz.adsb.czadsb.model.Aircraft
import cz.adsb.czadsb.model.AircraftList
import cz.adsb.czadsb.utils.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.*
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask


class MapActivity : AppCompatActivity(), MapEventsReceiver {

    private var aircraftList: AircraftList = AircraftList()
    private var aircraftMarkersMap: MutableMap<Number, Marker> = mutableMapOf()
    private var selectedAircraftId: Number? = null
    private lateinit var bSheetBehavior: BottomSheetBehavior<View>
    private lateinit var actionMenu: FloatingActionMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map)

        val map = find<MapView>(R.id.map)
        configureMap(map)

        actionMenu = configureActionMenu(map)

        bSheetBehavior = configureBottomSheet(map)
        bSheetBehavior.hide()


        val mOverlay = OverlayFactory.createMarkersOverlay(map)
        timer(null, false, 0, 5000, {
            refreshAircrafts(map, mOverlay)
            refreshAircraftInfo()
            centerMapOnSelectedAircraft(map)
        })
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }

    private fun getStartPoint() : GeoPoint {
        val locationManger: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastLoc = locationManger.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        return if (lastLoc != null) {
            GeoPoint(lastLoc.latitude, lastLoc.longitude)
        } else {
            GeoPoint(50.0755381, 14.4378005)
        }
    }

    private fun refreshAircrafts(map: MapView, mOverlay: FolderOverlay) {
        if (map.getScreenRect(null).height() == 0) {
            map.addOnFirstLayoutListener { _, _, _, _, _ -> loadAircraftsForView(map, mOverlay) }
        } else {
            loadAircraftsForView(map, mOverlay)
        }
    }

    private fun selectAircraft(aircraft: Aircraft) : Boolean {
        selectedAircraftId = aircraft.id
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
        find<TextView>(R.id.ac_type).text = aircraft.manufacturer?.concatenate(aircraft.type, " ") ?: "N/A"
        find<TextView>(R.id.registration).text = aircraft.registration ?: "Reg. N/A"
    }

    private fun refreshAircraftInfo() {
        if (selectedAircraftId != null) {
            val x = selectedAircraftId!!
            println("refreshing dynamic data for aircraft ID: $x")
            //TODO refresh dynamic data
        }
    }

    private fun centerMapOnSelectedAircraft(map: MapView) {
        if (selectedAircraftId != null) {
            val selAcMarker = aircraftMarkersMap[selectedAircraftId!!]
            if (selAcMarker != null) {
                map.controller.animateTo(selAcMarker.position)
            }
        }
    }

    private fun loadAircraftsForView(map: MapView, mOverlay: FolderOverlay) {
        val north = map.boundingBox.latNorth
        val south = map.boundingBox.latSouth
        val west = map.boundingBox.lonWest
        val east = map.boundingBox.lonEast
        doAsync {
            aircraftList = PlanesFetcher.fetchAircrafts(applicationContext, aircraftList, north, south, west, east)
            aircraftList.aircrafts.forEach {
                val aircraft = it.value
                if (aircraft.willShowOnMap()) {
                    if (aircraftMarkersMap.containsKey(aircraft.id)) {
                        aircraftMarkersMap[aircraft.id]?.position = aircraft.position
                        // do not rotate balloon icon
                        if (!(aircraft.type == "BALL" || aircraft.type == "RADAR")) {
                            aircraftMarkersMap[aircraft.id]?.rotation = aircraft.hdg!!.toFloat()
                        }
                        uiThread {
                            // UGLY but working
                            aircraftMarkersMap[aircraft.id]?.closeInfoWindow()
                            aircraftMarkersMap[aircraft.id]?.showInfoWindow()
                        }
                    } else {
                        val aMarker = OverlayFactory.createAircraftMarker(map, aircraft)
                        aMarker.setOnMarkerClickListener { marker, mapView ->
                            mapView.controller.animateTo(marker.position)
                            selectAircraft(aircraft)
                        }
                        aircraftMarkersMap.put(aircraft.id, aMarker)
                        mOverlay.add(aMarker)
                        uiThread {
                            aMarker.showInfoWindow()
                        }
                    }
                }
            }
            val toDelete = aircraftMarkersMap.keys.minus(aircraftList.aircrafts.keys)
            toDelete.forEach {
                if (it == selectedAircraftId) {
                    return@forEach
                }
                val marker = aircraftMarkersMap[it]
                mOverlay.items.remove(marker)
                aircraftMarkersMap.remove(it)
                uiThread {
                    marker?.closeInfoWindow()
                }
            }

            uiThread {
                map.invalidate()
            }
        }
    }

    /********************* LAYOUT CONFIG ********************/

    private fun configureMap(map: MapView) {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(false)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        val startPoint = getStartPoint()
        mapController.setCenter(startPoint)
        mapController.setZoom(9)

        val eventsOverlay = MapEventsOverlay(this)
        map.overlays.add(eventsOverlay)

        map.setOnTouchListener(View.OnTouchListener { v, event ->
            if (selectedAircraftId != null) {
                if (event.action == MotionEvent.ACTION_UP) {
                    mapController.setCenter(aircraftMarkersMap[selectedAircraftId!!]?.position)
                    return@OnTouchListener false
                }
            }
            false
        })
    }

    private fun configureActionMenu(map: MapView): FloatingActionMenu {
        val menu = find<FloatingActionMenu>(R.id.floating_action_menu)
        val loginBtn = find<FloatingActionButton>(R.id.login_button)
        val filtersBtn = find<FloatingActionButton>(R.id.filters_button)
        val preferencesBtn = find<FloatingActionButton>(R.id.preferences_button)

        menu.setOnMenuToggleListener {
            when (UserManager.isUserLoggedIn(applicationContext)) {
                true -> loginBtn.labelText = getString(R.string.Logout)
                false -> loginBtn.labelText = getString(R.string.Login)
            }
        }

        loginBtn.setOnClickListener {
            when (UserManager.isUserLoggedIn(applicationContext)) {
                true -> { UserManager.logout(applicationContext); toast(R.string.you_have_been_logged_out) }
                false -> DialogFactory.createLoginDialog(map).show()
            }
            menu.close(true)
        }
        filtersBtn.setOnClickListener {
            //TODO
        }
        preferencesBtn.setOnClickListener {
            //TODO
        }
        return menu
    }

    private fun configureBottomSheet(map: MapView) : BottomSheetBehavior<View> {
        val bSheet = find<View>(R.id.bottom_sheet)
        bSheetBehavior = BottomSheetBehavior.from(bSheet)
        bSheetBehavior.setBottomSheetCallback(object:BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        actionMenu.visibility = View.GONE
                        actionMenu.close(false)
                        map.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        if (map.layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                            map.layoutParams.height = map.height - find<LinearLayout>(R.id.bottom_sheet).height
                        }
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        actionMenu.visibility = View.VISIBLE
                        selectedAircraftId = null
                        map.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
                map.requestLayout()
                //centering needs to be delayed
                Timer().schedule(timerTask {centerMapOnSelectedAircraft(map)}, 50)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        return bSheetBehavior
    }

    /********************* MAP LISTENERS ********************/

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return true
    }

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        if (selectedAircraftId != null) {
            selectedAircraftId = null
            bSheetBehavior.hide()
        }
        if (actionMenu.isOpened) {
            actionMenu.close(true)
        }
        return false
    }
}
