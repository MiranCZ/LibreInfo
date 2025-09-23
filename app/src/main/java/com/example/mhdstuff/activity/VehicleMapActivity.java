package com.example.mhdstuff.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.MapVehicle;
import com.example.mhdstuff.parsing.types.Post;
import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.parsing.types.Vehicle;
import com.example.mhdstuff.util.request.VehicleWebsocket;
import com.example.mhdstuff.util.request.soap.SoapHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.plugins.annotation.ClusterOptions;
import org.maplibre.android.plugins.annotation.Symbol;
import org.maplibre.android.plugins.annotation.SymbolManager;
import org.maplibre.android.plugins.annotation.SymbolOptions;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.RasterLayer;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonOptions;
import org.maplibre.android.style.sources.RasterSource;
import org.maplibre.android.style.sources.TileSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class VehicleMapActivity extends AppCompatActivity {

    private final Map<Integer, Symbol> vehicles = new ConcurrentHashMap<>();
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);
        setContentView(R.layout.activity_vehicle_map);

        mapView = findViewById(R.id.vehicle_map);
        mapView.onCreate(savedInstanceState);

        new Thread(() -> {
            IdStorage storage = IdStorage.getInstance();


            final CountDownLatch latch = new CountDownLatch(1);
            var ref = new Object() {
                List<Vehicle> vehicles = null;
            };

            runOnUiThread(() -> setupMap(storage, (symbolManager) -> {
                new Thread(() -> {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    for (Vehicle vehicle : ref.vehicles) {
                        MapVehicle mapVehicle = vehicle.toMapVehicle();

                        if (!vehicles.containsKey(mapVehicle.id())) {
                            updateVehicles(mapVehicle, symbolManager, storage);
                        }
                    }
                }).start();
            }));

            ref.vehicles = Vehicle.parseVehicles(SoapHelper.getVehicles(), storage);
            latch.countDown();
        }).start();
    }

    private void setupMap(IdStorage storage, Consumer<CustomSymbolManager> onReady) {
        Context context = this;
        mapView.getMapAsync(map -> {
            var stopBitmap = toBitmap(context, R.drawable.map_pin_regular);
            var busBitmap = toBitmap(context, R.drawable.vehicle_arrow);

            map.setStyle("https://api.maptiler.com/maps/basic-v2/style.json?key=U4nGAJfk1oEvXcTaX02N");


            map.setCameraPosition(new CameraPosition.Builder().target(new LatLng(49.191748,  16.613163)).zoom(15).build());
            map.getStyle(style -> {
                CustomSymbolManager symbolManager = new CustomSymbolManager(mapView,map, style);
                CustomSymbolManager vehicleSymbolManager = new CustomSymbolManager(mapView,map, style);

                SymbolLayer layer = symbolManager.getLayer();
                layer.setMinZoom(13);


                vehicleSymbolManager.getLayer().withProperties(PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP));

                style.addImage("stop_icon", stopBitmap);
                style.addImage("bus_icon", busBitmap, true);

                symbolManager.setIconAllowOverlap(true);

                vehicleSymbolManager.setIconAllowOverlap(true);
                vehicleSymbolManager.setIconIgnorePlacement(true);
                vehicleSymbolManager.setTextAllowOverlap(true);
                vehicleSymbolManager.setTextIgnorePlacement(true);

                onReady.accept(vehicleSymbolManager);

                for (Post post : storage.postStorage().getAllPosts()) {
                    SymbolOptions options = new SymbolOptions().withLatLng(post.location().toLatLng()).withIconImage("stop_icon").withIconSize(1f).withIconAnchor("bottom");
                    symbolManager.create(options);
                }

                VehicleWebsocket.subscribe(VehicleMapActivity.class, message -> {
                    MapVehicle vehicle = MapVehicle.parse(new Gson().fromJson(message, JsonObject.class));

                    updateVehicles(vehicle, vehicleSymbolManager, storage);
                });

            });
        });
    }

    private void updateVehicles(MapVehicle vehicle, CustomSymbolManager vehicleSymbolManager, IdStorage storage) {
        if (vehicles.containsKey(vehicle.id())) {
            Symbol symbol = vehicles.get(vehicle.id());
            symbol.setLatLng(vehicle.location().toLatLng());
            symbol.setIconRotate((float) vehicle.bearing());

            vehicleSymbolManager.update(symbol);
        } else {
            LineAlias alias = vehicle.line().toLineAlias(storage.lineStorage());

            SymbolOptions options = new SymbolOptions().withLatLng(vehicle.location().toLatLng())
                    .withIconImage("bus_icon").withIconRotate((float) vehicle.bearing())
                    .withTextField(alias.lineDisplayName()).withTextSize(11f)
                    .withIconSize(1.25f)
                    .withIconColor(alias.backgroundColorStr())
                    .withTextColor(alias.textColorStr())
                    .withSymbolSortKey((float) vehicles.size());

            var symbol = vehicleSymbolManager.create(options);
            vehicles.put(vehicle.id(), symbol);
        }
    }

    @NonNull
    private static Bitmap toBitmap(Context context, int id) {
        var drawable = AppCompatResources.getDrawable(context, id);
        var bitmap = Bitmap.createBitmap(96,96, Bitmap.Config.ARGB_8888);
        var canvas = new Canvas(bitmap);
        drawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
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
