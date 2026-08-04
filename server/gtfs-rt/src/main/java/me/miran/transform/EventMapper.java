package me.miran.transform;

import com.google.gson.Gson;
import me.miran.ids.ApiEvent;
import me.miran.ids.ApiMeta;
import me.miran.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventMapper {


    private static final Pattern BR = Pattern.compile("<br\\s*/?>");
    private static final Pattern DELAY = Pattern.compile("Předpokládané zpoždění\\s*(\\d+)");

    public static List<Event> mapEvents(String apiResponse) {
        var gson = new Gson();

        var response = gson.fromJson(apiResponse, ApiEventsResponse.class);

        return response.events
                .stream()
                .map(
                        e -> {
                            var description = e.description();

                            List<String> parts = new ArrayList<>(List.of(BR.split(description, -1)));

                            String title = parts.isEmpty() ? "" : parts.removeFirst().trim();

                            final String PREFIX = "brno:";
                            if (title.toLowerCase().startsWith(PREFIX)) {
                                title = title.substring(PREFIX.length()).stripLeading();
                            } else {
                                return null;
                            }

                            Integer delay = null;
                            Matcher m = DELAY.matcher(description);
                            if (m.find()) {
                                delay = Integer.parseInt(m.group(1));
                                parts.removeIf(p -> DELAY.matcher(p).find());
                            }

                            String body = parts.stream()
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .collect(Collectors.joining("\n"));

                            return new Event(e.id(), title, body, e.lastUpdate(), null, delay, e.affectedLines());
                        }
                ).filter(Objects::nonNull)
                .toList();
    }

    private record ApiEventsResponse(List<ApiEvent> events, ApiMeta metadata) {
    }


}
