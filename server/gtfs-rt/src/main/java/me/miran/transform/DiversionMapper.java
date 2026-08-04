package me.miran.transform;

import com.google.gson.Gson;
import me.miran.ids.ApiDiversion;
import me.miran.ids.ApiMeta;
import me.miran.model.Diversion;

import java.util.List;

public class DiversionMapper {

    private record ApiDiversionsResponse(List<ApiDiversion> diversions, ApiMeta metadata) {
    }


    public static List<Diversion> mapDiversions(String apiResponse) {
        var gson = new Gson();

        var response = gson.fromJson(apiResponse, ApiDiversionsResponse.class);

        return response.diversions
                .stream()
                .map(
                        d ->
                                new Diversion(d.id(), d.title(), d.description(), d.validFrom(), d.validTo(), d.latitude(), d.longitude(), d.affectedLines())
                )
                .toList();
    }


}
