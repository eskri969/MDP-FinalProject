package dte.masteriot.mdp.webloadasynctask;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    LatLng datos, location;
    RadioButton uno, dos, tres;
    Polyline currentPolyline;
    String nombre, contaminacion;
    PolylineOptions po = new PolylineOptions();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


                mapFragment.getMapAsync(this);
        Bundle parametros = this.getIntent().getParcelableExtra("bundle");
        datos = (LatLng) parametros.getParcelable("coordinates");
        location= (LatLng) parametros.getParcelable("location");
        nombre = parametros.getString("nombre");
        contaminacion = parametros.getString("contaminacion");
        if(contaminacion==null){
            contaminacion="no data";
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*if (location != null && datos != null){
            finish();
        }*/


        String url = getUrl(location, datos, "motorcar");
        new FetchURL(MapsActivity.this, this).execute(url, "motorcar");
        mMap = googleMap;
                // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(datos).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title(nombre).snippet("Latest value: " + contaminacion));//.title("Marker in Sydney"));
        mMap.addMarker(new MarkerOptions().position(location)); //****
  //      mMap.moveCamera(CameraUpdateFactory.newLatLng(datos));
  //      mMap.animateCamera( CameraUpdateFactory.zoomTo( 13.0f ) );
        uno=(RadioButton) findViewById(R.id.hybrid);
        dos=(RadioButton) findViewById(R.id.maps);
        tres=(RadioButton) findViewById(R.id.satelite);
        po.add(location) .width(5).color(Color.RED);
        currentPolyline= mMap.addPolyline(po);
        currentPolyline.setVisible(true);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(datos);
        builder.include(location);
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.15); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.animateCamera(cu);
        mMap.moveCamera(cu);

    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.hibrido:
                if (checked)
                    mMap.setMapType(4);
                  //  dos.setChecked(false);
                  //  tres.setChecked(false);
                    break;
            case R.id.maps:
                if (checked)
                    mMap.setMapType(1);
                  //  uno.setChecked(false);
                  //  tres.setChecked(false);

                    break;
            case R.id.satelite:
                if (checked)
                    mMap.setMapType(2);
                  //  uno.setChecked(false);
                  //  dos.setChecked(false);
                    break;
        }
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode){
        String str_origin = "flat=" + origin.latitude + "&flon=" + origin.longitude;
        String str_dest = "&tlat=" + dest.latitude + "&tlon=" + dest.longitude;
        String mode = "v=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String url = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&" + parameters; //+ "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {

        if(currentPolyline!=null)
            currentPolyline.remove();
        currentPolyline= mMap.addPolyline((PolylineOptions) values[0]);
    }

    public void adaptador(String s){
        ArrayList<LatLng> cords=  new ArrayList<>();
        String[] parts = s.split(" ");
        for(int i=1; i<parts.length-1; i++){
            String[] aux=parts[i].split(",");
            LatLng ll= new LatLng(parseDouble(aux[1]), parseDouble(aux[0]));
            cords.add(ll);
            po.add(ll);

        }
     //   po.setPoints(cords);

        currentPolyline.setPoints(cords);
    }
}
