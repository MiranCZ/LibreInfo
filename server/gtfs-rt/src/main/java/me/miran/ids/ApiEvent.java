package me.miran.ids;

import java.util.List;

public record ApiEvent(int id, String description, String lastUpdate, int severity, List<Integer> affectedLines) {
}
