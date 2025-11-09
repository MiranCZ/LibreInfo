package me.miran.mhdstuff.parsing.types;

import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.util.DelayUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Vehicle(int id, int idB, int idC, int vType, int lType, Location location, int bearing,
                      LineAlias line, int routeId, Integer course, boolean lowFloor, int delay,
                      Stop lastStop, Stop finalStop, Optional<String> finalStopName, boolean inactive, int serviceId)  {

    public static List<Vehicle> parseVehicles(JsonArray array, IdStorage storage) {
        List<Vehicle> result = new ArrayList<>();

        for (JsonElement element : array) {
            result.add(parse(element.getAsJsonObject(), storage));
        }

        return result;
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

    public SpannableString getDelaySpan(Context context) {
        return DelayUtil.getDelaySpan(context, delay);
    }

    public int getDelayColor() {
        return DelayUtil.getDelayColor(delay);
    }

}
