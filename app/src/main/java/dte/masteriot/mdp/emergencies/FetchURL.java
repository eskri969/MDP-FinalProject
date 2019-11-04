package dte.masteriot.mdp.emergencies;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by Vishal on 10/20/2018.
 */

public class FetchURL extends AsyncTask<String, Void, String> {
    Context mContext;
    String directionMode = "driving";
    InputStream targetStream;
    MapsActivity ma;

    public FetchURL(Context mContext, MapsActivity ma) {
        this.mContext = mContext;
        this.ma= ma;
    }

    @Override
    protected String doInBackground(String... strings) {
        // For storing data from web service
        String data="";
        directionMode = strings[1];
        try {
            // Fetching the data from web service
            data = downloadUrl(strings[0]);
            Log.d("mylog", "Background task data " + data.toString());
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }

        return data;
    }

    //pasa de string a inputstream para poder parsear el archivo
    public InputStream givenUsingPlainJava_whenConvertingStringToInputStream_thenCorrect(String s)
            throws IOException {
         targetStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
         return targetStream;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        String sa="";
        try {
            //de toda la respuesta me quedo con la parte de las coordenadas
            sa = parseador(s);
        }catch (Exception e){

        }
        //se lo mandamos a este metodo que guarda las coordenadas en un arraylist de LatLng
        //y  asi cargarlo al polyline
        ma.adaptador(sa);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line+" ");
            }
            data = sb.toString();
            Log.d("mylog", "Downloaded URL: " + data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("mylog", "Exception downloading URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    //METODO PARA QUEDARME SOLO CON EL STRING DE COORDENADAS
    private String parseador(String s) throws IOException {
        String cameraURL="";
        targetStream=givenUsingPlainJava_whenConvertingStringToInputStream_thenCorrect(s);
        XmlPullParserFactory parserFactory;
        try {
            parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(targetStream, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String elementName = null;
                elementName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("coordinates".equals(elementName)) {//buscamos el campo con las coordenadas
                            cameraURL = parser.nextText();
                            Log.v("coordinates", cameraURL);
                        }
                        break;
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
              cameraURL = e.toString();//caso de fallo
        }

            return cameraURL;
    }
}
