package me.miran.mhdstuff.activity;

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

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.activity.base.BaseActivity;
import me.miran.mhdstuff.activity.bottomsheet.VehicleInfoBottomSheet;
import me.miran.mhdstuff.activity.testing.OverpassDownloader;
import me.miran.mhdstuff.activity.testing.OverpassToGeoJson;
import me.miran.mhdstuff.exception.RequestException;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.types.MapVehicle;
import me.miran.mhdstuff.parsing.types.Post;
import me.miran.mhdstuff.util.request.RequestHelper;
import me.miran.mhdstuff.util.request.VehicleWebsocket;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
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
        selectedContext.setBehavior(BottomSheetBehavior.from(bottomSheet), this);

        selectedContext.setSelected(getIntent().getIntExtra("following", -1));

        mapView = findViewById(R.id.vehicle_map);
        mapView.onCreate(savedInstanceState);

        new Thread(() -> {
            storage = IdStorage.getInstance();


            final CountDownLatch latch = new CountDownLatch(1);
            var ref = new Object() {
                final List<MapVehicle> vehicles = new ArrayList<>();
            };

            runOnUiThread(() -> setupMap(storage, (geoJson, map) -> {
                new Thread(() -> {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    updateGeoJson(geoJson, map, ref.vehicles);

                    for (MapVehicle vehicle : ref.vehicles) {
                        idToVehMap.put(vehicle.id(), vehicle);
                    }
                }).start();
            }));

            try {
                for (JsonElement vehicle : RequestHelper.getVehicles()) {
                    ref.vehicles.add(MapVehicle.parse(vehicle.getAsJsonObject(), storage));
                }
            } catch (RequestException e) {
                e.showError(this);
            }

            latch.countDown();
        }).start();
    }

    private void setupMap(IdStorage storage, BiConsumer<GeoJsonSource, MapLibreMap> onReady) {
        Context context = this;
        mapView.getMapAsync(map -> {
            map.getUiSettings().setRotateGesturesEnabled(false);
            map.getUiSettings().setCompassEnabled(false);

            var stopBitmap = toBitmap(context, R.drawable.stop, 64);
            var busBitmap = toBitmap(context, R.drawable.vehicle_arrow, 96);

            map.setStyle("https://api.maptiler.com/maps/basic-v2/style.json?key=U4nGAJfk1oEvXcTaX02N");

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

                    List<MapVehicle> vehicles = features.stream()
                            .map(feature -> idToVehMap.get((int) ((double) Double.valueOf(feature.getNumberProperty("id")+""))))
                            .collect(Collectors.toList());

                    if (vehicles.size() > 1) {
                        showVehicleSelectionDialog(vehicles);
                    } else {
                        selectedContext.setSelected(vehicles.get(0).id());
                    }
                    return true;
                });

                VehicleWebsocket.subscribe(VehicleMapActivity.class, message -> {
                    MapVehicle vehicle = MapVehicle.parse(new Gson().fromJson(message, JsonObject.class), storage);

                    updateGeoJson(source, map, vehicle);
                });



                SymbolOptions def = new SymbolOptions().withIconImage("stop_icon").withIconSize(1f).withIconAnchor("bottom");
                for (Post post : storage.postStorage().getAllPosts()) {
                    SymbolOptions options = def.withLatLng(post.location().toLatLng());
                    stopLayer.create(options);
                }

            });
        });
    }

    private void showVehicleSelectionDialog(List<MapVehicle> vehicles) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_poi);

        String[] vehicleNames = vehicles.stream()
                .map(vehicle -> vehicle.line().lineDisplayName() + " -> " + vehicle.finalStop().name)
                .toArray(String[]::new);

        builder.setItems(vehicleNames, (dialog, which) -> {
            selectedContext.setSelected(vehicles.get(which).id());
        });

        builder.create().show();
    }

    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, List<MapVehicle> vehicles) {
        updateGeoJson(source, map, vehicles.toArray(new MapVehicle[0]));
    }

    // TODO add dynamic timer so that when a lot of elements is updated the map is redrawn prematurely
    HashMap<Integer, Integer> idMap = new HashMap<>();
    HashMap<Integer, MapVehicle> idToVehMap = new HashMap<>();
    int id = 0;
    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, MapVehicle... vehicles) {
        if (timer == null) setupCountdown(source);


        int id = 0;
        for (MapVehicle vehicle : vehicles) {
            Feature feature = Feature.fromGeometry(Point.fromLngLat(vehicle.location().longitude(), vehicle.location().latitude()));
            feature.addStringProperty("color", vehicle.line().backgroundColorStr());
            feature.addStringProperty("textColor", vehicle.line().textColorStr());
            feature.addStringProperty("name", vehicle.line().lineDisplayName());
            feature.addNumberProperty("bearing", vehicle.bearing());
            feature.addNumberProperty("id", vehicle.id());

            if (!idMap.containsKey(vehicle.id())) {
                idMap.put(vehicle.id(), id++);
            }
            idToVehMap.put(vehicle.id(), vehicle);
            // FIXME this is still not the best
            feature.addNumberProperty("sort", idMap.get(vehicle.id()) % 100);

            vehicleFeatureMap.put(vehicle.id(), feature);

            if (selectedContext.following && selectedContext.selected == vehicle.id()) {
                runOnUiThread(() -> map.setCameraPosition(new CameraPosition.Builder().target(vehicle.location().toLatLng()).build()));
            }
            if (!selectedContext.fetchedLine && selectedContext.selected == vehicle.id()) {
                selectedContext.fetchedLine = true;
                new Thread(() -> {
                    String overpassJson = OverpassDownloader.downloadData(vehicle.line().lineDisplayName());

                    OverpassToGeoJson.GeoJsonPair pair = OverpassToGeoJson.convert(overpassJson, vehicle.finalStop().name);

                    runOnUiThread(() -> {
                        routeSource.setGeoJson(pair.routesGeoJson);
                        routeLayer.setProperties(
                                PropertyFactory.lineColor(vehicle.line().backgroundColorStr())
                        );
                    });
                }).start();

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
        timer.cancel();
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
                MapVehicle vehicle = idToVehMap.get(selected);

                VehicleInfoBottomSheet fragment = new VehicleInfoBottomSheet(vehicle, parent);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.bottom_sheet_fragment_container, fragment)
                        .commit();
                if (behavior != null) {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

                mapView.getMapAsync(mapLibreMap ->
                        mapLibreMap.animateCamera(
                                new CameraUpdateFactory.CameraPositionUpdate(
                                        0,
                                        vehicle.location().toLatLng(),
                                        0,
                                        16.5,
                                        new double[4]
                                )
                        )
                );
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
