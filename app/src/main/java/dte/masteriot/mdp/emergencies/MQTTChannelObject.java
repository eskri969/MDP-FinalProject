package dte.masteriot.mdp.emergencies;

import com.google.android.gms.maps.model.LatLng;
public class MQTTChannelObject {

    private String nombre;
    private LatLng coordinates;
    private int last_Entry;
    private String writeKey;
    private String readKey;
    private CameraObject closestCamera;
    private int id;
    private MainActivity ma;
    Boolean alert;


    public MQTTChannelObject(int id, String nombre, LatLng coordinates, int last_Entry, String writeKey,
                             String readKey, CameraObject closestCamera, MainActivity ma) {
        this.id = id;
        this.nombre = nombre;
        this.coordinates = coordinates;
        this.last_Entry = last_Entry;
        this.writeKey = writeKey;
        this.readKey = readKey;
        this.closestCamera = closestCamera;
        this.ma=ma;
        this.alert=false;
    }
    public void setId(int id){ this.id=id;}

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
        setClosestCamera(ma.CameraProx(coordinates));
    }

    public void setLast_Entry(int last_entry) {
        this.last_Entry = last_entry;
        ma.text.setText("Number of Emergencies " + ma.nEmergencies);
        this.closestCamera.setContaminacion(Integer.toString(last_entry));
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