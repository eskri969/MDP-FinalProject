package dte.masteriot.mdp.webloadasynctask;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng datos;
    RadioButton uno, dos, tres;
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(datos));//.title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(datos));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );
        uno=(RadioButton) findViewById(R.id.hybrid);
        dos=(RadioButton) findViewById(R.id.maps);
        tres=(RadioButton) findViewById(R.id.satelite);
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
}
