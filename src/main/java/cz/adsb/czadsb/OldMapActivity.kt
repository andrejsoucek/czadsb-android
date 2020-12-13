//package cz.adsb.czadsb
//
//import android.app.Dialog
//import android.os.Bundle
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.whenStarted
//import androidx.preference.PreferenceManager
//import com.auth0.android.Auth0
//import com.auth0.android.Auth0Exception
//import com.auth0.android.authentication.AuthenticationException
//import com.auth0.android.provider.AuthCallback
//import com.auth0.android.provider.VoidCallback
//import com.auth0.android.provider.WebAuthProvider
//import com.auth0.android.result.Credentials
//import com.github.clans.fab.FloatingActionMenu
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//import cz.adsb.czadsb.factories.OverlayFactory
//import cz.adsb.czadsb.model.radar.Aircraft
//import cz.adsb.czadsb.model.radar.AircraftList
//import cz.adsb.czadsb.model.user.User
//import cz.adsb.czadsb.model.user.Authenticator
//import cz.adsb.czadsb.model.planes.PlanesFetcher
//import cz.adsb.czadsb.utils.*
//import kotlinx.android.synthetic.main.activity_map.*
//import org.osmdroid.config.Configuration
//import org.osmdroid.events.MapEventsReceiver
//import org.osmdroid.tileprovider.tilesource.TileSourceFactory
//import org.osmdroid.util.GeoPoint
//import org.osmdroid.views.MapView
//import org.osmdroid.views.overlay.FolderOverlay
//import org.osmdroid.views.overlay.MapEventsOverlay
//import org.osmdroid.views.overlay.Marker
//import java.util.*
//import kotlin.concurrent.timer
//import kotlinx.android.synthetic.main.aircraft_info.*
//import kotlinx.android.synthetic.main.aircraft_info_full.*
//import kotlinx.android.synthetic.main.aircraft_info_peek.*
//import kotlinx.android.synthetic.main.floating_menu.*
//import kotlinx.coroutines.*
//import org.osmdroid.views.CustomZoomButtonsController
//
//
//class OldMapActivity : AppCompatActivity(), MapEventsReceiver {
//
//    private var aircraftList: AircraftList = AircraftList()
//    private var aircraftMarkersMap: MutableMap<Number, Marker> = mutableMapOf()
//    private var selectedAircraftId: Number? = null
//    private lateinit var bSheetBehavior: BottomSheetBehavior<View>
//    private lateinit var actionMenu: FloatingActionMenu
//
//    override fun onCreate(savedInstanceState: Bundle?)
//    {
//        super.onCreate(savedInstanceState)
//        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
//        setContentView(R.layout.activity_map)
//
//        val mapView = configureMap(map)
//
//        actionMenu = configureActionMenu(mapView)
//        bSheetBehavior = configureBottomSheet(mapView)
//        bSheetBehavior.hide()
//
//        if (User.isLoggedIn(this)) {
//            val markersOverlay = OverlayFactory.createMarkersOverlay(mapView)
//            timer(null, false, 0, getRefreshRate()) {
//                refreshAircrafts(map, markersOverlay)
//                runOnUiThread {
//                    centerMapOnSelectedAircraft(map)
//                    refreshAircraftInfo()
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
//    }
//
//    private fun refreshAircrafts(map: MapView, markersOverlay: FolderOverlay) {
//        if (map.getScreenRect(null).height() == 0) {
//            map.addOnFirstLayoutListener { _, _, _, _, _ -> lifecycleScope.launch { loadAircraftsForView(map, markersOverlay) }}
//        } else {
//            lifecycleScope.launch { loadAircraftsForView(map, markersOverlay) }
//        }
//    }
//
//    private fun selectAircraft(aircraft: Aircraft) : Boolean {
//        selectedAircraftId = aircraft.id
//        if (bSheetBehavior.isHidden()) {
//            bSheetBehavior.collapse()
//        }
//        fillStaticAircraftInfo(aircraft)
//        refreshAircraftInfo()
//        return true
//    }
//
//    private fun fillStaticAircraftInfo(aircraft: Aircraft) {
//        // peeking
//        callsign.text = aircraft.callsign
//        operator.text = aircraft.operator
//        from.text = aircraft.from?.firstChars(3) ?: "N/A"
//        to.text = aircraft.to?.firstChars(3) ?: "N/A"
//        ac_type.text = aircraft.manufacturer?.concatenate(aircraft.type, " ") ?: "N/A"
//        registration.text = aircraft.registration ?: "Reg. N/A"
//        // expanded
//        modelTV.text = aircraft.model ?: getString(R.string.unknown_aircraft)
//        icaoTV.text = aircraft.icao ?: "N/A"
//    }
//
//    private fun refreshAircraftInfo() {
//        if (selectedAircraftId != null) {
//            val aircraft = aircraftList.aircrafts[selectedAircraftId]
//            if (aircraft?.onGround == true) {
//                altTV.text = "GND"
//            } else {
//                altTV.text = aircraft?.amslAlt.toAltitude()
//            }
//            speedTV.text = aircraft?.spd.toSpeed()
//            headingTV.text = aircraft?.hdg.toHeading()
//            squawkTV.text = aircraft?.squawk ?: "N/A"
//        }
//    }
//
//    private fun centerMapOnSelectedAircraft(map: MapView) {
//        if (selectedAircraftId != null) {
//            val selAcMarker = aircraftMarkersMap[selectedAircraftId]
//            if (selAcMarker != null) {
//                map.controller.animateTo(selAcMarker.position)
//            }
//        }
//    }
//
//    private fun loadAircraftsForView(map: MapView, markersOverlay: FolderOverlay) {
//        lifecycleScope.launch {
//            whenStarted {
//                aircraftList = PlanesFetcher.fetch(
//                    applicationContext,
//                    aircraftList.lastDv,
//                    map.boundingBox.latNorth,
//                    map.boundingBox.latSouth,
//                    map.boundingBox.lonWest,
//                    map.boundingBox.lonEast
//                )
//                aircraftList.aircrafts.forEach {
//                    val aircraft = it.value
//                    if (aircraft.willShowOnMap()) {
//                        if (aircraftMarkersMap.containsKey(aircraft.id)) {
//                            aircraftMarkersMap[aircraft.id]!!.position = aircraft.position
//                            // do not rotate balloon icon
//                            if (!(aircraft.type == "BALL" || aircraft.type == "RADAR")) {
//                                aircraftMarkersMap[aircraft.id]!!.rotation = aircraft.hdg?.toFloat()?.times(-1) ?: 0f
//                            }
//                            // UGLY but working
//                            aircraftMarkersMap[aircraft.id]!!.infoWindow.draw()
//                        } else {
//                            val aMarker = OverlayFactory.createAircraftMarker(map, aircraft)
//                            aMarker.setOnMarkerClickListener { _, _ ->
//                                selectAircraft(aircraft)
//                            }
//                            aircraftMarkersMap.put(aircraft.id, aMarker)
//                            markersOverlay.add(aMarker)
//                            aMarker.showInfoWindow()
//                        }
//                    }
//                }
//                val toDelete = aircraftMarkersMap.keys.minus(aircraftList.aircrafts.keys)
//                toDelete.forEach {
//                    if (it == selectedAircraftId) {
//                        return@forEach
//                    }
//                    val marker = aircraftMarkersMap[it]
//                    marker?.closeInfoWindow()
//                    markersOverlay.items.remove(marker)
//                    aircraftMarkersMap.remove(it)
//                }
//
//                map.invalidate()
//            }
//        }
//    }
//
//    private fun getRefreshRate() : Long {
//        return getProperty("refresh_rate").toLong()
//    }
//
//    /********************* LAYOUT CONFIG ********************/
//
//    private fun configureMap(map: MapView): MapView {
//        val defaultMapCenter = GeoPoint(50.0755381, 14.4378005);
//        map.setTileSource(TileSourceFactory.MAPNIK)
//        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER);
//        map.setMultiTouchControls(true)
//
//        val mapController = map.controller
//        mapController.setCenter(defaultMapCenter)
//        mapController.setZoom(9.0)
//
//        val eventsOverlay = MapEventsOverlay(this)
//        map.overlays.add(eventsOverlay)
//
//        map.setOnTouchListener(View.OnTouchListener { _, event ->
//            if (selectedAircraftId != null) {
//                if (event.action == MotionEvent.ACTION_UP) {
//                    mapController.setCenter(aircraftMarkersMap[selectedAircraftId]?.position ?: defaultMapCenter)
//                    return@OnTouchListener false
//                }
//            }
//            false
//        })
//        return map;
//    }
//
//    private fun configureActionMenu(map: MapView): FloatingActionMenu {
//        floating_action_menu.setOnMenuToggleListener {
//            when (User.isLoggedIn(applicationContext)) {
//                true -> login_button.labelText = getString(R.string.Logout)
//                false -> login_button.labelText = getString(R.string.Login)
//            }
//        }
//
//        login_button.setOnClickListener {
//            val auth0 = Auth0(this)
//            auth0.isOIDCConformant = true
//            if (User.isLoggedIn(applicationContext)) {
//                WebAuthProvider.logout(auth0)
//                    .withScheme("demo")
//                    .start(this, object : VoidCallback {
//                        override fun onSuccess(payload: Void?) {}
//                        override fun onFailure(error: Auth0Exception) {
//                            // Show error to user
//                        }
//                    })
//                User.logout(applicationContext);
//                Toast.makeText(
//                    applicationContext,
//                    R.string.you_have_been_logged_out,
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                val authenticator = Authenticator(auth0)
//                authenticator.authenticate(this@OldMapActivity, object : AuthCallback {
//                    override fun onFailure(dialog: Dialog) {
//                    }
//
//                    override fun onFailure(exception: AuthenticationException) {
//                        Log.e("auth", "authentication failed", exception)
//                    }
//
//                    override fun onSuccess(credentials: Credentials) {
//                        User.login(applicationContext, credentials)
//                    }
//                })
//            }
//            floating_action_menu.close(true)
//        }
//        filters_button.setOnClickListener {
//            //TODO
//        }
//        preferences_button.setOnClickListener {
//            //TODO
//        }
//        return floating_action_menu
//    }
//
//    private fun configureBottomSheet(map: MapView) : BottomSheetBehavior<View> {
//        bSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
//        bSheetBehavior.addBottomSheetCallback(object:BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//                        actionMenu.visibility = View.GONE
//                        actionMenu.close(false)
//                        map.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//                    }
//                    BottomSheetBehavior.STATE_EXPANDED -> {
//                        if (map.layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
//                            map.layoutParams.height = map.height - bottom_sheet.height
//                        }
//                    }
//                    BottomSheetBehavior.STATE_HIDDEN -> {
//                        actionMenu.visibility = View.VISIBLE
//                        selectedAircraftId = null
//                        map.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//                    }
//                }
//                map.requestLayout()
//                //centering needs to be delayed
//                centerMapOnSelectedAircraft(map)
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//            }
//        })
//        return bSheetBehavior
//    }
//
//    /********************* MAP LISTENERS ********************/
//
//    override fun longPressHelper(p: GeoPoint?): Boolean {
//        return true
//    }
//
//    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
//        if (selectedAircraftId != null) {
//            selectedAircraftId = null
//            bSheetBehavior.hide()
//        }
//        if (actionMenu.isOpened) {
//            actionMenu.close(true)
//        }
//        return false
//    }
//}