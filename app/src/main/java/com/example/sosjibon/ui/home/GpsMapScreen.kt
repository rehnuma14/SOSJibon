package com.example.sosjibon.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private const val GEOAPIFY_API_KEY = ""
private const val GEOAPIFY_STYLE_URL = "https://maps.geoapify.com/v1/styles/osm-carto/style.json?apiKey=$GEOAPIFY_API_KEY"
private val DEFAULT_LOCATION = LatLng(22.3394, 91.8319)
private const val ROUTE_SOURCE_ID = "nearest-medical-route-source"
private const val ROUTE_LAYER_ID = "nearest-medical-route-layer"

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsMapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var savedLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var savedLongitude by rememberSaveable { mutableStateOf<Double?>(null) }

    val savedUserLocation = if (savedLatitude != null && savedLongitude != null) {
        LatLng(savedLatitude!!, savedLongitude!!)
    } else {
        null
    }

    var savedNearestName by rememberSaveable { mutableStateOf<String?>(null) }
    var savedNearestLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var savedNearestLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var savedNearestAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var savedNearestType by rememberSaveable { mutableStateOf<String?>(null) }
    var savedNearestDistance by rememberSaveable { mutableStateOf<Double?>(null) }
    var userLocation by remember { mutableStateOf(savedUserLocation) }

    remember {
        MapLibre.getInstance(context.applicationContext)
        true
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    var medicalMarkers by remember { mutableStateOf<List<Marker>>(emptyList()) }
    var nearestMedicalMarker by remember { mutableStateOf<Marker?>(null) }
    var findingNearest by remember { mutableStateOf(false) }
    var medicalLoaded by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!hasLocationPermission) {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val newLocation = LatLng(location.latitude, location.longitude)
                userLocation = newLocation
                savedLatitude = location.latitude
                savedLongitude = location.longitude
            }
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            try {
                val lastGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastGpsLocation != null) {
                    val newLocation = LatLng(lastGpsLocation.latitude, lastGpsLocation.longitude)
                    userLocation = newLocation
                    savedLatitude = lastGpsLocation.latitude
                    savedLongitude = lastGpsLocation.longitude
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        3000L,
                        5f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }

                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000L,
                        5f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Unable to access GPS", Toast.LENGTH_LONG).show()
            }

            onDispose {
                try {
                    locationManager.removeUpdates(locationListener)
                } catch (_: Exception) { }
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(userLocation, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val location = userLocation ?: return@LaunchedEffect

        userMarker?.let { map.removeMarker(it) }

        val icon = IconFactory.getInstance(context).fromBitmap(
            createCircleBitmap(context, Color.rgb(0, 100, 255), "●")
        )

        userMarker = map.addMarker(
            MarkerOptions()
                .position(location)
                .title("You are here")
                .icon(icon)
        )
    }

    LaunchedEffect(mapLibreMap) {
        val location = userLocation ?: return@LaunchedEffect
        val map = mapLibreMap ?: return@LaunchedEffect
        if (medicalLoaded) return@LaunchedEffect
        medicalLoaded = true
        coroutineScope.launch {
            loadMedicalPlaces(
                context = context,
                map = map,
                latitude = location.latitude,
                longitude = location.longitude
            ) { markers ->
                medicalMarkers = markers
            }
        }
    }

    LaunchedEffect(mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val latitude = savedNearestLatitude ?: return@LaunchedEffect
        val longitude = savedNearestLongitude ?: return@LaunchedEffect

        if (nearestMedicalMarker == null) {
            nearestMedicalMarker?.let { map.removeMarker(it) }
            val destination = LatLng(latitude, longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(destination)
                    .title(savedNearestName ?: "Medical Facility")
                    .snippet(savedNearestAddress ?: "Address unavailable")
                    .icon(createMedicalIcon(context, savedNearestType ?: "clinic"))
            )
            nearestMedicalMarker = marker
        }

        val userLat = savedLatitude ?: return@LaunchedEffect
        val userLon = savedLongitude ?: return@LaunchedEffect

        coroutineScope.launch {
            val routeJson = getRoadRoute(
                fromLatitude = userLat,
                fromLongitude = userLon,
                toLatitude = latitude,
                toLongitude = longitude
            )

            if (routeJson != null) {
                mapLibreMap?.let { currentMap ->
                    val style = currentMap.style ?: return@let
                    val routeSource = style.getSource(ROUTE_SOURCE_ID) as? GeoJsonSource
                    routeSource?.setGeoJson(routeJson)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    mapView.apply {
                        getMapAsync { map ->
                            mapLibreMap = map
                            map.setStyle(GEOAPIFY_STYLE_URL) {
                                val cameraLocation = userLocation ?: savedUserLocation ?: DEFAULT_LOCATION

                                map.cameraPosition = CameraPosition.Builder().target(cameraLocation).zoom(
                                    if (savedUserLocation != null) 15.0 else 14.0
                                ).build()

                                val style = map.style
                                if (style != null && style.getSource(ROUTE_SOURCE_ID) == null) {
                                    val routeSource = GeoJsonSource(ROUTE_SOURCE_ID)
                                    style.addSource(routeSource)

                                    val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
                                    routeLayer.setProperties(
                                        PropertyFactory.lineColor(Color.rgb(220, 40, 50)),
                                        PropertyFactory.lineWidth(6f),
                                        PropertyFactory.lineOpacity(0.9f)
                                    )
                                    style.addLayer(routeLayer)
                                }
                            }
                        }
                    }
                }
            )

            Button(
                onClick = {
                    val location = userLocation ?: savedUserLocation
                    if (location == null) {
                        Toast.makeText(context, "Waiting for your GPS location...", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (findingNearest) return@Button
                    findingNearest = true

                    coroutineScope.launch {
                        val nearest = findNearestMedicalPlace(latitude = location.latitude, longitude = location.longitude)
                        if (nearest == null) {
                            findingNearest = false
                            Toast.makeText(context, "No hospital or clinic found nearby", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        savedNearestName = nearest.name
                        savedNearestLatitude = nearest.latitude
                        savedNearestLongitude = nearest.longitude
                        savedNearestAddress = nearest.address
                        savedNearestType = nearest.type
                        savedNearestDistance = nearest.distanceMeters

                        mapLibreMap?.let { map ->
                            nearestMedicalMarker?.let { map.removeMarker(it) }
                            val destination = LatLng(nearest.latitude, nearest.longitude)
                            nearestMedicalMarker = map.addMarker(
                                MarkerOptions()
                                    .position(destination)
                                    .title(nearest.name)
                                    .snippet(nearest.address)
                                    .icon(createMedicalIcon(context, nearest.type))
                            )
                        }

                        val routeJson = getRoadRoute(
                            fromLatitude = location.latitude,
                            fromLongitude = location.longitude,
                            toLatitude = nearest.latitude,
                            toLongitude = nearest.longitude
                        )

                        if (routeJson == null) {
                            findingNearest = false
                            Toast.makeText(context, "Could not calculate road route", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        mapLibreMap?.let { map ->
                            val style = map.style
                            if (style != null) {
                                val routeSource = style.getSource(ROUTE_SOURCE_ID) as? GeoJsonSource
                                routeSource?.setGeoJson(routeJson)
                                val bounds = getRouteBounds(routeJson)
                                if (bounds != null) {
                                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120), 1200)
                                }
                            }
                        }

                        val distanceText = nearest.distanceMeters?.let {
                            String.format(Locale.US, "%.1f km", it / 1000.0)
                        } ?: "Nearby"

                        Toast.makeText(context, "Route to ${nearest.name} • $distanceText", Toast.LENGTH_LONG).show()
                        findingNearest = false
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
            ) {
                if (findingNearest) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    Text("Finding...")
                } else {
                    Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Nearest Medical")
                }
            }

            FloatingActionButton(
                onClick = {
                    val location = userLocation ?: savedUserLocation
                    if (location != null) {
                        mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0), 1000)
                    } else {
                        Toast.makeText(context, "Waiting for GPS location...", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 90.dp)
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    }
}

private data class MedicalPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val distanceMeters: Double?,
    val type: String
)

private suspend fun loadMedicalPlaces(
    context: Context,
    map: MapLibreMap,
    latitude: Double,
    longitude: Double,
    onLoaded: (List<Marker>) -> Unit
) {
    try {
        val url = "https://api.geoapify.com/v2/places?categories=healthcare.hospital,healthcare.clinic_or_praxis&filter=circle:$longitude,$latitude,5000&bias=proximity:$longitude,$latitude&limit=30&apiKey=$GEOAPIFY_API_KEY"
        val json = withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            try {
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }

        val root = JSONObject(json)
        val features = root.optJSONArray("features") ?: return
        val newMarkers = mutableListOf<Marker>()

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val properties = feature.optJSONObject("properties") ?: continue
            val geometry = feature.optJSONObject("geometry") ?: continue
            val coordinates = geometry.optJSONArray("coordinates") ?: continue

            if (coordinates.length() < 2) continue

            val lon = coordinates.optDouble(0, Double.NaN)
            val lat = coordinates.optDouble(1, Double.NaN)

            if (lon.isNaN() || lat.isNaN()) continue

            val name = properties.optString("name", "Medical Facility")
            val address = properties.optString("formatted", "Address unavailable")
            val categories = properties.optString("categories", "")
            val type = if (categories.contains("hospital", ignoreCase = true)) "hospital" else "clinic"

            val icon = if (type == "hospital") createHospitalIcon(context) else createClinicIcon(context)
            val marker = map.addMarker(MarkerOptions().position(LatLng(lat, lon)).title(name).snippet(address).icon(icon))
            newMarkers.add(marker)
        }
        onLoaded(newMarkers)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private suspend fun findNearestMedicalPlace(latitude: Double, longitude: Double): MedicalPlace? {
    return try {
        val url = "https://api.geoapify.com/v2/places?categories=healthcare.hospital,healthcare.clinic_or_praxis&filter=circle:$longitude,$latitude,5000&bias=proximity:$longitude,$latitude&limit=20&apiKey=$GEOAPIFY_API_KEY"
        val json = withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) return@withContext null
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        } ?: return null

        val root = JSONObject(json)
        val features = root.optJSONArray("features") ?: return null
        if (features.length() == 0) return null

        var nearest: MedicalPlace? = null
        var shortestDistance = Double.MAX_VALUE

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val properties = feature.optJSONObject("properties") ?: continue
            val geometry = feature.optJSONObject("geometry") ?: continue
            val coordinates = geometry.optJSONArray("coordinates") ?: continue

            if (coordinates.length() < 2) continue

            val lon = coordinates.optDouble(0, Double.NaN)
            val lat = coordinates.optDouble(1, Double.NaN)

            if (lon.isNaN() || lat.isNaN()) continue

            val name = properties.optString("name", "Medical Facility")
            val address = properties.optString("formatted", "Address unavailable")
            val distance = if (properties.has("distance")) {
                properties.optDouble("distance", Double.NaN)
            } else {
                calculateDistance(latitude, longitude, lat, lon).toDouble()
            }

            if (distance.isNaN()) continue

            val categories = properties.optString("categories", "")
            val type = if (categories.contains("hospital", ignoreCase = true)) "hospital" else "clinic"

            if (distance < shortestDistance) {
                shortestDistance = distance
                nearest = MedicalPlace(name = name, latitude = lat, longitude = lon, address = address, distanceMeters = distance, type = type)
            }
        }

        nearest
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun getRoadRoute(fromLatitude: Double, fromLongitude: Double, toLatitude: Double, toLongitude: Double): String? {
    return try {
        val waypoints = "$fromLatitude,$fromLongitude|$toLatitude,$toLongitude"
        val url = "https://api.geoapify.com/v1/routing?waypoints=$waypoints&mode=drive&format=geojson&apiKey=$GEOAPIFY_API_KEY"

        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) return@withContext null
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getRouteBounds(routeJson: String): LatLngBounds? {
    return try {
        val root = JSONObject(routeJson)
        val features = root.optJSONArray("features") ?: return null
        val builder = LatLngBounds.Builder()
        var foundPoint = false

        for (featureIndex in 0 until features.length()) {
            val feature = features.getJSONObject(featureIndex)
            val geometry = feature.optJSONObject("geometry") ?: continue
            val coordinates = geometry.optJSONArray("coordinates") ?: continue
            val geometryType = geometry.optString("type")

            when (geometryType) {
                "MultiLineString" -> {
                    for (i in 0 until coordinates.length()) {
                        val line = coordinates.getJSONArray(i)
                        for (j in 0 until line.length()) {
                            val point = line.getJSONArray(j)
                            if (point.length() < 2) continue
                            val lon = point.getDouble(0)
                            val lat = point.getDouble(1)
                            builder.include(LatLng(lat, lon))
                            foundPoint = true
                        }
                    }
                }
                "LineString" -> {
                    for (i in 0 until coordinates.length()) {
                        val point = coordinates.getJSONArray(i)
                        if (point.length() < 2) continue
                        val lon = point.getDouble(0)
                        val lat = point.getDouble(1)
                        builder.include(LatLng(lat, lon))
                        foundPoint = true
                    }
                }
            }
        }

        if (!foundPoint) return null
        builder.build()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val result = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, result)
    return result[0]
}

private fun createHospitalIcon(context: Context) = IconFactory.getInstance(context).fromBitmap(
    createCircleBitmap(context, Color.rgb(220, 30, 30), "H")
)

private fun createClinicIcon(context: Context) = IconFactory.getInstance(context).fromBitmap(
    createCircleBitmap(context, Color.rgb(30, 120, 220), "C")
)

private fun createMedicalIcon(context: Context, type: String) =
    if (type.equals("hospital", ignoreCase = true)) {
        createHospitalIcon(context)
    } else {
        createClinicIcon(context)
    }

private fun createCircleBitmap(context: Context, color: Int, text: String): Bitmap {
    val size = 100
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = color
    canvas.drawCircle(50f, 50f, 40f, paint)

    paint.color = Color.WHITE
    paint.textSize = 38f
    paint.textAlign = Paint.Align.CENTER
    paint.isFakeBoldText = true

    canvas.drawText(text, 50f, 63f, paint)
    return bitmap
}
