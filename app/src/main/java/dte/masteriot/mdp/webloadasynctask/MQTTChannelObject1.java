package dte.masteriot.mdp.webloadasynctask;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MQTTChannelObject1 {

    private String nombre;
    private LatLng coordinates;
    private int last_Entry;
    private String writeKey;
    private String readKey;
    private CameraObject closestCamera;
    private int id;
    MainActivity ma;


    public MQTTChannelObject1(int id, String nombre, LatLng coordinates, int last_Entry, String writeKey,
                             String readKey, CameraObject closestCamera, MainActivity ma) {
        this.id = id;
        this.nombre = nombre;
        this.coordinates = coordinates;
        this.last_Entry = last_Entry;
        this.writeKey = writeKey;
        this.readKey = readKey;
        this.closestCamera = closestCamera;
        this.ma=ma;
    }
    public void setId(int id){ this.id=id;}

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public void setLast_Entry(int last_entry){
        if(last_entry>100) {
            ma.cambioColor(closestCamera.getNombre(), 1);
            this.last_Entry = last_entry;
        }else {
            if (this.last_Entry >= 100)
                ma.cambioColor(closestCamera.getNombre(), 0);
            this.last_Entry = last_entry;

        }
    }

    public void setWriteKey(String writeKey){
        this.writeKey = writeKey;
    }

    public void setReadKey(String readKey){
        this.readKey = readKey;
    }

    public void setClosestCamera(CameraObject closestCamera){
        this.closestCamera = closestCamera;
    }

    public int getId(){return id;}

    public String getNombre() {
        return nombre;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public int getLast_Entry(){
        return last_Entry;
    }

    public String getWriteKey(){
        return writeKey;
    }

    public String getReadKey(){
        return readKey;
    }

    public CameraObject getClosestCamera(){
        return closestCamera;
    }

}