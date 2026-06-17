package me.miran.libreinfo.util.request;

import me.miran.libreinfo.R;
import me.miran.libreinfo.util.Text;

public class Endpoint {

    public static final Endpoint STATIC_GTFS = new Endpoint(
            "https://mirancz.github.io/gtfsstatic",
            Text.translatable(R.string.endpoint_static_gtfs)
    );

    public static final Endpoint APP_SERVER = new Endpoint(
            "https://api.libre-info.com",
            Text.translatable(R.string.endpoint_app_server)
    );

    public final String url;
    public final Text name;

    private Endpoint(String URL, Text name) {
        url = URL;
        this.name = name;
    }

    public Endpoint resolve(String... subdomains) {
        String result = url;

        for (String subdomain : subdomains) {
            result = result +"/"+ subdomain;
        }

        return new Endpoint(result, name);
    }

}
