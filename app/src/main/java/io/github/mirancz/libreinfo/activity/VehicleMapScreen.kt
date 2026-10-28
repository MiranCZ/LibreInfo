package io.github.mirancz.libreinfo.activity

import android.content.Context
import androidx.compose.animation.EnterExitState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.Location
import io.github.mirancz.libreinfo.parsing.types.Vehicle
import io.github.mirancz.libreinfo.parsing.types.dto.VehicleDTO
import io.github.mirancz.libreinfo.parsing.types.response.RoutePathResponse
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.LineIcon
import io.github.mirancz.libreinfo.ui.components.ShimmerBox
import io.github.mirancz.libreinfo.ui.components.rememberActivityShimmer
import io.github.mirancz.libreinfo.ui.theme.extendedColors
import io.github.mirancz.libreinfo.util.DelayUtil
import io.github.mirancz.libreinfo.util.json
import io.github.mirancz.libreinfo.util.request.RequestHelper
import io.github.mirancz.libreinfo.util.request.VehicleWebsocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToColor
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.exponential
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.interpolate
import org.maplibre.compose.expressions.dsl.rem
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.dsl.times
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.IconRotationAlignment
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.map.RenderOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson
import kotlin.runCatching
import kotlin.time.Duration.Companion.milliseconds

private const val STYLE_URL = "https://api.maptiler.com/maps/basic-v2/style.json?key="

// Brno city center
private val DEFAULT_TARGET = Position(longitude = 16.613163, latitude = 49.191748)


@Serializable
public data class SelectedRoute(
    val vehicleId: Int,
    val serviceId: Int,
    val lineId: Int,
    val routeId: Int,
    val color: String,
    val lat: Double?,
    val lng: Double?
)

@Composable
fun VehicleMapScreen(state: NavState, vehicle: SelectedRoute?) {
    val context = LocalContext.current

    val transition = LocalNavAnimatedContentScope.current.transition
    val showMap =
        transition.currentState == EnterExitState.Visible && transition.targetState == EnterExitState.Visible

    var selected by remember { mutableStateOf(vehicle) }
    // set once the first tiles are drawn, until then the map only shows the style's background.
    // Keyed on showMap since every time the map is shown, a new map instance is created
    var mapLoaded by remember(showMap) { mutableStateOf(false) }
    val posts by loadPosts()
    val routePath by getRoutePathState(selected, context)

    ScreenScaffold(stringResource(R.string.vehicle_map), onBack = state.onBack) {
        Box(Modifier.fillMaxSize()) {
            if (showMap) {
                VehicleMapContent(
                    state, posts, routePath, selected, mapLoaded,
                    onMapLoaded = { mapLoaded = true },
                    onSelectedChange = { selected = it },
                )
            }

            if (!mapLoaded) MapPlaceholder()
        }
    }
}

@Composable
private fun MapPlaceholder() {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ShimmerBox(Modifier.fillMaxSize(), rememberActivityShimmer(), RectangleShape)
    }
}

@Composable
private fun VehicleMapContent(
    state: NavState,
    posts: String?,
    routePath: FeatureCollection<LineString, JsonObject>,
    selected: SelectedRoute?,
    mapLoaded: Boolean,
    onMapLoaded: () -> Unit,
    onSelectedChange: (SelectedRoute?) -> Unit,
) {
    val context = LocalContext.current

    val cameraState = getCameraStateFrom(selected)
    val scope = rememberCoroutineScope()

    val currentSelected by rememberUpdatedState(selected)

    val vehicles = remember { mutableStateMapOf<Int, Vehicle>() }
    val selectedVehicle = selected?.let { vehicles[it.vehicleId] }

    // onMapClick runs before layer click handlers, so this holds the offset of the current click
    var lastClickOffset by remember { mutableStateOf(DpOffset.Zero) }
    var picker by remember { mutableStateOf<VehiclePicker?>(null) }

    fun select(v: Vehicle) {
        onSelectedChange(v.toSelectedRoute() ?: return)
        val lat = v.latitude ?: return
        val lng = v.longitude ?: return

        scope.launch {
            cameraState.animateTo(
                CameraPosition(target = Position(lng, lat), zoom = 16.0)
            )
        }
    }


    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val storage = AppContainer.storageProvider.getInstance()
            runCatching {
                RequestHelper.getVehicles(
                    context, false
                ).vehicles.map { it.map(storage) }
            }.onSuccess { list ->
                vehicles.putAll(list.associateBy { it.id })

                // update camera if vehicle moved from screen open; nothing else can be selected yet
                // since the vehicle layer is empty until this fetch completes
                val update = currentSelected?.let { vehicles[it.vehicleId] } ?: return@onSuccess
                val lat = update.latitude ?: return@onSuccess
                val lng = update.longitude ?: return@onSuccess

                scope.launch {
                    cameraState.animateTo(CameraPosition(target = Position(lng, lat), zoom = 16.0))
                }
            }

            VehicleWebsocket.subscribe(MainActivity::class.java) { message ->
                val v = json.decodeFromString(VehicleDTO.serializer(), message).map(storage)
                vehicles[v.id] = v
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { VehicleWebsocket.unsubscribe(MainActivity::class.java) }
    }

    val vehicleData by produceState(FeatureCollection()) {
        snapshotFlow { vehicles.isEmpty() }.first { !it }
        while (true) {
            value = FeatureCollection(vehicles.values.mapNotNull { it.toFeature() })
            delay(2_000.milliseconds)
        }
    }

    VehicleSheetScaffold(selectedVehicle) {
        Box(Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(STYLE_URL + BuildConfig.MAPTILER_API_KEY),
                cameraState = cameraState,
                options = MapOptions(
                    ornamentOptions = OrnamentOptions(
                        isCompassEnabled = false, isScaleBarEnabled = false
                    ), gestureOptions = GestureOptions(isTiltEnabled = false),

                    // this should maybe make the map smoother, TODO test
                    renderOptions = RenderOptions(renderMode = RenderOptions.RenderMode.TextureView)
                ),
                onMapClick = { _, offset ->
                    lastClickOffset = offset
                    val hit =
                        cameraState.projection?.queryRenderedFeatures(offset, setOf("vehicles"))
                            .orEmpty()

                    if (hit.isEmpty()) onSelectedChange(null)
                    ClickResult.Pass
                },
                onMapLoadFinished = onMapLoaded,
                onMapLoadFailed = { onMapLoaded() },
            ) {
                RoutePathLayer(routePath)

                // posts wait for the base map, otherwise their GeoJSON processing delays its first tiles
                if (mapLoaded) PostsLayer(posts)

                VehicleLayer(selected, vehicleData, vehicles, {
                    picker = VehiclePicker(lastClickOffset, it)
                }, { select(it) })
            }

            picker?.let { p ->
                Box(Modifier.offset(p.offset.x, p.offset.y)) {
                    PickerDropDown(p, vehicles, { picker = null }) {
                        picker = null
                        select(it)
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun VehicleSheetScaffold(
    selectedVehicle: Vehicle?,
    content: @Composable (PaddingValues) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(skipHiddenState = true)
    )
    val density = LocalDensity.current
    var sheetHeight by remember { mutableStateOf(0.dp) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (selectedVehicle != null) sheetHeight else 0.dp,
        sheetDragHandle = null,
        sheetContent = {
            selectedVehicle?.let {
                VehicleSheet(
                    it, Modifier.onSizeChanged { size ->
                        sheetHeight = with(density) { size.height.toDp() }
                    })
            }
        },
        content = content
    )
}

@Composable
private fun getRoutePathState(
    selected: SelectedRoute?,
    context: Context
): State<FeatureCollection<LineString, JsonObject>> = produceState(FeatureCollection(), selected) {
    val sel = selected ?: run { value = FeatureCollection(); return@produceState }
    value = withContext(Dispatchers.IO) {
        runCatching {
            RequestHelper.getRoutePath(
                context, sel.serviceId, sel.lineId, sel.routeId
            )
        }.getOrNull()?.toFeatureCollection(sel.color) ?: FeatureCollection()
    }
}

@Composable
private fun VehicleLayer(
    selected: SelectedRoute?,
    vehicleData: FeatureCollection<Point, JsonObject>,
    vehicles: SnapshotStateMap<Int, Vehicle>,
    onShowPicker: (List<Int>) -> Unit,
    onSelect: (Vehicle) -> Unit
) {
    val isSelected = feature["id"].asNumber() eq const(selected?.vehicleId?.toFloat() ?: -1f)
    val selScale = switch(condition(isSelected, const(1.2f)), fallback = const(1f))

    val vehicleSource = rememberGeoJsonSource(GeoJsonData.Features(vehicleData))
    val busIcon = image(
        painterResource(R.drawable.vehicle_arrow), DpSize(32.dp, 32.dp), drawAsSdf = true
    )

    SymbolLayer(
        id = "vehicles",
        source = vehicleSource,
        iconImage = busIcon,
        iconSize = interpolate(
            exponential(1.2f), zoom(),
            11 to const(0.6f) * selScale,
            15 to const(1.25f) * selScale,
        ),
        iconColor = feature["color"].convertToColor(),
        iconRotate = feature["bearing"].asNumber(),
        iconRotationAlignment = const(IconRotationAlignment.Map),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        textField = feature["name"].asString(),
        textColor = feature["textColor"].convertToColor(),
        textSize = interpolate(
            exponential(1.2f), zoom(),
            9.9f to const(0.sp) * selScale,
            10 to const(7.sp) * selScale,
            11 to const(7.sp) * selScale,
            15 to const(11.sp) * selScale,
        ),
        textAllowOverlap = const(true),
        textOptional = const(true),
        sortKey = switch(
            condition(isSelected, const(1_000_000f)),
            fallback = feature["id"].asNumber() % const(100),
        ),
        onClick = { features ->
            // features are sorted front-to-back, may contain duplicates across tiles
            val ids =
                features.mapNotNull { it.properties?.get("id")?.jsonPrimitive?.int }.distinct()

            if (ids.size > 1) {
                onShowPicker(ids)
            } else {
                ids.firstOrNull()?.let { vehicles[it] }?.let(onSelect)
            }
            ClickResult.Consume
        })
}

@Composable
private fun RoutePathLayer(routePath: FeatureCollection<LineString, JsonObject>) {
    val routeSource = rememberGeoJsonSource(GeoJsonData.Features(routePath))
    LineLayer(
        id = "route",
        source = routeSource,
        color = feature["color"].convertToColor(),
        width = const(4.dp),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
}

@Composable
private fun PostsLayer(posts: String?) {
    posts?.let { data ->
        val source = rememberGeoJsonSource(GeoJsonData.JsonString(data))

        SymbolLayer(
            id = "stops",
            source = source,
            minZoom = 13f,
            iconImage = image(painterResource(R.drawable.stop), DpSize(24.dp, 24.dp)),
            iconAnchor = const(SymbolAnchor.Bottom),
            iconAllowOverlap = const(false),
            onClick = { features ->
                // TODO
                ClickResult.Consume
            },
        )
    }
}

@Composable
private fun getCameraStateFrom(vehicle: SelectedRoute?): CameraState {
    val lat = vehicle?.lat
    val lng = vehicle?.lng

    val cameraState = rememberCameraState(
        firstPosition = if (vehicle == null || lat == null || lng == null) {
            CameraPosition(target = DEFAULT_TARGET, zoom = 15.0)
        } else {
            CameraPosition(target = Position(latitude = lat, longitude = lng), zoom = 15.0)
        }
    )
    return cameraState
}

// Serialized here because GeoJsonData.Features runs toJson() on the main thread during composition,
// which takes over 100ms for all posts.
@Composable
private fun loadPosts(): State<String?> = produceState(null) {
    value = withContext(Dispatchers.IO) {
        val storage = AppContainer.storageProvider.getInstance()   // suspend
        FeatureCollection(storage.postStorage().allPosts.filter { it.location != Location.NONE }
            .map { post ->
                Feature(
                    geometry = Point(
                        Position(
                            post.location.longitude, post.location.latitude
                        )
                    ),
                    properties = buildJsonObject {
                        put("postId", post.postID)
                        put("name", post.name)
                    },
                )
            }).toJson()
    }
}

@Composable
private fun PickerDropDown(
    p: VehiclePicker,
    vehicles: SnapshotStateMap<Int, Vehicle>,
    onDismiss: () -> Unit,
    onClick: (Vehicle) -> Unit
) {
    DropdownMenu(expanded = true, onDismissRequest = { onDismiss() }) {
        p.ids.mapNotNull { vehicles[it] }.forEach { v ->
            DropdownMenuItem(
                leadingIcon = { LineIcon(line = v.line, padding = 0.dp) },
                text = {
                    Text(
                        v.getFinalStopText(), maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                },
                onClick = {
                    onClick(v)
                },
            )
        }
    }
}

private data class VehiclePicker(val offset: DpOffset, val ids: List<Int>)

private fun Vehicle.toSelectedRoute(): SelectedRoute? = SelectedRoute(
    vehicleId = id,
    serviceId = serviceId ?: return null,
    lineId = line.id,
    routeId = routeId,
    color = line.backgroundColorStr(),
    lat = latitude,
    lng = longitude,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleSheet(item: Vehicle, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier.fillMaxWidth()) {
        BottomSheetDefaults.DragHandle(Modifier.align(Alignment.CenterHorizontally))
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
            ) {
                Row(Modifier.weight(1f)) {
                    LineIcon(line = item.line, padding = 0.dp)
                    Text(
                        item.getFinalStopText(),
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 12.dp)
                    )
                }

                if (item.lowFloor == true) {
                    Icon(
                        painter = painterResource(R.drawable.wheelchair_regular),
                        "lowfloor",
                        Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        tint = MaterialTheme.extendedColors.onSurfaceMedium
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    Icon(
                        painter = painterResource(R.drawable.stop),
                        null,
                        Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        item.lastStop.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }


                item.delay?.let { delay ->
                    Text(
                        DelayUtil.getDelayText(context, delay),
                        fontSize = 14.sp,
                        color = Color(DelayUtil.getDelayColor(delay))
                    )
                }
            }

        }
    }
}


private fun Vehicle.toFeature(): Feature<Point, JsonObject>? {
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return Feature(
        Point(Position(lon, lat)),
        buildJsonObject {
            put("id", id)
            put("serviceId", serviceId)
            put("lineId", line.id)
            put("routeId", routeId)
            put("name", line.lineDisplayName())
            put("color", line.backgroundColorStr())
            put("textColor", line.textColorStr())
            put("bearing", bearing ?: 0.0)
        },
    )
}

private fun RoutePathResponse.toFeatureCollection(color: String): FeatureCollection<LineString, JsonObject> {
    if (points.size < 2) return FeatureCollection()
    return FeatureCollection(
        Feature(
            LineString(points.map { (lat, lon) -> Position(longitude = lon, latitude = lat) }),
            buildJsonObject { put("color", color) },
        )
    )
}
