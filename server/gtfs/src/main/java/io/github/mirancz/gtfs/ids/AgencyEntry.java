package io.github.mirancz.gtfs.ids;

public record AgencyEntry(
        String agency_id,
        String agency_name,
        String agency_url,
        String agency_timezone,
        String agency_lang,
        String agency_phone
) {
}
