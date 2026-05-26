package me.miran.libreinfo.util.request;

import me.miran.libreinfo.util.Text;

public class Endpoint {

    public static final Endpoint STATIC_GTFS = new Endpoint(
            "https://mirancz.github.io/gtfsstatic",
            Text.literal("static gtfs endpoint")
    );

    public static final Endpoint APP_SERVER = new Endpoint(
            "http://138.3.254.103:5000",
            Text.literal("app server")
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
