package io.github.mirancz.libreinfo.activity;

import static org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap;
import static org.maplibre.android.style.layers.PropertyFactory.iconColor;
import static org.maplibre.android.style.layers.PropertyFactory.iconImage;
import static org.maplibre.android.style.layers.PropertyFactory.iconRotate;
import static org.maplibre.android.style.layers.PropertyFactory.iconSize;
import static org.maplibre.android.style.layers.PropertyFactory.symbolSortKey;
import static org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap;
import static org.maplibre.android.style.layers.PropertyFactory.textColor;
import static org.maplibre.android.style.layers.PropertyFactory.textField;
import static org.maplibre.android.style.layers.PropertyFactory.textOptional;
import static org.maplibre.android.style.layers.PropertyFactory.textSize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import io.github.mirancz.libreinfo.BuildConfig;
import io.github.mirancz.libreinfo.R;
import io.github.mirancz.libreinfo.activity.base.BaseActivity;
import io.github.mirancz.libreinfo.activity.bottomsheet.VehicleInfoBottomSheet;
import io.github.mirancz.libreinfo.exception.AppException;
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer;
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage;
import io.github.mirancz.libreinfo.parsing.types.Post;
import io.github.mirancz.libreinfo.parsing.types.Vehicle;
import io.github.mirancz.libreinfo.parsing.types.dto.VehicleDTO;
import io.github.mirancz.libreinfo.util.AppJsonKt;
import io.github.mirancz.libreinfo.util.AppLog;
import io.github.mirancz.libreinfo.util.request.RequestHelper;
import io.github.mirancz.libreinfo.util.request.VehicleWebsocket;
import com.google.android.material.bottomsheet.BottomSheetBehavior;


import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.plugins.annotation.ClusterOptions;
import org.maplibre.android.plugins.annotation.SymbolManager;
import org.maplibre.android.plugins.annotation.SymbolOptions;
import org.maplibre.android.style.expressions.Expression;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonOptions;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

// FIXME the text rendering is still not the best ugh
// TODO refactor, cleanup
public class VehicleMapActivity extends BaseActivity {

    private final Map<Integer, Feature> vehicleFeatureMap = new ConcurrentHashMap<>();
    private MapView mapView;
    private Timer timer;
    private SelectedContext selectedContext = new SelectedContext();

    private static final String ROUTE_SOURCE_ID = "route-source";
    private static final String ROUTE_LAYER_ID = "route-layer";
    private static final String VEHICLE_LAYER_ID = "text-layer";
    GeoJsonSource routeSource;
    LineLayer routeLayer;
    IdStorage storage;
    CustomSymbolManager stopLayer;

    public VehicleMapActivity() {
        super(R.string.vehicle_map);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);
        setContentView(R.layout.activity_vehicle_map);

        View bottomSheet = findViewById(R.id.bottom_sheet_container);

        mapView = findViewById(R.id.vehicle_map);
        mapView.onCreate(savedInstanceState);

        selectedContext.setBehavior(BottomSheetBehavior.from(bottomSheet), this);

        selectedContext.setSelected(getIntent().getIntExtra("following", -1));


        new Thread(() -> {
            storage = AppContainer.INSTANCE.getStorageProvider().getBlocking(IdStorage.class);

            runOnUiThread(() -> setupMap(storage, this::startVehicleUpdates));
        }).start();
    }

    private void startVehicleUpdates(GeoJsonSource source, MapLibreMap map) {
        new Thread(() -> {
            try {
                List<Vehicle> vehicles = RequestHelper.getVehicles(this).getVehicles().stream()
                        .map(dto -> dto.map(storage))
                        .collect(Collectors.toList());

                updateGeoJson(source, map, vehicles);
            } catch (AppException e) {
                // not fatal, the socket fills the map in as vehicles report their positions
                AppLog.e("Failed to load vehicle snapshot", e);
            }

            VehicleWebsocket.subscribe(VehicleMapActivity.class, message -> {
                Vehicle vehicle = AppJsonKt.json
                        .decodeFromString(VehicleDTO.Companion.serializer(), message)
                        .map(storage);

                updateGeoJson(source, map, vehicle);
            });
        }).start();
    }

    private void setupMap(IdStorage storage, BiConsumer<GeoJsonSource, MapLibreMap> onReady) {
        Context context = this;
        mapView.getMapAsync(map -> {
            map.getUiSettings().setRotateGesturesEnabled(false);
            map.getUiSettings().setCompassEnabled(false);

            var stopBitmap = toBitmap(context, R.drawable.stop, 64);
            var busBitmap = toBitmap(context, R.drawable.vehicle_arrow, 96);

            map.setStyle("https://api.maptiler.com/maps/basic-v2/style.json?key="+ BuildConfig.MAPTILER_API_KEY);

            double lat = getIntent().getDoubleExtra("lat", -1);
            double lng = getIntent().getDoubleExtra("lng", -1);


            GeoJsonSource source = new GeoJsonSource("points-source", new GeoJsonOptions());

            LatLng camPos;
            if (lat != -1 && lng != -1) {
                camPos = new LatLng(lat, lng);
            } else {
                camPos = new LatLng(49.191748, 16.613163);
            }
            map.setCameraPosition(new CameraPosition.Builder().target(camPos).zoom(15).build());

            findViewById(R.id.loading_spinner).setVisibility(View.GONE);

            map.getStyle(style -> {
                style.addImage("bus_icon", busBitmap, true);
                style.addImage("stop_icon", stopBitmap);

                routeSource = new GeoJsonSource(ROUTE_SOURCE_ID);


                routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);
                routeLayer.setProperties(
                        PropertyFactory.lineWidth(4f)
                );
                style.addSource(routeSource);
                style.addLayer(routeLayer);

                style.addSource(source);

                stopLayer = new CustomSymbolManager(mapView,map, style);
                stopLayer.getLayer().setMinZoom(13f);
                stopLayer.setIconAllowOverlap(false);

                SymbolLayer text = new SymbolLayer(VEHICLE_LAYER_ID, "points-source");
                text.setProperties(
                        textSize(11f),
                        textAllowOverlap(true),
                        textOptional(true),
                        textField(Expression.get("name")),
                        textColor(Expression.get("textColor")),
                        iconImage("bus_icon"),
                        iconColor(Expression.get("color")),
                        iconSize(1.25f),
                        iconRotate(Expression.get("bearing")),
                        iconAllowOverlap(true),
                        symbolSortKey(Expression.get("sort"))
                );

                style.addLayer(text);

                onReady.accept(source, map);

                map.addOnMapClickListener(point -> {
                    PointF screenPoint = map.getProjection().toScreenLocation(point);
                    List<Feature> features = map.queryRenderedFeatures(screenPoint, VEHICLE_LAYER_ID);
                    if (features.isEmpty()) {
                        selectedContext.setSelected(-1);
                        return false;
                    }

                    List<Vehicle> vehicles = features.stream()
                            .map(feature -> idToVehMap.get(feature.getNumberProperty("id").intValue()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    if (vehicles.isEmpty()) {
                        selectedContext.setSelected(-1);
                        return false;
                    }

                    if (vehicles.size() > 1) {
                        showVehicleSelectionDialog(vehicles);
                    } else {
                        selectedContext.setSelected(vehicles.get(0).getId());
                    }
                    return true;
                });

                SymbolOptions def = new SymbolOptions().withIconImage("stop_icon").withIconSize(1f).withIconAnchor("bottom");
                for (Post post : storage.postStorage().getAllPosts()) {
                    SymbolOptions options = def.withLatLng(post.location().toLatLng());
                    stopLayer.create(options);
                }

            });
        });
    }

    private void showVehicleSelectionDialog(List<Vehicle> vehicles) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_poi);

        String[] vehicleNames = vehicles.stream()
                .map(vehicle -> vehicle.getLine().lineDisplayName() + " -> " + vehicle.getFinalStopText())
                .toArray(String[]::new);

        builder.setItems(vehicleNames, (dialog, which) -> {
            selectedContext.setSelected(vehicles.get(which).getId());
        });

        builder.create().show();
    }

    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, List<Vehicle> vehicles) {
        updateGeoJson(source, map, vehicles.toArray(new Vehicle[0]));
    }

    // TODO add dynamic timer so that when a lot of elements is updated the map is redrawn prematurely
    HashMap<Integer, Integer> idMap = new HashMap<>();
    HashMap<Integer, Vehicle> idToVehMap = new HashMap<>();
    int id = 0;
    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, Vehicle... vehicles) {
        if (timer == null) setupCountdown(source);


        int id = 0;
        for (Vehicle vehicle : vehicles) {
            LatLng position = toLatLng(vehicle);
            if (position == null) continue;

            Feature feature = Feature.fromGeometry(Point.fromLngLat(position.getLongitude(), position.getLatitude()));
            feature.addStringProperty("color", vehicle.getLine().backgroundColorStr());
            feature.addStringProperty("textColor", vehicle.getLine().textColorStr());
            feature.addStringProperty("name", vehicle.getLine().lineDisplayName());
            feature.addNumberProperty("bearing", vehicle.getBearing() == null ? 0 : vehicle.getBearing());
            feature.addNumberProperty("id", vehicle.getId());

            if (!idMap.containsKey(vehicle.getId())) {
                idMap.put(vehicle.getId(), id++);
            }
            idToVehMap.put(vehicle.getId(), vehicle);
            // FIXME this is still not the best
            feature.addNumberProperty("sort", idMap.get(vehicle.getId()) % 100);

            vehicleFeatureMap.put(vehicle.getId(), feature);

            if (selectedContext.following && selectedContext.selected == vehicle.getId()) {
                runOnUiThread(() -> map.setCameraPosition(new CameraPosition.Builder().target(position).build()));
            }
            if (!selectedContext.fetchedLine && selectedContext.selected == vehicle.getId()) {
                selectedContext.fetchedLine = true;
                // TODO implement trip shapes
//                new Thread(() -> {
//                    runOnUiThread(() -> {
//                        routeSource.setGeoJson(pair.routesGeoJson);
//                        routeLayer.setProperties(
//                                PropertyFactory.lineColor(vehicle.getLine().backgroundColorStr())
//                        );
//                    });
//                }).start();

            }
        }
    }

    private void setupCountdown(GeoJsonSource source) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mapView == null) {
                    cancel();
                    return;
                }

                runOnUiThread(() -> source.setGeoJson(FeatureCollection.fromFeatures(vehicleFeatureMap.values().toArray(new Feature[0]))));
            }
        },0, 2_000);
    }

    @Nullable
    private static LatLng toLatLng(Vehicle vehicle) {
        if (vehicle.getLatitude() == null || vehicle.getLongitude() == null) return null;

        return new LatLng(vehicle.getLatitude(), vehicle.getLongitude());
    }

    @NonNull
    private static Bitmap toBitmap(Context context, int id, int size) {
        var drawable = AppCompatResources.getDrawable(context, id);
        var bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static class CustomSymbolManager extends SymbolManager {

        public CustomSymbolManager(@NonNull MapView mapView, @NonNull MapLibreMap maplibreMap, @NonNull Style style) {
            super(mapView, maplibreMap, style);
        }

        public CustomSymbolManager(@NonNull MapView mapView, @NonNull MapLibreMap maplibreMap, @NonNull Style style, @Nullable String belowLayerId, @Nullable String aboveLayerId) {
            super(mapView, maplibreMap, style, belowLayerId, aboveLayerId);
        }

        public CustomSymbolManager(@NonNull MapView mapView, @NonNull MapLibreMap maplibreMap, @NonNull Style style, @Nullable String belowLayerId, @Nullable String aboveLayerId, @Nullable GeoJsonOptions geoJsonOptions) {
            super(mapView, maplibreMap, style, belowLayerId, aboveLayerId, geoJsonOptions);
        }

        public CustomSymbolManager(@NonNull MapView mapView, @NonNull MapLibreMap maplibreMap, @NonNull Style style, @Nullable String belowLayerId, @Nullable String aboveLayerId, @NonNull ClusterOptions clusterOptions) {
            super(mapView, maplibreMap, style, belowLayerId, aboveLayerId, clusterOptions);
        }

        // hehe
        public SymbolLayer getLayer() {
            return layer;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        VehicleWebsocket.unsubscribe(VehicleMapActivity.class);
    }

    @Override
    protected void onDestroy() {
        if (stopLayer != null) {
            stopLayer.deleteAll();
            stopLayer.onDestroy();
            stopLayer = null;
        }

        mapView.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
        mapView = null;
        vehicleFeatureMap.clear();
        routeLayer = null;
        routeSource = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    private class SelectedContext {
        private int selected = -1;
        boolean following = false;
        boolean fetchedLine = false;
        boolean changed = false;

        private BottomSheetBehavior<View> behavior;
        private BaseActivity parent;

        public void setBehavior(BottomSheetBehavior<View> behavior, BaseActivity parent) {
            this.behavior = behavior;
            this.parent = parent;
            this.behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int peekHeight = Math.round(100 * displayMetrics.density); // TODO calculate so that the line icon is always visible
            behavior.setPeekHeight(peekHeight);
            behavior.setHideable(false);
        }

        public void setSelected(int selected) {
            this.selected = selected;
            following = false;
            fetchedLine = false;
            changed = true;

            if (selected != -1) {
                Vehicle vehicle = idToVehMap.get(selected);
                if (vehicle == null) {
                    this.selected = -1;
                    return;
                }

                LatLng position = toLatLng(vehicle);

                VehicleInfoBottomSheet fragment = new VehicleInfoBottomSheet(vehicle, parent);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bottom_sheet_fragment_container, fragment)
                        .commit();
                if (behavior != null) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                if (position != null) {
                    mapView.getMapAsync(mapLibreMap ->
                            mapLibreMap.animateCamera(
                                    new CameraUpdateFactory.CameraPositionUpdate(
                                            0,
                                            position,
                                            0,
                                            16.5,
                                            0,
                                            0,
                                            16,
                                            new double[4]
                                    )
                            )
                    );
                }
            } else {
                if (behavior != null) {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.bottom_sheet_fragment_container);
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
            }
        }
    }

}
