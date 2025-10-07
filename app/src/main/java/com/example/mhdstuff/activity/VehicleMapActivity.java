package com.example.mhdstuff.activity;

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
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.testing.OverpassDownloader;
import com.example.mhdstuff.activity.testing.OverpassToGeoJson;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.MapVehicle;
import com.example.mhdstuff.parsing.types.Post;
import com.example.mhdstuff.parsing.types.Vehicle;
import com.example.mhdstuff.parsing.types.VehicleBase;
import com.example.mhdstuff.util.request.VehicleWebsocket;
import com.example.mhdstuff.util.request.soap.SoapHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
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

// FIXME the text rendering is still not the best ugh
// TODO refactor, cleanup
public class VehicleMapActivity extends BaseActivity {

    private final Map<Integer, Feature> vehicleFeatureMap = new ConcurrentHashMap<>();
    private MapView mapView;
    private Timer timer;
    private SelectedContext selectedContext = new SelectedContext();

    private static final String ROUTE_SOURCE_ID = "route-source";
    private static final String ROUTE_LAYER_ID = "route-layer";
    GeoJsonSource routeSource;
    LineLayer routeLayer;
    IdStorage storage;
    CustomSymbolManager stopLayer;

    public VehicleMapActivity() {
        super("Poloha vozidel");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);
        setContentView(R.layout.activity_vehicle_map);

        selectedContext.setSelected(getIntent().getIntExtra("following", -1));

        mapView = findViewById(R.id.vehicle_map);
        mapView.onCreate(savedInstanceState);

        new Thread(() -> {
            storage = IdStorage.getInstance();


            final CountDownLatch latch = new CountDownLatch(1);
            var ref = new Object() {
                final List<VehicleBase> vehicles = new ArrayList<>();
            };

            runOnUiThread(() -> setupMap(storage, (geoJson, map) -> {
                new Thread(() -> {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    updateGeoJson(geoJson, map, ref.vehicles);
                }).start();
            }));

            ref.vehicles.addAll(Vehicle.parseVehicles(SoapHelper.getVehicles(), storage));
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

                SymbolLayer text = new SymbolLayer("text-layer", "points-source");
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

    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, List<VehicleBase> vehicles) {
        updateGeoJson(source, map, vehicles.toArray(new VehicleBase[0]));
    }

    // TODO add dynamic timer so that when a lot of elements is updated the map is redrawn prematurely
    HashMap<Integer, Integer> idMap = new HashMap<>();
    int id = 0;
    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, VehicleBase... vehicles) {
        if (timer == null) setupCountdown(source);


        int id = 0;
        for (VehicleBase vehicle : vehicles) {
            Feature feature = Feature.fromGeometry(Point.fromLngLat(vehicle.location().longitude(), vehicle.location().latitude()));
            feature.addStringProperty("color", vehicle.line().backgroundColorStr());
            feature.addStringProperty("textColor", vehicle.line().textColorStr());
            feature.addStringProperty("name", vehicle.line().lineDisplayName());
            feature.addNumberProperty("bearing", vehicle.bearing());

            if (!idMap.containsKey(vehicle.id())) {
                idMap.put(vehicle.id(), id++);
            }
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

        public void setSelected(int selected) {
            this.selected = selected;
            following = false;
            fetchedLine = false;
            changed = true;
        }

    }

}
