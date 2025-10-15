package com.example.mhdstuff.parsing.types;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.util.request.soap.SoapSaneObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Vehicle(int id, int idB, int idC, int vType, int lType, Location location, int bearing,
                      LineAlias line, int routeId, Integer course, boolean lowFloor, int delay,
                      Stop lastStop, Stop finalStop, Optional<String> finalStopName, boolean inactive, int serviceId) implements VehicleBase {

    public static List<Vehicle> parseVehicles(JsonArray array, IdStorage storage) {
        List<Vehicle> result = new ArrayList<>();

        for (JsonElement element : array) {
            result.add(parse(element.getAsJsonObject(), storage));
        }

        return result;
    }

    public static List<Vehicle> parseVehicles(SoapSaneObject obj, IdStorage storage) {
        List<Vehicle> result = new ArrayList<>();
        for (Object o : obj) {
            result.add(parse(SoapSaneObject.parse((SoapObject) o), storage));
        }

        return result;
    }

    public static Vehicle parse(SoapSaneObject obj, IdStorage storage) {
        if (obj == null) return null;

        int azimut = obj.getInt("Azimut");
        int carNum = obj.getInt("CarNum");
        int carNumB = obj.getInt("VhcBCarNum");

        int delay = obj.getInt("DelayInMins");

        Stop finalStop = storage.stopStorage().getStop(obj.getInt("FinalStopID"));
        Stop lastStop = storage.stopStorage().getStop(obj.getInt("LastStopID"));

        boolean lowFloor = obj.getBoolean("IsBarrierLess");
        Location location = Location.parse(obj);

        LineAlias line = LineAlias.parse(obj.getString("LineID"), storage.lineStorage());

        int routeId = obj.getInt("RouteID");
        int serviceId = obj.getInt("ServiceID");

        return new Vehicle(carNum, carNumB, 0, -1, -1, location, azimut, line,
                routeId, 0, lowFloor, delay, lastStop, finalStop, Optional.empty(), false, serviceId);
    }

    public static Vehicle parse(JsonObject obj, IdStorage storage) {
        if (obj == null) return null;

        // numbers of vehicles (idk how they handle it if there is more than three lol)
        int id = obj.get("ID").getAsInt();
        int idB = obj.get("IDB").getAsInt();
        int idC = obj.get("IDC").getAsInt();

        // idfk what this is
        int vType = obj.get("VType").getAsInt();
        int lType = obj.get("LType").getAsInt();

        Location location = Location.parse(obj);

        // *probably* how many people are currently in it? not sure
        int bearing = obj.get("Bearing").getAsInt();

//        TransportLine line = TransportLine.parseFromIdAndName(storage.lineStorage(), obj.get("LineID").getAsInt(), obj.get("LineName").getAsString());

        //TODO figure out what this means
        int routeId = obj.get("RouteID").getAsInt();

        // this seems to be a number, but is presented as string in the response
        // TODO figure out if it can be safely assumed to be an int
        String course = obj.get("Course").getAsString();

        // low floor is an educated guess from LF here
        boolean lowFloor = obj.get("LF").getAsBoolean();

        int delay = obj.get("Delay").getAsInt();
        Stop lastStop = storage.stopStorage().getStop(obj.get("LastStopID").getAsInt());
        Stop finalStop = storage.stopStorage().getStop(obj.get("FinalStopID").getAsInt());

        Optional<String> finalStopName;

        if (obj.has("FinalStopName")) {
            finalStopName = Optional.of(obj.get("FinalStopName").getAsString());
        } else {
            finalStopName = Optional.empty();
        }

        // not sure what inactive signals
        boolean inactive = obj.get("IsInactive").getAsBoolean();

        return new Vehicle(
                id, idB, idC, vType, lType, location, bearing, /*line,*/null, routeId, Integer.parseInt(course),
                lowFloor, delay, lastStop, finalStop, finalStopName, inactive, -1
        );
    }

    public View createVehicleInfo(ViewGroup parent, Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.vehicle_entry_layout, parent , false);


        FrameLayout icon = view.findViewById(R.id.vehicle_line_icon);
        icon.addView(line.createLineIconView(icon, context),0);

        TextView heading = view.findViewById(R.id.vehicle_heading);
        heading.setText(finalStop.name);

        return view;
    }

    public String getVehicleNumbersString() {
        String res = id+"";
        if (idB != 0) res += " + "+idB;
        if (idC != 0) res += " + "+idC;

        return res;
    }

    public String getServiceString() {
        String res = serviceId+"";
        while (res.length() < 5) {
            res = "0"+res;
        }

        return res;
    }

    public String getFinalStopText() {
        return finalStopName.orElseGet(() -> finalStop.name);
    }

    public SpannableString getDelaySpan() {
        String text = (delay == 0) ? "včas" : (delay + " min");
        int color = getDelayColor();

        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);

        return spannable;
    }

    public int getDelayColor() {
        return getDelayColor(delay);
    }

    public static int getDelayColor(int delay) {
        int color;
        if (delay == 0) {
            color = Color.GREEN;
        } else if (delay < 3) {
            color = 0xFFEED000;
        } else if (delay < 5) {
            color = 0xFFFFA500; //orange
        } else if (delay < 10){
            color = Color.RED;
        } else {
            color = 0xFF8B0000; //darkred
        }
        return color;
    }
}
