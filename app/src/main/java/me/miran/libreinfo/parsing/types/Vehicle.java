package me.miran.libreinfo.parsing.types;

import android.content.Context;
import android.text.SpannableString;

import me.miran.libreinfo.parsing.storage.manager.IdStorage;
import me.miran.libreinfo.parsing.types.stop.Stop;
import me.miran.libreinfo.parsing.types.stop.StopId;
import me.miran.libreinfo.util.DelayUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Vehicle(int id, int idB, int idC, int vType, int lType, Location location, int bearing,
                      LineAlias line, int routeId, Integer course, boolean lowFloor, int delay,
                      Stop lastStop, Stop finalStop, Optional<String> finalStopName, boolean inactive, String serviceId)  {

    public static List<Vehicle> parseVehicles(JsonArray array, IdStorage storage) {
        List<Vehicle> result = new ArrayList<>();

        for (JsonElement element : array) {
            Vehicle v = parse(element.getAsJsonObject(), storage);

            if (!v.inactive) {
                result.add(v);
            }
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

        LineAlias line = LineAlias.parse(obj.get("LineID").getAsString(), storage.lineStorage());

        //TODO figure out what this means
        int routeId = obj.get("RouteID").getAsInt();

        // this seems to be a number, but is presented as string in the response
        // TODO figure out if it can be safely assumed to be an int
        String course = obj.get("Course").getAsString();

        // low floor is an educated guess from LF here
        boolean lowFloor = obj.get("LF").getAsBoolean();

        int delay = obj.get("Delay").getAsInt();
        int lastStopId = obj.get("LastStopID").getAsInt();
        int finalStopId = obj.get("FinalStopID").getAsInt();

        Stop lastStop = storage.stopStorage().getStop(StopId.original(lastStopId));
        Stop finalStop = storage.stopStorage().getStop(StopId.original(finalStopId));

        Optional<String> finalStopName;

        if (obj.has("FinalStopName")) {
            finalStopName = Optional.of(obj.get("FinalStopName").getAsString());
        } else {
            finalStopName = Optional.empty();
        }

        // not sure what inactive signals
        boolean inactive = obj.get("IsInactive").getAsBoolean();

        int courseInt = -1;

        try {
            courseInt = Integer.parseInt(course);
        } catch (NumberFormatException ignored) {
        }

        return new Vehicle(
                id, idB, idC, vType, lType, location, bearing, line, routeId, courseInt,
                lowFloor, delay, lastStop, finalStop, finalStopName, inactive, course
        );
    }

    public String getVehicleNumbersString() {
        String res = id+"";
        if (idB != 0) res += " + "+idB;
        if (idC != 0) res += " + "+idC;

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
