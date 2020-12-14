package cz.adsb.czadsb

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import cz.adsb.czadsb.model.planes.AircraftMarker
import cz.adsb.czadsb.utils.*
import cz.adsb.czadsb.viewmodel.AircraftInfoViewModel
import cz.adsb.czadsb.viewmodel.AircraftListViewModel
import cz.adsb.czadsb.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.aircraft_info.*
import kotlinx.android.synthetic.main.aircraft_info_full.*
import kotlinx.android.synthetic.main.aircraft_info_peek.*
import kotlinx.android.synthetic.main.floating_menu.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polyline
import java.util.*

class MapActivity : AppCompatActivity() {

    private val userViewModel by viewModels<UserViewModel>()

    private val aircraftInfoViewModel by viewModels<AircraftInfoViewModel>()

    private val aircraftListViewModel by viewModels<AircraftListViewModel>()

    private val markersOverlay = FolderOverlay()

    private val path = Polyline()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.userViewModel.userLoggedIn.observeEvent(this@MapActivity, {
            if (!it) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

        setContentView(R.layout.activity_map)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ), 1
        )

        initActionMenu()
        val map = initMap()
        val bs = initBottomSheet()

        observeAircraftInfo(bs)
        observeAircraftList(map)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    private fun initActionMenu() {
        login_button.setOnClickListener {
            this.userViewModel.performLogout(this@MapActivity)
        }
        filters_button.setOnClickListener {
            //TODO
        }
        preferences_button.setOnClickListener {
            //TODO
        }
    }

    private fun initMap(): MapView {
        Configuration.getInstance().load(
            this.applicationContext,
            PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        val defaultMapCenter = GeoPoint(50.0755381, 14.4378005)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setCenter(defaultMapCenter)
        mapController.setZoom(9.0)

        this.path.color = Color.MAGENTA
        this.path.width = 4.0f

        map.overlays.add(this.path)
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (floating_action_menu.isOpened) {
                    floating_action_menu.close(true)
                }
                this@MapActivity.aircraftInfoViewModel.selectedAircraft.value = null
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return true
            }
        }))
        map.overlays.add(this.markersOverlay)

        return map
    }

    private fun initBottomSheet(): BottomSheetBehavior<LinearLayout> {
        val bSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        bSheetBehavior.isHideable = true
        bSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        map.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        if (map.layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                            map.layoutParams.height = map.height - bottom_sheet.height
                        }
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        map.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                        this@MapActivity.aircraftInfoViewModel.selectedAircraft.value = null
                    }
                    else -> return
                }
                map.requestLayout()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        return bSheetBehavior
    }

    private fun observeAircraftInfo(bs: BottomSheetBehavior<LinearLayout>) {
        this.aircraftInfoViewModel.selectedAircraft.observe(this@MapActivity, {
            if (it != null) {
                map.controller.animateTo(it.position)
                callsign.text = it.callsign
                operator.text = it.operator
                from.text = it.from?.firstChars(3) ?: "N/A"
                to.text = it.to?.firstChars(3) ?: "N/A"
                ac_type.text = it.manufacturer?.concatenate(it.type, " ") ?: "N/A"
                registration.text = it.registration ?: "Reg. N/A"
                modelTV.text = it.model ?: getString(R.string.unknown_aircraft)
                icaoTV.text = it.icao ?: "N/A"
                this.aircraftInfoViewModel.state.value = AircraftInfoViewModel.State.PEEKING
                this.aircraftInfoViewModel.track.value = it.trackPoints.plus(it.position!!)
            } else {
                this.aircraftInfoViewModel.state.value = AircraftInfoViewModel.State.HIDDEN
                this.aircraftInfoViewModel.track.value = listOf()
            }
        })
        this.aircraftInfoViewModel.track.observe(this@MapActivity, {
            this@MapActivity.path.setPoints(it)
        })
        this.aircraftInfoViewModel.state.observe(this@MapActivity, {
            when (it) {
                AircraftInfoViewModel.State.PEEKING -> {
                    bs.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                AircraftInfoViewModel.State.OPEN -> {
                    bs.state = BottomSheetBehavior.STATE_EXPANDED
                }
                AircraftInfoViewModel.State.HIDDEN -> {
                    bs.state = BottomSheetBehavior.STATE_HIDDEN
                }
                else -> return@observe
            }
        })
    }

    private fun observeAircraftList(map: MapView) {
        this.aircraftListViewModel.event.observeEvent(this@MapActivity, {
            try {
                this@MapActivity.aircraftListViewModel.refreshAircraftList(
                    map.boundingBox.latNorth,
                    map.boundingBox.latSouth,
                    map.boundingBox.lonWest,
                    map.boundingBox.lonEast
                )
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show();
            }
        })
        this.aircraftListViewModel.aircraftList.observe(this@MapActivity, { aircraftList ->
            val updatedAircrafts = aircraftList.aircrafts
            val currentAircraftsMap: Map<Number, AircraftMarker> =
                this@MapActivity.markersOverlay.items.associateBy {
                    (it as AircraftMarker).getAircraftId()
                } as Map<Number, AircraftMarker>
            updatedAircrafts.forEach {
                if (it.value.willShowOnMap()) {
                    if (currentAircraftsMap.containsKey(it.value.id)) {
                        val marker = currentAircraftsMap[it.key] ?: error("Marker not found!")
                        marker.position = it.value.position
                        marker.rotation = it.value.hdg?.toFloat()?.times(-1) ?: 0f
                        marker.infoWindow.draw()
                    } else {
                        val marker = AircraftMarker.create(map, it.value)
                        this@MapActivity.markersOverlay.add(marker)
                        marker.setOnMarkerClickListener { _, _ ->
                            this@MapActivity.aircraftInfoViewModel.selectedAircraft.value = it.value
                            return@setOnMarkerClickListener true
                        }
                        marker.showInfoWindow()
                    }
                }
            }
            val toDelete = currentAircraftsMap.keys.minus(aircraftList.aircrafts.keys)
            toDelete.forEach {
                if (it == this@MapActivity.aircraftInfoViewModel.selectedAircraft.value?.id) {
                    return@forEach
                }
                val marker = currentAircraftsMap[it]
                marker?.closeInfoWindow()
                markersOverlay.items.remove(marker)
            }

            val selectedAircraft = this@MapActivity.aircraftInfoViewModel.selectedAircraft.value
            if (selectedAircraft != null && updatedAircrafts[selectedAircraft.id] != null) {
                val upToDateSelectedAircraft = updatedAircrafts[selectedAircraft.id]!!
                map.controller.animateTo(upToDateSelectedAircraft.position)
                altTV.text =
                    if (upToDateSelectedAircraft.onGround == true) "GND" else upToDateSelectedAircraft.amslAlt.toAltitude()
                speedTV.text = upToDateSelectedAircraft.spd.toSpeed()
                headingTV.text = upToDateSelectedAircraft.hdg.toHeading()
                squawkTV.text = upToDateSelectedAircraft.squawk ?: "N/A"
                this.aircraftInfoViewModel.track.value = upToDateSelectedAircraft.trackPoints
            }
            map.invalidate()
        })
        this.aircraftListViewModel.error.observe(this@MapActivity, {
            Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show()
        })
    }
}