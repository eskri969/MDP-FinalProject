package dte.masteriot.mdp.webloadasynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.os.Parcelable;
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
    private TextView text;
    ArrayList<String> nameURLS_ArrayList = new ArrayList<>();
    ArrayList<String> camerasURLS_ArrayList = new ArrayList<>();
    ArrayList<String> names_ArrayList = new ArrayList<>();
    ArrayList<CameraObject> cameras = new ArrayList<>();
    private boolean checked;
    private int posicion;
    Bitmap imagenGuardada;
    ImageView targetImage;

    Parcelable state;

    //private Button btLoad;
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
        targetImage = (ImageView)findViewById(R.id.imageView);
        if(savedInstanceState!= null){
            posicion = savedInstanceState.getInt("posicion");
            imagenGuardada = savedInstanceState.getParcelable("imagen");
            targetImage.setImageBitmap(imagenGuardada);


        }


        //  if(savedInstanceState!=null) {
        //    mListInstanceState = savedInstanceState.getParcelable("salvada");
        //}
        
       // btLoad = (Button) findViewById( R.id.readWebpage );
        readKMLCameras();
        //adaptador = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, camerasURLS_ArrayList);
        //lv.setAdapter(adaptador);
   //     text.setText( "Click button to connect to " + URL_CAMERAS );
    }

    public void readKMLCameras() {
        //btLoad.setEnabled(false);
  //      text.setText( "Connecting to " + URL_CAMERAS );
        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute( URL_CAMERAS );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("posicion", posicion);
        outState.putParcelable("imagen",imagenGuardada);



    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        posicion = savedInstanceState.getInt("posicion");
        imagenGuardada = savedInstanceState.getParcelable("imagen");
        targetImage.setImageBitmap(imagenGuardada);
    }


    public void cambioColor() {
        Log.v("EEAA", "he entrado"  );
        lv = (ListView) findViewById(R.id.lv);
        lv.getChildAt(0).setBackgroundColor(Color.RED);
        String v = String.valueOf(lv.getChildAt(0));
        Log.v("EEAA", v  );



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
                                        names_ArrayList.add(aux1);
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
            lv = (ListView) findViewById(R.id.lv);




            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);



            ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_checked,names_ArrayList);
            lv.setAdapter(adapter);


           // lv.onRestoreInstanceState(mListInstanceState);

           // lv.setChoiceMode( ListView.CHOICE_MODE_SINGLE );

            lv.setClickable(true);

            if(posicion != 0 ){

                lv.setItemChecked(posicion,true);
                CameraObject came = cameras.get(posicion);

                Log.v("EEA", String.valueOf(imagenGuardada));
                ImageView im = (ImageView)((AppCompatActivity) MainActivity.this).findViewById(R.id.imageView);
                im.setImageBitmap(imagenGuardada);

                //task.execute( camerasURLS_ArrayList.get(position) );




            }



            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    Object o = lv.getItemAtPosition(position);
                    //CameraObject co = (CameraObject) lv.getItemAtPosition(position);
                    String str=(String)o;//As you are using Default String Adapter
                    //String str=(String)co.getNombre();
                    //Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
                    //text.setText(camerasURLS_ArrayList.get(pos));
                    pos=position;
                    posicion = position;

                    Log.v("EEA", "hola");
                   // text = (TextView) findViewById(R.id.textView);
                    Log.v("DESCRIPCION4",str);
                   // Log.v("DESCRIPCION4", String.valueOf(co));
                    CameraObject came = null;

                    for(int i = 0; i < cameras.size(); i++){
                        if(str == cameras.get(i).getNombre()){
                            came = cameras.get(i);
                            break;
                        }
                    }


                    //text.setText(str);
                    CargaImagenes task = new CargaImagenes();
                    //task.execute( camerasURLS_ArrayList.get(position) );
                    task.execute(came);

                }
            });




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
                imagenGuardada = imagen;
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


