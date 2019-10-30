package dte.masteriot.mdp.emergencies;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Collections;
import android.location.Location;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.maps.model.LatLng;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private final String serverURI = "tcp://mqtt.thingspeak.com:1883";
    private final String MQTTKEY = "7DEGZ7VUVSYT3NOR";//"AR779WGJO3VRONTH";//

    private static final String URL_MQTT_CHANNELS ="https://api.thingspeak.com/channels.xml?api_key=2W2DSAAQK6O84GGF";// "https://api.thingspeak.com/channels.xml?api_key=PMCXXJU7ZJKZM2VO";
    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";
    private TextView text;
    ArrayList<String> nameURLS_ArrayList = new ArrayList<>();
   ArrayList<String> marcados = new ArrayList<>();
   ArrayList<MQTT_handler> handlers = new ArrayList<>();
    ArrayList<CameraObject> cameras = new ArrayList<>();
    private int posicion;
    Boolean actualizacion=false;
    String nombre="";
    LatLng canal=null;
    Bitmap imagenGuardada;
    ImageView targetImage;
    String contaminacion="";
    Boolean girado=false;
    ListView lv;
    XmlPullParserFactory parserFactory;
    ArrayAdapter adaptador;
    private ImageView im;
    LatLng coor;
    LatLng destino=null;
    String auxcoor;
    Integer pos = 0;
    Integer nEmergencies=0;
    int actualsubs=0;


    ArrayList<MQTTChannelObject> MQTTchannels = new ArrayList<>();
    private ArrayList<String> subscribedTopics = new ArrayList<>();


    private final int REQUESTPERMISIONCODE = 0;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_load);
        text = (TextView) findViewById(R.id.textView);
        targetImage = (ImageView) findViewById(R.id.imageView);
        lv = (ListView) findViewById(R.id.lv);

    /*    if (savedInstanceState != null) {
            posicion = savedInstanceState.getInt("posicion");
            imagenGuardada = savedInstanceState.getParcelable("imagen");
            targetImage.setImageBitmap(imagenGuardada);
        }*/

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                        REQUESTPERMISIONCODE);
                //REQUEST_FINE_LOCATION);
            }

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //   return;
        }else{
            readKMLCameras();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUESTPERMISIONCODE: {
                if (grantResults.length > 0) {
                    Log.i("errorCheck", "YES");
                    boolean FineLocationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean CoarseLocationPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteStoragePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    if (FineLocationPermission && CoarseLocationPermission && WriteStoragePermission && ReadStoragePermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                        readKMLCameras();
                    } else {
                        finish();
                        Log.i("errorCheck", "NO");
                    } //
                } else {
                    finish();
                }
            /*case REQUEST_COARSE_LOCATION:{

            }
            case REQUEST_READ_EXTERNAL:{

            }
            case REQUEST_WRITE_EXTERNAL:{

            }*/
            }
        }
    }

        public void readKMLCameras () {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            onLocationChanged(location);
            //      btLoad.setEnabled(false);
            //      text.setText( "Connecting to " + URL_CAMERAS );
            DownloadWebPageTask task = new DownloadWebPageTask();
            task.execute(URL_CAMERAS);
            DownloadMQTTChannlesTask taskMQTT = new DownloadMQTTChannlesTask();
            taskMQTT.execute(URL_MQTT_CHANNELS);

        }

        @Override
        public void onSaveInstanceState (Bundle outState){
            super.onSaveInstanceState(outState);
            for (int i = 0; i < handlers.size(); i++) {
                Log.v("UNSUBS", "client" + handlers.get(i).clientId);
                try {
                    if(actualsubs>0) {
                        handlers.get(i).unsubscribeToTopic(subscribedTopics.get(i));
                        actualsubs--;
                    }
                }catch(Exception e){
                    Log.v("UNSUBEX",e.toString());
                }
                if(handlers.get(i).mqttAndroidClient.isConnected()) {
                    handlers.get(i).disconnect();
                }
            }
            actualsubs=0;
            if(came!=null) {
                outState.putString("nombre", came.getNombre());
                outState.getString("camera", came.getContaminacion());

            }

            outState.putInt("posicion", posicion);
            outState.putParcelable("imagen", imagenGuardada);
            outState.putParcelable("location", coor);
            outState.putInt("nEmer", nEmergencies);
            if(destino!=null) {
                outState.putParcelable("destino", destino);
            }
        }
        @Override
        public void onRestoreInstanceState (Bundle savedInstanceState){
            super.onRestoreInstanceState(savedInstanceState);
            posicion = savedInstanceState.getInt("posicion");
            girado=true;
            imagenGuardada = savedInstanceState.getParcelable("imagen");
            coor=savedInstanceState.getParcelable("location");
            destino=savedInstanceState.getParcelable("destino");
            contaminacion=savedInstanceState.getString("contaminacion");
            nEmergencies=0;//savedInstanceState.getInt("nEmer");
             nombre=savedInstanceState.getString("nombre");
            canal=null;
            for(int i =0; i<MQTTchannels.size();i++){
                if(MQTTchannels.get(i).getClosestCamera().getNombre().equals(nombre)){
                    canal=MQTTchannels.get(i).getCoordinates();
                }
            }
            text.setText("Number emergencies: " + nEmergencies);
            targetImage.setImageBitmap(imagenGuardada);
            for(int i =0; i<MQTTchannels.size();i++){
                if(MQTTchannels.get(i).getClosestCamera().equals(nombre)){
                    canal=MQTTchannels.get(i).getCoordinates();
                }
            }
            targetImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    imagenGuardada = null;
                    Intent intent = new Intent(v.getContext(), MapsActivity.class);
                    Bundle args = new Bundle();
                    if(contaminacion!=null) {
                        args.putString("contaminacion", contaminacion);
                    }else{
                        args.putString("contaminacion", "no data");
                    }
                    args.putString("nombre", nombre);
                    args.putParcelable("coordinates", destino);
                    args.putParcelable("location", coor);
                    args.putParcelable("canal", canal);
                    //args.putParcelable("coordinates", coorURLS_ArrayList.get(pos));
                    intent.putExtra("bundle", args);
                    startActivity(intent);
                }
            });
        }


        @Override
        public void onLocationChanged (Location location){
            coor = new LatLng(location.getLatitude(), location.getLongitude());
            // textview.setText("Longitude:   "+longitude+"   Latitide:  "+latitude);

        }

        @Override
        public void onStatusChanged (String s,int i, Bundle bundle){ }

        @Override
        public void onProviderEnabled (String s){ }

        @Override
        public void onProviderDisabled (String s){ }


    private CameraObject CameraProx (LatLng channel){
        float min = Float.MAX_VALUE;
        float actual = 0;
        int index = 0;
        Location channelLoc = new Location("channelLoc");
        channelLoc.setLatitude(channel.latitude);
        channelLoc.setLongitude(channel.longitude);
        for (int i = 0; i < cameras.size(); i++) {
            Location camera = new Location("camera");
            camera.setLatitude(cameras.get(i).getCoordinates().latitude);
            camera.setLongitude(cameras.get(i).getCoordinates().longitude);
            actual = channelLoc.distanceTo(camera);

            if (actual < min) {
                min = actual;
                //Log.v("MQTTCAMD",""+min);
                index = i;
            }
        }
        Log.v("MQTTCAM", cameras.get(index).getNombre());
        return cameras.get(index);
    }


    CameraObject came;

        private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
            private String contentType = "";

            @Override
            @SuppressWarnings("deprecation")
            protected String doInBackground(String... urls) {
                String response = "";

                HttpURLConnection urlConnection = null;
                try {

                    parserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = parserFactory.newPullParser();
                    XmlPullParser parser1 = parser;

                    InputStream is = getAssets().open("CCTV.kml");
                    // Content type should be "application/vnd.google-earth.kml+xml"

                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(is, null);
                    String aux;
                    int eventType = parser.getEventType();
                    CameraObject camera = new CameraObject("", "", null);
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String elementName = null;
                        elementName = parser.getName();
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                if ("Placemark".equals(elementName)) {
                                    camera = new CameraObject("", "", null);
                                } else if ("description".equals(elementName)) {
                                    String cameraURL = parser.nextText();
                                    cameraURL = cameraURL.substring(cameraURL.indexOf("http:"));
                                    cameraURL = cameraURL.substring(0, cameraURL.indexOf(".jpg") + 4);
                                    //camerasURLS_ArrayList.add(cameraURL);
                                    camera.setUrl(cameraURL);
                                    response += cameraURL + "\n";
                                } else if ("Data".equals(elementName)) {
                                    aux = parser.getAttributeValue(null, "name");
                                    //    Log.v("EEEEE", aux );
                                    if (aux.equals("Nombre")) {
                                        String aux1;
                                        parser.nextTag();
                                        aux1 = parser.nextText();
                                        //              Log.v("aux1", aux1);
                                        nameURLS_ArrayList.add(aux1);
                                        camera.setNombre(aux1);
                                    } else {

                                    }
                                    ;

                                } else if ("coordinates".equals(elementName)) {
                                    String coorURL = parser.nextText();
                                    //coorURLS_ArrayList.add(coorURL);
                                    String lat = coorURL.substring((coorURL.indexOf(",")) + 1, coorURL.length() - 4);
                                    String lon = coorURL.substring(0, coorURL.indexOf(","));
                                    // coorURLS_ArrayList.add(new LatLng(Double.valueOf(lat).doubleValue(),Double.valueOf(lon).doubleValue()));
                                    // coor=new LatLng(Double.valueOf(parser.nextText()).doubleValue());
                                    camera.setCoordinates(new LatLng(Double.valueOf(lat).doubleValue(), Double.valueOf(lon).doubleValue()));
                                    cameras.add(camera);
                                }
                                break;
                        }
                        eventType = parser.next();
                    }

                } catch (Exception e) {
                    response = e.toString();
                }

                return response;
            }

            @Override
            protected void onPostExecute(String result) {

                lv = (ListView) findViewById(R.id.lv);
               // lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                Collections.sort(nameURLS_ArrayList);
                Collections.sort(cameras, new Comparator<CameraObject>() {
                    @Override
                    public int compare(CameraObject o1, CameraObject o2) {
                        return o1.getNombre().compareTo(o2.getNombre());

                    }
                });
                CamerasArrayAdapter countryArrayAdapter = new CamerasArrayAdapter( MainActivity.this, cameras );//nameURLS_ArrayList );
                lv.setAdapter(countryArrayAdapter);
                //ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_checked, nameURLS_ArrayList);
                //lv.setAdapter(adapter);



                lv.setClickable(true);

                if (posicion != 0) {
                    lv.setItemChecked(posicion, true);
                    CameraObject came = cameras.get(posicion);
                    ImageView im = (ImageView) ((AppCompatActivity) MainActivity.this).findViewById(R.id.imageView);
                    im.setImageBitmap(imagenGuardada);
                    //task.execute( camerasURLS_ArrayList.get(position) );
                }

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                        //Object o = lv.getItemAtPosition(position);
                        CameraObject co = (CameraObject) lv.getItemAtPosition(position);
                        //String str = (String) o;//As you are using Default String Adapter
                        String str=(String)co.getNombre();
                        //Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
                        //text.setText(camerasURLS_ArrayList.get(pos));
                        pos = position;
                        posicion = position;
                        //text.setText(co.getUrl());

                        came = null;
                        for (int i = 0; i < cameras.size(); i++) {
                            if (str == cameras.get(i).getNombre()) {
                                came = cameras.get(i);
                                break;
                            }
                        }
                        destino=came.getCoordinates();
                        CargaImagenes task = new CargaImagenes();
                        //task.execute( camerasURLS_ArrayList.get(position) );
                        task.execute(came);

                    }
                });
                /*for(int i=0; i<marcados.size();i++){
                    View view = lv.getChildAt(1);
                    aplicaCambio(view);
                }*/
            }
        }

        class CargaImagenes extends AsyncTask<CameraObject, Void, Bitmap> {

            ProgressDialog pDialog;
            CameraObject url;

            @Override
            protected void onPreExecute() {
                // TODO Auto-generated method stub
                super.onPreExecute();


            }

            @Override
            protected Bitmap doInBackground(CameraObject... params) {
                // TODO Auto-generated method stub
                Log.i("doInBackground", "Entra en doInBackground");
                url = params[0];
                Bitmap imagen = descargarImagen(url.getUrl());
                return imagen;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                // TODO Auto-generated method stub
                super.onPostExecute(result);
                ImageView im = (ImageView) ((AppCompatActivity) MainActivity.this).findViewById(R.id.imageView);
                im.setImageBitmap(result);
                im.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        imagenGuardada = null;
                        Intent intent = new Intent(v.getContext(), MapsActivity.class);
                        Bundle args = new Bundle();
                        args.putString("contaminacion", came.getContaminacion());
                        args.putParcelable("coordinates", url.getCoordinates());
                        args.putParcelable("location", coor);
                        args.putString("nombre", came.getNombre() );
                        LatLng canal=null;
                        for(int i =0; i<MQTTchannels.size();i++){
                            if(MQTTchannels.get(i).getClosestCamera().getNombre().equals(came.getNombre())){
                                canal=MQTTchannels.get(i).getCoordinates();
                            }
                        }
                        args.putParcelable("canal", canal);
                        //args.putParcelable("coordinates", coorURLS_ArrayList.get(pos));

                        intent.putExtra("bundle", args);
                        startActivity(intent);
                    }
                });
            }

            private Bitmap descargarImagen(String imageHttpAddress) {
                URL imageUrl = null;
                Bitmap imagen = null;
                try {
                    imageUrl = new URL(imageHttpAddress);
                    HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                    conn.connect();
                    imagen = BitmapFactory.decodeStream(conn.getInputStream());
                    imagenGuardada = imagen;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                return imagen;
            }
        }

        class DownloadMQTTChannlesTask extends AsyncTask<String, Void, String> {

            private String contentType = "";
            @Override
            @SuppressWarnings("deprecation")
            protected String doInBackground(String... urls) {
                String response = "";

                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(urls[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    contentType = urlConnection.getContentType();
                    InputStream is = urlConnection.getInputStream();
                    parserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = parserFactory.newPullParser();

                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(is, null);
                    String aux;
                    int eventType = parser.getEventType();
                    MQTTChannelObject channel = new MQTTChannelObject(0, "", null,
                            0, "", "", null, MainActivity.this);
                    //////////////////////////////
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String elementName = null;
                        elementName = parser.getName();
                        switch (eventType) {
                            case XmlPullParser.START_TAG:

                                if ("channel".equals(elementName)) {
                                    Log.v("MQTTNEW", "new channel");
                                    channel = new MQTTChannelObject(0, "", null,
                                            0, "", "", null, MainActivity.this);
                                } else if ("id".equals(elementName)) {
                                    String id = parser.nextText();
                                    Log.v("MQTTID", id);
                                    channel.setId(Integer.parseInt(id));
                                } else if ("name".equals(elementName)) {
                                    String name = parser.nextText();
                                    Log.v("MQTTNAME", name);
                                    channel.setNombre(name);
                                } else if ("latitude".equals(elementName)) {
                                    String lat = parser.nextText();
                                    Log.v("MQTTLAT", lat);
                                    parser.nextTag();
                                    String longit = parser.nextText();
                                    Log.v("MQTTLONG", longit);
                                    channel.setCoordinates(new LatLng(Double.valueOf(lat).doubleValue(),
                                            Double.valueOf(longit).doubleValue()));
                                } else if ("last-entry-id".equals(elementName)) {
                                    if (parser.getAttributeCount() == 1) {
                                        String laste = parser.nextText();
                                        Log.v("MQTTLAST", laste);
                                        channel.setLast_Entry(Integer.parseInt(laste));
                                    } else {
                                        Log.v("MQTTLAST", "none");
                                    }
                                } else if ("api-key".equals(elementName)) {
                                    parser.nextTag();
                                    String apik = parser.nextText();
                                    //Log.v("MQTTKEY", apik);
                                    parser.nextTag();
                                    String apif = parser.nextText();
                                    Log.v("MQTTKEYF", apif);
                                    if (apif.equals("true")) {
                                        Log.v("MQTTKEYRW", apik);
                                        channel.setWriteKey(apik);
                                    } else {
                                        Log.v("MQTTKEYR", apik);
                                        channel.setReadKey(apik);
                                        boolean repeated = false;
                                        for (int i = 0; i < MQTTchannels.size(); i++) {
                                            if (MQTTchannels.get(i).getId() == channel.getId()) {
                                                repeated = true;
                                                Log.v("MQTTREPEAT", "" + channel.getId());
                                            }
                                        }
                                        if (!repeated) {
                                            MQTTchannels.add(channel);
                                        }
                                    }

                                }
                                break;
                        }
                        eventType = parser.next();
                    }

                } catch (Exception e) {
                    response = e.toString();
                }

                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                for (int i = 0; i < MQTTchannels.size(); i++) {
                    if (!subscribedTopics.contains("channels/" + MQTTchannels.get(i).getId() + "/subscribe/fields/field1/" + MQTTchannels.get(i).getReadKey())) {
                        subscribedTopics.add("channels/" + MQTTchannels.get(i).getId() + "/subscribe/fields/field1/" + MQTTchannels.get(i).getReadKey());

                        Log.v("MQTTCHAN", "channels/" + MQTTchannels.get(i).getId() + "/subscribe/fields/field1/" + MQTTchannels.get(i).getReadKey());
                        MQTT_handler mqtthand = new MQTT_handler(i, MQTTchannels.get(i));
                        mqtthand.MQTT_handler_start(MQTTchannels.get(i), getApplicationContext());
                        actualsubs++;
                      //  MQTTchannels.get(i).setClosestCamera(CameraProx(MQTTchannels.get(i).coordinates));
                        handlers.add(mqtthand);
                    } else  {
                        Log.v("MQTTCHAN", "repeated " + "channels/" + MQTTchannels.get(i).getId() + "/subscribe/fields/field1/" + MQTTchannels.get(i).getReadKey());
                }
                }
                }

            }


    public class MQTTChannelObject {

        private String nombre;
        private LatLng coordinates;
        private int last_Entry;
        private String writeKey;
        private String readKey;
        private CameraObject closestCamera;
        private int id;
        private MainActivity ma;
        private Boolean alert;


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
            setClosestCamera(CameraProx(coordinates));
        }

        public void setLast_Entry(int last_entry) {
            this.last_Entry = last_entry;
                text.setText("Number of Emergencies " + nEmergencies);

            closestCamera.setContaminacion(Integer.toString(last_entry));
        }

        public void setWriteKey(String writeKey){
            this.writeKey = writeKey;
        }

        public void setReadKey(String readKey){
            this.readKey = readKey;
        }

        public void setClosestCamera(CameraObject closestCamera){
            this.closestCamera = closestCamera;
            marcados.add(closestCamera.getNombre());
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

    public class MQTT_handler extends AppCompatActivity {

        MqttAndroidClient mqttAndroidClient;

     //   private final String serverURI = "tcp://mqtt.thingspeak.com:1883";
     //   private final String MQTTKEY = "7DEGZ7VUVSYT3NOR";
        private String clientId;
        private String subscriptionTopic;
       MQTTChannelObject ch;

        public MQTT_handler(int clientIdN, MQTTChannelObject ch) {
            this.clientId = clientIdN+""+System.currentTimeMillis();
            this.ch = ch;
        }


        public int MQTT_handler_start(final MQTTChannelObject channel, Context context) {

            this.subscriptionTopic = "channels/" + channel.getId() + "/subscribe/fields/field1/" + channel.getReadKey();

            mqttAndroidClient = new MqttAndroidClient(context, serverURI, "client" + this.clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {

                    if (reconnect) {
                        Log.v("MQTTH", "Reconnected to : " + serverURI);
                        // Because Clean Session is true, we need to re-subscribe
                        subscribeToTopic(subscriptionTopic);
                    } else {
                        Log.v("MQTTH", "Connected to: " + serverURI);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.v("MQTTH", "The Connection was lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    //Log.v("MQTTH", "Incoming message for clientId"+clientId+": " + message.toString());
                    //Integer aux = new Byte(message.getPayload()[0]).intValue();
                    //Integer aux1= fromByteArray(message.getPayload());
                    //Integer aux2 = Integer.parseInt(new    String (message.getPayload()));
                    //Integer aux2 = byteArrayToInt(message.getPayload());

                    String str = new String (message.getPayload());
                    String str2=str.substring(str.length()-1, str.length());
                    Integer aux2;
                    if(str2.equals("\n")) {
                         aux2 = Integer.parseInt(str.substring(0, str.length() - 1));
                    }else{
                         aux2 = Integer.parseInt(str);

                    }
                    if(ch.alert && aux2<100){
                        nEmergencies--;
                        ch.alert=false;
                    }else if(!ch.alert && aux2>100){
                        nEmergencies++;
                        ch.alert=true;
                    }
                    ch.setLast_Entry(aux2);

                }



                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(true);

            mqttConnectOptions.setUserName("user");
            mqttConnectOptions.setPassword(MQTTKEY.toCharArray());

            try {
                //Log.v("Connecting to " + serverUri);
                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                        subscribeToTopic(subscriptionTopic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.v("MQTTH", "Failed to connect to: " + serverURI);
                    }
                });


            } catch (MqttException ex) {
                ex.printStackTrace();
            }
            return 0;

        }

        public void subscribeToTopic(String subscriptionTopic) {
            try {
                mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.v("MQTTH", "Subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.v("MQTTH", "Failed to subscribe");
                    }
                });
            } catch (MqttException ex) {
                System.err.println("Exception whilst subscribing");
                ex.printStackTrace();
            }
        }
        public void disconnect() {
            try {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
                mqttAndroidClient.disconnect(5);
                mqttAndroidClient.setCallback(null);
                mqttAndroidClient = null;
                Thread.sleep(200);
            }catch (MqttException | InterruptedException e){
                e.printStackTrace();
            }
        }

        public void unsubscribeToTopic(String subscriptionTopic) {
            try {
                IMqttToken unsubToken = mqttAndroidClient.unsubscribe(subscriptionTopic);
                unsubToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.v("MQTTH", "UNSubscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        Log.v("MQTTH", "Failed to UNsubscribe");
                        // some error occurred, this is very unlikely as even if the client
                        // did not had a subscription to the topic the unsubscribe action
                        // will be successfully
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    class CamerasArrayAdapter extends ArrayAdapter<CameraObject> {
        private ArrayList<CameraObject> items;
        private Context mContext;

        CamerasArrayAdapter(Context context, ArrayList<CameraObject> cameras ) {
            super( context, 0, cameras );
            items = cameras;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent ) {

            View newView = convertView;

            // This approach can be improved for performance
            if ( newView == null ) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                newView = inflater.inflate(R.layout.list, parent, false);
            }
            //-----

            TextView textView = (TextView) newView.findViewById(R.id.textView);

            CameraObject country = items.get(position);

            textView.setText(country.getNombre());
            if(country.getContaminacion()!=null) {
                if (Integer.parseInt(items.get(position).getContaminacion()) >= 100) {
                    newView.setBackgroundColor(Color.RED);

                }else {
                    newView.setBackgroundColor(Color.TRANSPARENT);
                }

            }else{
                newView.setBackgroundColor(Color.TRANSPARENT);

            }
            return newView;
        }
    }

}


