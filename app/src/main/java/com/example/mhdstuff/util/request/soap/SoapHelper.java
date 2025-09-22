package com.example.mhdstuff.util.request.soap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.util.Map;

public class SoapHelper {

    private static final String NAMESPACE = "http://tempuri.org/";
    private static final String BASE_URL = "http://kordis.idsjmk.cz:8000/Traffic/";


    public static SoapSaneObject getDepartures(int stopID) {
        return makeSoapCall(
                "GetNearDeparturesAcrossPosts",
                "NearDepartures",
                "INearDeparturesService/GetNearDeparturesAcrossPosts",
                Map.of("stopID", stopID)
        );
    }

    /**
     * Oh dear god
     */
    private static SoapSaneObject makeSoapCall(String soapName, String endpoint, String action, Map<String, Object> properties) {
        SoapObject request = new SoapObject(NAMESPACE, soapName);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            request.addProperty(entry.getKey(), entry.getValue());
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE transport = new HttpTransportSE(BASE_URL + endpoint, 30_000);


        String SOAP_ACTION = NAMESPACE + action;

        Object responseObj;
        try {
            transport.call(SOAP_ACTION, envelope);
            responseObj = envelope.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return SoapSaneObject.parse((SoapObject) responseObj);

    }

}
