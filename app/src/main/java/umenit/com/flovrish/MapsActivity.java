package umenit.com.flovrish;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    int PLACE_PICKER_REQUEST = 1;
    TextView searchbartext;
    double[] latitude = new double[]{28.35601, 28.3921, 28.3420, 28.3115};
    double[] longitude = new double[]{77.1417, 77.1309, 77.1300, 77.1053};
    double[] changedlatitude = new double[]{28.5625561, 28.5010, 28.5610, 28.4010};
    double[] changedlongitude = new double[]{77.237884, 77.267884, 77.287884, 77.337884};
    double defaultlatitude = 28.5625561;
    double defaultlongitude = 77.2378848;
    String[] description = new String[]{"Lal Bangla", "Red Fort", "Tomb of Darya Khan", "Baoli"};
    String[] changeddescription = new String[]{"bhut Bangla", "pagla Fort", "Tomb of murda Khan", "pagli"};
    UiSettings mMapSettings;
    TextView chosenlocation;
    private GoogleMap mMap;
    private Intent intent;
    private ListView shopslist;
    private ArrayAdapter<String> adapter, afterfocuschangeadapter;
    private int zoomLevel;
    private int i=0;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        LinearLayout search = (LinearLayout) findViewById(R.id.searchaddress);
        searchbartext = (TextView) findViewById(R.id.search_bar);
        shopslist = (ListView) findViewById(R.id.shopslist);


        //create a instance of ArrayAdapter to show the nearby shops of choosen lacation

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, description);

        afterfocuschangeadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, changeddescription);

        shopslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) shopslist.getItemAtPosition(position);

                // Show Toast on click of listitem

                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();
            }
        });
        chosenlocation = (TextView) findViewById(R.id.chosenlocation);
        mapFragment.getMapAsync(this);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MapsActivity.this), PLACE_PICKER_REQUEST);
                    //Clear the previously drown marker over the map
                    mMap.clear();

                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        MapsActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });             }

    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * i just add a marker near Delhi, India.
     */

    @Override

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /**
         * enable to get your current position on map
         */

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Add a marker in DELHI and move the camera when activity is first created
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(defaultlatitude, defaultlongitude), 14.1f));


        //ui setting for the map
        mMapSettings = mMap.getUiSettings();
        mMapSettings.setZoomControlsEnabled(true);


//draw a circle on map
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(28.5625561, 77.2378848))
                .radius(1000)
                .strokeColor(Color.RED)
                .strokeWidth(1)
                .fillColor(Color.TRANSPARENT));

    }

    //method to draw chosen marker

    public void setMarker(double latitude, double longitude) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("picked location").draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.markershop)));
       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14.1f));
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(1000)
                .strokeColor(Color.RED)
                .strokeWidth(1)
                .fillColor(Color.TRANSPARENT));
    }
// method to draw near by marker the searched place.

    public void setnearbyMarker(double[] latitude, double[] longitude) {

        for (int i = 0; i < latitude.length; i++) {
            for (int j = i; j <= i; j++) {
                for (int k = j; k <= j; k++) {

                    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude[i], longitude[j])).title(description[k]));
                }
            }

        }
    }

    public void drawMarkerafterlocationchange(double[] changedlatitude, double[] changedlongitude)
    {
        for (int i = 0; i < changedlatitude.length; i++) {
            for (int j = i; j <= i; j++) {
                for (int k = j; k <= j; k++) {

                    mMap.addMarker(new MarkerOptions().position(new LatLng(changedlatitude[i], changedlongitude[j])).title(changeddescription[k]).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
                }
            }

        }
    }

    /**
     * receive the data from the intent after choosing a placed
     *
     * @param requestCode
     * @param resultCode
     * @param data        and show the place at searchbar and in the TextView set in slider of the map plus populate the all the maker and their
     *                    content.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();

                searchbartext.setText(toastMsg);
                //get latlang of choosen location from PlaceApi of google and store in  object reference position.

                LatLng position = place.getLatLng();
                //get the latitude and longitude location place and draw marker on it.
                double latitudeofchosen = position.latitude;
                double longitudeofchosen = position.longitude;
                setMarker(latitudeofchosen, longitudeofchosen);

                chosenlocation.setText(toastMsg);
                setnearbyMarker(latitude, longitude);
                shopslist.setAdapter(adapter);

                /**
                 * redraw marker once user stated interacting with the map.
                 */
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                              ++i;
                        //clear the previously drawn marker on the map
                        if(i==0) {
                            setnearbyMarker(latitude, longitude);//implement logic to remove
                        }
                        //draw new marker on the map
                        else if(i==2) {
                            drawMarkerafterlocationchange(changedlatitude, changedlongitude);
                        }
                        try {

                            //add new list for the nearby place of changed location

                            shopslist.setAdapter(afterfocuschangeadapter);

                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "exception can't handled" + ex, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

    }
//below methods will be used for realtime location change like gps.

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}