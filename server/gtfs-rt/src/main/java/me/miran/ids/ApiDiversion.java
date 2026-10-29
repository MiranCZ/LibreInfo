package me.miran.ids;

import java.util.List;

public record ApiDiversion(int id, String number, String title, double latitude, double longitude, String validFrom,
                           String validTo, boolean isValid, String description, String type,
                           List<Integer> affectedLines) {
}
