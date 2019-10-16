package dte.masteriot.mdp.webloadasynctask;

import com.google.android.gms.maps.model.LatLng;

public class CameraObject {

    private String url;
    private String nombre;
    public LatLng coordinates;


    public CameraObject(String nombre, String url, LatLng coordinates) {
        this.nombre = nombre;
        this.url = url;
        this.coordinates = coordinates;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUrl() {
        return url;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
}
