package cz.adsb.czadsb.view

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import cz.adsb.czadsb.BuildConfig
import cz.adsb.czadsb.R
import cz.adsb.czadsb.model.images.Image
import cz.adsb.czadsb.model.planes.Aircraft
import cz.adsb.czadsb.model.planes.AircraftList
import cz.adsb.czadsb.model.planes.AircraftMarker
import cz.adsb.czadsb.model.user.AuthenticationException
import cz.adsb.czadsb.utils.*
import cz.adsb.czadsb.viewmodel.AircraftInfoViewModel
import cz.adsb.czadsb.viewmodel.AircraftListViewModel
import cz.adsb.czadsb.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.aircraft_info.*
import kotlinx.android.synthetic.main.aircraft_info_full.*
import kotlinx.android.synthetic.main.aircraft_info_peek.*
import kotlinx.android.synthetic.main.floating_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polyline
import java.util.*


class MapActivity : AppCompatActivity() {

    private val userViewModel by viewModel<UserViewModel>()

    private val aircraftInfoViewModel by viewModel<AircraftInfoViewModel>()

    private val aircraftListViewModel by viewModel<AircraftListViewModel>()

    private val markersOverlay = FolderOverlay()

    private val path = Polyline()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.observeUser()

        setContentView(R.layout.activity_map)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ), 1
        )

        this.initActionMenu()
        val map = this.initMap()
        val bs = this.initBottomSheet()

        this.observeAircraftInfo(bs)
        map.addOnFirstLayoutListener { _, _, _, _, _ ->
            this.observeAircraftList(map)
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onBackPressed() {
        if (this.aircraftInfoViewModel.selectedAircraft.value == null && !floating_action_menu.isOpened) {
            super.onBackPressed()
        }
        floating_action_menu.close(true)
        this.aircraftInfoViewModel.selectedAircraft.value = null
    }

    private fun initActionMenu() {
        login_button.setOnClickListener {
            this.userViewModel.performLogout(this@MapActivity)
        }
        filters_button.setOnClickListener {
            Toast.makeText(applicationContext, "NOT IMPLEMENTED YET!", Toast.LENGTH_SHORT).show()
        }
        preferences_button.setOnClickListener {
            Toast.makeText(applicationContext, "NOT IMPLEMENTED YET!", Toast.LENGTH_SHORT).show()
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
                floating_action_menu.close(true)
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
            this.onSelectedAircraftChange(it)
        })
        this.aircraftInfoViewModel.track.observe(this@MapActivity, {
            this.path.setPoints(it)
        })
        this.aircraftInfoViewModel.state.observe(this@MapActivity, {
            this.onAircraftInfoStateChange(bs, it)
        })
        this.aircraftInfoViewModel.image.observe(this@MapActivity, {
            this.onAircraftImageChange(it)
        })
        this.aircraftInfoViewModel.error.observeEvent(this@MapActivity, {
            Toast.makeText(applicationContext, it, Toast.LENGTH_LONG).show()
        })
    }

    private fun observeAircraftList(map: MapView) {
        this.aircraftListViewModel.aircraftList.observe(this@MapActivity, {
            this.onAircraftListChange(map, it)
        })
        this.aircraftListViewModel.event.observeEvent(this@MapActivity, {
            this.refreshAircraftList(map.boundingBox)
        })
        this.aircraftListViewModel.error.observeEvent(this@MapActivity, {
            if (it is AuthenticationException) {
                this.userViewModel.performLogout(this@MapActivity)
                Toast.makeText(applicationContext, R.string.you_have_been_logged_out, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun observeUser() {
        this.userViewModel.userLoggedIn.observeEvent(this@MapActivity, {
            if (!it) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun refreshAircraftList(bb: BoundingBox) {
        this@MapActivity.aircraftListViewModel.refreshAircraftList(
            bb.latNorth,
            bb.latSouth,
            bb.lonWest,
            bb.lonEast
        )
    }

    private fun onSelectedAircraftChange(aircraft: Aircraft?) {
        if (aircraft != null) {
            map.controller.animateTo(aircraft.position)
            callsign.text = aircraft.callsign
            operator.text = aircraft.operator
            from.text = aircraft.from?.firstChars(3) ?: "N/A"
            to.text = aircraft.to?.firstChars(3) ?: "N/A"
            ac_type.text = aircraft.manufacturer?.concatenate(aircraft.type, " ") ?: "N/A"
            registration.text = aircraft.registration ?: "Reg. N/A"
            modelTV.text = aircraft.model ?: getString(R.string.unknown_aircraft)
            icaoTV.text = aircraft.icao ?: "N/A"
            plane_image.setImageDrawable(applicationContext.getDrawableByName("image_placeholder"))
            plane_image.isClickable = false
            this.aircraftInfoViewModel.state.value = AircraftInfoViewModel.State.PEEKING
            this.aircraftInfoViewModel.track.value = aircraft.trackPoints.plus(aircraft.position!!)
            this.aircraftInfoViewModel.getImage(aircraft.icao)
        } else {
            this.aircraftInfoViewModel.state.value = AircraftInfoViewModel.State.HIDDEN
            this.aircraftInfoViewModel.track.value = listOf()
        }
    }

    private fun onAircraftInfoStateChange(bs: BottomSheetBehavior<LinearLayout>, state: AircraftInfoViewModel.State) {
        when (state) {
            AircraftInfoViewModel.State.PEEKING -> {
                bs.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            AircraftInfoViewModel.State.OPEN -> {
                bs.state = BottomSheetBehavior.STATE_EXPANDED
            }
            AircraftInfoViewModel.State.HIDDEN -> {
                bs.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun onAircraftImageChange(image: Image?) {
        if (image != null) {
            Picasso.get().load(image.imageUrl).into(plane_image)
            plane_image.isClickable = true
            plane_image.setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse(image.pageUrl)
                startActivity(intent)
            }
        }
    }

    private fun onAircraftListChange(map: MapView, aircraftList: AircraftList) {
        val updatedAircrafts = aircraftList.aircrafts
        val currentAircraftsMap: Map<Number, AircraftMarker> =
            this@MapActivity.markersOverlay.items.associateBy {
                (it as AircraftMarker).getAircraftId()
            } as Map<Number, AircraftMarker>

        this.updateAircraftMarkers(updatedAircrafts, currentAircraftsMap)
        this.deleteOldMarkers(updatedAircrafts, currentAircraftsMap)
        this.updateSelectedAircraftInfo(updatedAircrafts)
        map.invalidate()
    }

    private fun updateAircraftMarkers(new: MutableMap<Number, Aircraft>, current: Map<Number, AircraftMarker>) {
        new.forEach {
            if (it.value.willShowOnMap()) {
                if (current.containsKey(it.value.id)) {
                    val marker = current[it.key] ?: error("Marker not found!")
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
    }

    private fun deleteOldMarkers(new: MutableMap<Number, Aircraft>, current: Map<Number, AircraftMarker>) {
        val toDelete = current.keys.minus(new.keys)
        toDelete.forEach {
            if (it == this@MapActivity.aircraftInfoViewModel.selectedAircraft.value?.id) {
                return@forEach
            }
            val marker = current[it]
            marker?.closeInfoWindow()
            markersOverlay.items.remove(marker)
        }
    }

    private fun updateSelectedAircraftInfo(new: MutableMap<Number, Aircraft>) {
        val selectedAircraftId = this@MapActivity.aircraftInfoViewModel.selectedAircraft.value?.id ?: return
        val upToDateSelectedAircraft = new[selectedAircraftId] ?: return
        map.controller.animateTo(upToDateSelectedAircraft.position)
        altTV.text = if (upToDateSelectedAircraft.onGround == true) "GND" else upToDateSelectedAircraft.amslAlt.toAltitude()
        speedTV.text = upToDateSelectedAircraft.spd.toSpeed()
        headingTV.text = upToDateSelectedAircraft.hdg.toHeading()
        squawkTV.text = upToDateSelectedAircraft.squawk ?: "N/A"
        this.aircraftInfoViewModel.track.value = upToDateSelectedAircraft.trackPoints
    }
}