package com.example.mhdstuff.activity;

import static org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap;
import static org.maplibre.android.style.layers.PropertyFactory.iconColor;
import static org.maplibre.android.style.layers.PropertyFactory.iconImage;
import static org.maplibre.android.style.layers.PropertyFactory.iconRotate;
import static org.maplibre.android.style.layers.PropertyFactory.iconSize;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.mhdstuff.R;
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
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonOptions;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// FIXME the text rendering is still not the best ugh
public class VehicleMapActivity extends AppCompatActivity {

    private final Map<Integer, Feature> vehicleFeatureMap = new ConcurrentHashMap<>();
    private MapView mapView;
    private Timer timer;
    private int following = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);
        setContentView(R.layout.activity_vehicle_map);

        following = getIntent().getIntExtra("following", -1);
        mapView = findViewById(R.id.vehicle_map);
        mapView.onCreate(savedInstanceState);

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();


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

            var stopBitmap = toBitmap(context, R.drawable.map_pin_regular);
            var busBitmap = toBitmap(context, R.drawable.vehicle_arrow);

            map.setStyle("https://api.maptiler.com/maps/basic-v2/style.json?key=U4nGAJfk1oEvXcTaX02N");


            GeoJsonSource source = new GeoJsonSource("points-source", new GeoJsonOptions());
            map.setCameraPosition(new CameraPosition.Builder().target(new LatLng(49.191748, 16.613163)).zoom(15).build());

            findViewById(R.id.loading_spinner).setVisibility(View.GONE);

            map.getStyle(style -> {
                style.addImage("bus_icon", busBitmap, true);
                style.addImage("stop_icon", stopBitmap);

                style.addSource(source);

                CustomSymbolManager stopLayer = new CustomSymbolManager(mapView,map, style);
                stopLayer.getLayer().setMinZoom(13f);
                stopLayer.setIconAllowOverlap(true);

                SymbolLayer l = new SymbolLayer("symbol-layer", "points-source");
                l.setProperties(
                        iconImage("bus_icon"),
                        iconColor(Expression.get("color")),
                        iconSize(1.25f),
                        iconRotate(Expression.get("bearing")),
                        iconAllowOverlap(true)

                );
                SymbolLayer text = new SymbolLayer("text-layer", "points-source");
                text.setProperties(
                        textSize(11f),
                        textAllowOverlap(false),
                        textOptional(true),
                        textField(Expression.get("name")),
                        textColor(Expression.get("textColor"))
                );

                style.addLayer(l);
                style.addLayerAbove(text, "symbol-layer");

                onReady.accept(source, map);


                VehicleWebsocket.subscribe(VehicleMapActivity.class, message -> {
                    MapVehicle vehicle = MapVehicle.parse(new Gson().fromJson(message, JsonObject.class), storage.lineStorage());

                    updateGeoJson(source, map, vehicle);
                });

                for (Post post : storage.postStorage().getAllPosts()) {
                    SymbolOptions options = new SymbolOptions().withLatLng(post.location().toLatLng()).withIconImage("stop_icon").withIconSize(1f).withIconAnchor("bottom");
                    stopLayer.create(options);
                }

            });
        });
    }

    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, List<VehicleBase> vehicles) {
        updateGeoJson(source, map, vehicles.toArray(new VehicleBase[0]));
    }

    // TODO add dynamic timer so that when a lot of elements is updated the map is redrawn prematurely
    private void updateGeoJson(GeoJsonSource source, MapLibreMap map, VehicleBase... vehicles) {
        if (timer == null) setupCountdown(source);

        for (VehicleBase vehicle : vehicles) {
            Feature feature = Feature.fromGeometry(Point.fromLngLat(vehicle.location().longitude(), vehicle.location().latitude()));
            feature.addStringProperty("color", vehicle.line().backgroundColorStr());
            feature.addStringProperty("textColor", vehicle.line().textColorStr());
            feature.addStringProperty("name", vehicle.line().lineDisplayName());
            feature.addNumberProperty("bearing", vehicle.bearing());

            vehicleFeatureMap.put(vehicle.id(), feature);

            if (vehicle.id() == following) {
                runOnUiThread(() -> map.setCameraPosition(new CameraPosition.Builder().target(vehicle.location().toLatLng()).build()));
            }
        }
    }

    private void setupCountdown(GeoJsonSource source) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> source.setGeoJson(FeatureCollection.fromFeatures(vehicleFeatureMap.values().toArray(new Feature[0]))));
            }
        },0, 2_000);
    }

    @NonNull
    private static Bitmap toBitmap(Context context, int id) {
        var drawable = AppCompatResources.getDrawable(context, id);
        var bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
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
        VehicleWebsocket.unsubscribe(VehicleWebsocket.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
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

}
