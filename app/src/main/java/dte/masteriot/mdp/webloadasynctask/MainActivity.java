package dte.masteriot.mdp.webloadasynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class MainActivity extends AppCompatActivity {

    //    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";
    private static final String URL_CAMERAS = "http://informo.madrid.es/informo/tmadrid/CCTV.kml";
    private static final String URL_MQTT_CHANNELS = "https://api.thingspeak.com/channels.xml?api_key=2W2DSAAQK6O84GGF";
    private TextView text;
    ArrayList<String> nameURLS_ArrayList = new ArrayList<>();
    ArrayList<CameraObject> cameras = new ArrayList<>();

    ArrayList<MQTTChannelObject> MQTTchannels = new ArrayList<>();
    ArrayList<String> clienIdList = new ArrayList<>();


    private Button btLoad;
    ListView lv;
    XmlPullParserFactory parserFactory;
    ArrayAdapter adaptador;
    private ImageView im;
    LatLng coor;
    String auxcoor;
    Integer pos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_web_load);
        text =  (TextView) findViewById(R.id.textView);
        btLoad = (Button) findViewById( R.id.readWebpage );

        //adaptador = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, camerasURLS_ArrayList);
        //lv.setAdapter(adaptador);
   //     text.setText( "Click button to connect to " + URL_CAMERAS );
    }

    public void readKMLCameras(View view) {
        btLoad.setEnabled(false);
  //      text.setText( "Connecting to " + URL_CAMERAS );
        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute( URL_CAMERAS );
        DownloadMQTTChannlesTask taskMQTT = new DownloadMQTTChannlesTask();
        taskMQTT.execute(URL_MQTT_CHANNELS);
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {

        private String contentType = "";

        @Override
        @SuppressWarnings( "deprecation" )
        protected String doInBackground(String... urls) {
            String response = "";

            HttpURLConnection urlConnection = null;
            try {
           //     URL url = new URL( urls[0] );
           //     urlConnection = (HttpURLConnection) url.openConnection();
           //     contentType = urlConnection.getContentType();
           //     InputStream is = urlConnection.getInputStream();
                parserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserFactory.newPullParser();
                XmlPullParser parser1=parser;

                InputStream is =getAssets().open("CCTV.kml");
                // Content type should be "application/vnd.google-earth.kml+xml"

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                String aux;
                int eventType = parser.getEventType();
                CameraObject camera = new CameraObject("","",null);
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String elementName = null;
                    elementName = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if("Placemark".equals(elementName)){
                                camera = new CameraObject("", "", null);
                            }else if ("description".equals(elementName)) {
                                String cameraURL = parser.nextText();
                                cameraURL = cameraURL.substring(cameraURL.indexOf("http:"));
                                cameraURL = cameraURL.substring(0, cameraURL.indexOf(".jpg") + 4);
                                //camerasURLS_ArrayList.add(cameraURL);
                                camera.setUrl(cameraURL);
                                response+=cameraURL + "\n";
                            } else if ("Data".equals(elementName)) {
                                 aux=parser.getAttributeValue(null,"name");
                                Log.v("EEEEE", aux );
                                    if (aux.equals("Nombre")){
                                            String aux1;
                                            parser.nextTag();
                                            aux1=parser.nextText();
                                            Log.v("aux1", aux1);
                                        nameURLS_ArrayList.add(aux1);
                                        camera.setNombre(aux1);
                                    }else {

                                    };

                            }else if("coordinates".equals(elementName)){
                                String coorURL=parser.nextText();
                                //coorURLS_ArrayList.add(coorURL);
                                String lat= coorURL.substring((coorURL.indexOf(","))+1, coorURL.length()-4);
                                String lon = coorURL.substring(0, coorURL.indexOf(","));
                               // coorURLS_ArrayList.add(new LatLng(Double.valueOf(lat).doubleValue(),Double.valueOf(lon).doubleValue()));
                                // coor=new LatLng(Double.valueOf(parser.nextText()).doubleValue());
                                camera.setCoordinates(new LatLng(Double.valueOf(lat).doubleValue(),Double.valueOf(lon).doubleValue()));
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
       /*     Toast.makeText(MainActivity.this, contentType, Toast.LENGTH_SHORT).show();
         //   text.setText( result ); //camerasURLS_ArrayList.get(34)
            adaptador = new ArrayAdapter(MainActivity.this, R.layout.activity_web_load, camerasURLS_ArrayList);
            lv = (ListView) findViewById(R.id.lv);
            lv.setAdapter(adaptador);
            */
            btLoad.setEnabled(true);
            lv = (ListView) findViewById(R.id.lv);
            CamerasArrayAdapter countryArrayAdapter = new CamerasArrayAdapter( MainActivity.this, cameras );//nameURLS_ArrayList );
            lv.setAdapter(countryArrayAdapter);

            lv.setChoiceMode( ListView.CHOICE_MODE_SINGLE );

            lv.setClickable(true);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    //Object o = lv.getItemAtPosition(position);
                    CameraObject co = (CameraObject) lv.getItemAtPosition(position);
                    //String str=(String)o;//As you are using Default String Adapter
                    String str=(String)co.getNombre();
                    Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
                    //text.setText(camerasURLS_ArrayList.get(pos));
                    pos=position;
                    text.setText(co.getUrl());
                    CargaImagenes task = new CargaImagenes();
                    //task.execute( camerasURLS_ArrayList.get(position) );
                    task.execute(co);
                }
            });
        }
    }

    private class DownloadMQTTChannlesTask extends AsyncTask<String, Void, String> {

        private String contentType = "";

        @Override
        @SuppressWarnings( "deprecation" )
        protected String doInBackground(String... urls) {
            String response = "";

            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL( urls[0] );
                urlConnection = (HttpURLConnection) url.openConnection();
                contentType = urlConnection.getContentType();
                InputStream is = urlConnection.getInputStream();
                parserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserFactory.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                String aux;
                int eventType = parser.getEventType();
                MQTTChannelObject channel = new MQTTChannelObject(0,"",null,
                        0,"","","");
                String aux1;
                //////////////////////////////
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String elementName = null;
                    elementName = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:

                            if("channel".equals(elementName)){
                                Log.v("MQTTNEW", "new channel");
                                channel  = new MQTTChannelObject(0,"",null,
                                        0,"","","");
                            }else if ("id".equals(elementName)) {
                                String id = parser.nextText();
                                Log.v("MQTTID", id);
                                channel.setId(Integer.parseInt(id));
                            }else if ("name".equals(elementName)) {
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
                            }else if("last-entry-id".equals(elementName)){
                                if(parser.getAttributeCount()==1) {
                                    String laste = parser.nextText();
                                    Log.v("MQTTLAST", laste);
                                    channel.setLast_Entry(Integer.parseInt(laste));
                                }
                                else{
                                    Log.v("MQTTLAST", "none");
                                }
                            }else if("api-key".equals(elementName)) {
                                parser.nextTag();
                                String apik = parser.nextText();
                                //Log.v("MQTTKEY", apik);
                                parser.nextTag();
                                String apif = parser.nextText();
                                Log.v("MQTTKEYF", apif);
                                if(apif.equals("true")){
                                    Log.v("MQTTKEYRW", apik);
                                    channel.setWriteKey(apik);
                                }
                                else{
                                    Log.v("MQTTKEYR", apik);
                                    channel.setReadKey(apik);
                                    MQTTchannels.add(channel);
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
            for( int i=0; i<MQTTchannels.size();i++) {
                if(!clienIdList.contains("client"+i)) {
                    clienIdList.add("client"+i);
                    Log.v("MQTTCHAN", "channels/" + MQTTchannels.get(i).getId() + "/subscribe/fields/field1/" + MQTTchannels.get(i).getReadKey());
                    MQTT_handler mqtthand = new MQTT_handler(i,
                            "channels/" + MQTTchannels.get(i).getId() + "/subscribe/fields/field1/" + MQTTchannels.get(i).getReadKey(),
                            getApplicationContext());
                }
            }

        }

    }

    class CargaImagenes extends AsyncTask<CameraObject, Void, Bitmap>{

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
            Log.i("doInBackground" , "Entra en doInBackground");
            url = params[0];
            Bitmap imagen = descargarImagen(url.getUrl());
            return imagen;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            ImageView im = (ImageView)((AppCompatActivity) MainActivity.this).findViewById(R.id.imageView);
            im.setImageBitmap(result);
            im.setOnClickListener( new View.OnClickListener(){
                public void onClick(View v){
                    Intent intent = new Intent (v.getContext(), MapsActivity.class);
                    Bundle args = new Bundle();
                    args.putParcelable("coordinates", url.getCoordinates() );
                    //args.putParcelable("coordinates", coorURLS_ArrayList.get(pos));
                    intent.putExtra("bundle",args);
                    startActivity(intent);
                }
            });
        }
        private Bitmap descargarImagen (String imageHttpAddress){
            URL imageUrl = null;
            Bitmap imagen = null;
            try{
                imageUrl = new URL(imageHttpAddress);
                HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
                conn.connect();
                imagen = BitmapFactory.decodeStream(conn.getInputStream());
            }catch(IOException ex){
                ex.printStackTrace();
            }

            return imagen;
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


        return newView;
    }
}


