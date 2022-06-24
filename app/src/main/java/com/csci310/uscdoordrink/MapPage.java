package com.csci310.uscdoordrink;

import static com.csci310.uscdoordrink.Database.getMerchantByUID;
import static com.csci310.uscdoordrink.Database.receiveDeliveryStatus;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.WalkingOptions;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.LongFunction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapPage extends Fragment implements OnMapReadyCallback, OnSymbolClickListener {

    public static MapView mapView;
    private static MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private static SymbolManager symbols;
    public static SymbolManager merchantSymbol;
    private HashMap<String, MarkerView> calloutImagesMap = new HashMap<>();
    private MarkerViewManager markerViewManager;
    private String lastMerchantLocationClicked;
    private static boolean savedInstance = false;
    private static CameraPosition camPos = null;
    private static ExtendedFloatingActionButton etaDisplay;
    private static boolean continueFindingRoute = true;


    public static final String COFFEESHOP_ICON = "coffeshop_icon_id";
    public static final String TEAHOUSE_ICON = "teahouse_icon_id";
    public static final String MERCHANT_ICON = "merchant_icon_id";
    public static final String TO_MERCHANT_ROUTE_SOURCE = "merchant_route_source";
    public static final String TO_MERCHANT_ROUTE_LAYER = "merchant_route_layer";
    public static final String CALLOUT_MERCHANT_LAYER = "callout_merchant_layer";
    public static String MERCHANT_LOCATION_LAYER = null;
    public static String SYMBOL_LAYER = null;


    private boolean locationPermissionsSet;

    public static double userLatTest;
    public static double userLngTest;

    public static int duration;

    private static Style mapStyle;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        locationPermissionsSet = false;
        if (Database.user.getIsCustomer() && ((Customer) (Database.user)).getDeliveries() != null)
            receiveDeliveryStatus(((Customer) (Database.user)).getDeliveries(), new CustomerDisplayRunnable() {
                @Override
                public void run(Delivery delivery, SerializablePoint merchantLocation) {
                    return;
                }
            });
        Mapbox.getInstance(this.getContext(), getString(R.string.mapbox_access_token));
        return inflater.inflate(R.layout.activity_map_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getLocationPermissions();
        if (savedInstanceState != null && savedInstanceState.getParcelable("lastCameraPos") != null) {
            savedInstance = true;
            camPos = savedInstanceState.getParcelable("lastCameraPos");
        }
        etaDisplay = view.findViewById(R.id.mapPageETAText);
        etaDisplay.setVisibility(View.GONE);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);
    }

    public void getLocationPermissions() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                enableUserLocationPulse(mapStyle);
                                zoomToPosition(getUserPosition());
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                enableUserLocationPulse(mapStyle);
                                zoomToPosition(getUserPosition());

                            } else {
                                // No location access granted.
                                Toast.makeText(this.getContext(), "Please enable location permissions for this app", Toast.LENGTH_SHORT).show();

                            }
                        }
                );
        if(ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            locationPermissionsSet = true;

        }
    }

    /*
    Sets up map layers, sets up user location, places merchants on the map
     */
    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        markerViewManager = new MarkerViewManager(mapView, mapboxMap);
        Context locationContext = this.getContext();
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapStyle = style;
                if(locationPermissionsSet) {
                    enableUserLocationPulse(style);
                }
                if (savedInstance) {
                    mapboxMap.setCameraPosition(camPos);
                    getUserPosition();
                } else {
                        zoomToPosition(getUserPosition());
                }

                displayMarkers(style);

                //Handle route style
                style.addSource(new GeoJsonSource(TO_MERCHANT_ROUTE_SOURCE));
                LineLayer routeLayer = new LineLayer(TO_MERCHANT_ROUTE_LAYER, TO_MERCHANT_ROUTE_SOURCE);
                routeLayer.setProperties(
                        iconImage(CALLOUT_MERCHANT_LAYER),
                        lineCap(Property.LINE_CAP_ROUND),
                        lineJoin(Property.LINE_JOIN_ROUND),
                        lineWidth(3f)
                );
                style.addLayerBelow(routeLayer, SYMBOL_LAYER);
                if (style.getLayer(SYMBOL_LAYER) != null) {
                    //style.getLayer(SYMBOL_LAYER).setMinZoom(11);
                }
            }
        });
    }


    public static void removeMerchantLocationOnMap() {
        if (MERCHANT_LOCATION_LAYER != null && mapStyle != null) {
            try {
                mapStyle.removeLayer(MERCHANT_LOCATION_LAYER);
                MERCHANT_LOCATION_LAYER = null;
            }
            catch (Exception e){
                Log.w("W", "Caught remove Merchant Exception");
            }
        }
    }


    public static void updateMerchantLocation(double lat, double lng) {
        try {
            if (mapView != null && mapboxMap != null && mapStyle != null) {
                removeMerchantLocationOnMap();
                merchantSymbol = new SymbolManager(mapView, mapboxMap, mapStyle);
                MERCHANT_LOCATION_LAYER = merchantSymbol.getLayerId();
                merchantSymbol.setIconAllowOverlap(true);
                merchantSymbol.setIconIgnorePlacement(false);


                SymbolOptions merchantLocationSymbolOptions = new SymbolOptions()
                        .withLatLng(new LatLng(lat, lng))
                        .withIconImage(MERCHANT_ICON)
                        .withIconSize(0.9f)
                        .withIconAnchor("bottom");

                merchantSymbol.create(merchantLocationSymbolOptions);
            }
        }
        catch (Exception e){
            Log.w("W", "Caught merchant update exception");
        }
    }


    /*
    Set up merchant symbols (markers) that will be displayed on the map
     */
    private void displayMarkers(@NonNull Style loadedStyle) {

        symbols = new SymbolManager(mapView, mapboxMap, loadedStyle);
        SYMBOL_LAYER = symbols.getLayerId();
        symbols.setIconAllowOverlap(true);
        symbols.setIconIgnorePlacement(false);
        addImagesToStyle(loadedStyle);
        Database.getNearbyMerchants(this, symbols);//This was changed in order to allow asynchronous merchant loading

        // Uses onAnnotationClick
        symbols.addClickListener(this);
    }

    @Override
    public boolean onAnnotationClick(Symbol symbol) {
        JsonObject data = symbol.getData().getAsJsonObject();

        // Used in hash table to get marker view for deletion
        String clickedMerchantID = data.get("uid").toString();

        // Handle hiding previously clicked merchant
        if (lastMerchantLocationClicked != null) {
            markerViewManager.removeMarker(calloutImagesMap.get(lastMerchantLocationClicked));

            //
            if (lastMerchantLocationClicked.equals(clickedMerchantID)) {

                lastMerchantLocationClicked = null;
                continueFindingRoute = false;
                etaDisplay.setVisibility(View.GONE);
                GeoJsonSource source = mapboxMap.getStyle().getSourceAs(TO_MERCHANT_ROUTE_SOURCE);
                source.setGeoJson(LineString.fromPolyline("", 6));
                return true;
            }
        }

        View customView = getCustomView(data);
        LatLng position = new LatLng(symbol.getLatLng().getLatitude(), symbol.getLatLng().getLongitude());

        MarkerView markerView = new MarkerView(position, customView);

        //Center callout under marker
        calloutImagesMap.put(clickedMerchantID, markerView);
        int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        customView.measure(measureSpec, measureSpec);
        float measureWidth = customView.getMeasuredWidth();
        float measureHeight = customView.getMeasuredHeight();

        // Override view's position
        markerView.setOnPositionUpdateListener(new MarkerView.OnPositionUpdateListener() {
            @Override
            public PointF onUpdate(PointF pointF) {
                pointF.x -= measureWidth / 2;
                pointF.y -= measureHeight * 2;
                return pointF;
            }
        });
        markerViewManager.addMarker(markerView);

        lastMerchantLocationClicked = clickedMerchantID;


        // Get directions route to merchant
        Point origin = Point.fromLngLat(userLngTest, userLatTest);
        Point destination = Point.fromLngLat(symbol.getLatLng().getLongitude(), symbol.getLatLng().getLatitude());
        continueFindingRoute = true;

        String transportMethod = data.get("modeOfTransport").getAsString();

        getUserPosition();
        getRoute(origin, destination, transportMethod);

        return true;
    }

    /*
        Get custom view based on activity_merchant_bubble
        Populate view
         */
    private View getCustomView(JsonObject data) {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        FrameLayout merchantBubble = (FrameLayout) inflater.inflate(R.layout.activity_merchant_bubble, null);
        TextView titleText = merchantBubble.findViewById(R.id.marker_window_title);
        titleText.setText(data.get("name").getAsString());

        TextView descriptionText = merchantBubble.findViewById(R.id.marker_window_snippet);
        descriptionText.setText(data.get("merchantDescription").toString());

        TextView moreInfo = merchantBubble.findViewById(R.id.clickForMoreInfo);
        moreInfo.setText("Click for more info");

        merchantBubble.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        merchantBubble.setTag(data);

        return merchantBubble;
    }

    /*
    Add universal images that will be added to the style
    */
    private void addImagesToStyle(@NonNull Style loadedStyle) {
        loadedStyle.addImage(MERCHANT_ICON, BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.merchant_icon, null).getCurrent()), false);
        loadedStyle.addImage(COFFEESHOP_ICON, BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.coffeeshop_icon, null).getCurrent()), false);
        loadedStyle.addImage(TEAHOUSE_ICON, BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.teahouse_icon, null).getCurrent()), false);
    }

    /*
    Set coordinates of merchant markers the map
     */
    public void positionIconsOnMap(@NonNull SymbolManager symbols, Iterable<Merchant> points) {

        Gson gson = new GsonBuilder().create();
        for (Merchant store : points) {

            if (store.getLocation() == null) continue;

            double lat = store.getLocation().lat;
            double lng = store.getLocation().lon;

            //JsonElement merchantMarkerData = JsonParser.parseString("{uid: \""+ store.getUid() +"\", name: \"" + store.getName() + "\", description:\""+ store.getMerchantDescription() +" \", " + "type: \"" + ((store.getIsCoffee())? "coffee" : "tea")  +"\", Transport: \"" + store.getModeOfTransport()+ "\"}");
            JsonElement merchantMarkerData = JsonParser.parseString(gson.toJson(store));
            SymbolOptions merchantMarkerOptions = new SymbolOptions()
                    .withLatLng(new LatLng(lat, lng))
                    .withIconImage(store.getIsCoffee() ? COFFEESHOP_ICON : TEAHOUSE_ICON)
                    .withIconSize(0.9f)
                    .withIconAnchor("bottom")
                    .withData(merchantMarkerData);

            symbols.create(merchantMarkerOptions);

        }
    }

    public static PointF symbolCoords(Symbol symbol) {
        return mapboxMap.getProjection().toScreenLocation(symbol.getLatLng());
    }


    public static SymbolManager getSymbols() {
        return symbols;
    }

    /*
            API call to get directions from user's location to the merchant
             */
    private void getRoute(Point origin, Point destination, String transportMethod) {

        String parsedTransportMethod = "";
        if (transportMethod.equals("Cycling")) {
            parsedTransportMethod = DirectionsCriteria.PROFILE_CYCLING;
        } else if (parsedTransportMethod.equals("Walking")) {
            parsedTransportMethod = DirectionsCriteria.PROFILE_WALKING;
        } else {
            parsedTransportMethod = DirectionsCriteria.PROFILE_DRIVING;
        }


        MapboxDirections client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .profile(parsedTransportMethod)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                //Log.println(Log.INFO, "test", response.body().toJson());
                if (response.body() == null) {
                    Toast.makeText(getContext(), "A route could not be calculated for the merchant location.", Toast.LENGTH_LONG).show();
                } else if (response.body().routes().size() < 1) {
                    Toast.makeText(getContext(), "A route could not be calculated for the merchant location.", Toast.LENGTH_LONG).show();
                } else {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            GeoJsonSource source = style.getSourceAs(TO_MERCHANT_ROUTE_SOURCE);
                            if (source != null && continueFindingRoute) {
                                source.setGeoJson(LineString.fromPolyline(response.body().routes().get(0).geometry(), 6));
                                JsonObject generalRouteData = JsonParser.parseString(response.body().toJson()).getAsJsonObject();

                                // Duration in form of seconds with 2 decimal points. decimal point is thrown out
                                duration = generalRouteData.get("routes").getAsJsonArray().get(0).getAsJsonObject().get("duration").getAsInt();

                                String etaText = parseDuration(duration, transportMethod);
                                etaDisplay.setText(etaText);
                                etaDisplay.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }


            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "A route could not be generated for the location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String parseDuration(int duration, String transportMethod) {
        int minutes = duration / 60;
        //int remainingSeconds = duration % 60;
        /*while (remainingSeconds > 9) {
            remainingSeconds /= 10;
        }*/

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentDateandTime = sdf.format(new Date());
        Date date = null;
        try {
            date = sdf.parse(currentDateandTime);
        } catch (ParseException e) {
            Log.e("", "ParseException");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);




        String methodBy = transportMethod;
        if (transportMethod.equals("Driving")) {
            methodBy = "By Car";
        } else if (transportMethod.equals("Walking")) {
            methodBy = "By Walking";
        } else if (transportMethod.equals("Cycling")) {
            methodBy = "By Bike";
        }

        int calendar_hour = calendar.get(Calendar.HOUR);
        calendar_hour = (calendar_hour == 0) ? 12 : calendar_hour;

        String hour = (calendar_hour  <= 9) ? "0" + calendar_hour : calendar_hour + "";
        String minute = (calendar.get(Calendar.MINUTE)  <= 9) ? "0" + calendar.get(Calendar.MINUTE) : calendar.get(Calendar.MINUTE) + "";
        String amPM = (calendar.get(Calendar.AM_PM) == 0) ? "AM" : "PM";

        return "ETA: " + hour  + ":" + minute + " "  + amPM + "\n" + methodBy;
    }


    /*
    Display user's location on the map and zoom in to their location
     */
    @SuppressWarnings({"MissingPermission"})
    private void enableUserLocationPulse(@NonNull Style loadedStyle) {

        //Ensure location permissions are enabled

            LocationComponentOptions locationComponentOptions = LocationComponentOptions.builder(this.getContext())
                    .build();


            //Get component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();


            //Activate component with predefined options
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this.getContext(), loadedStyle)
                    .locationComponentOptions(locationComponentOptions)
                    .build());

            //Enable the component to make it visible
            locationComponent.setLocationComponentEnabled(true);


            //Zoom camera to user's location
            locationComponent.setCameraMode(CameraMode.TRACKING);

            //Set's the component's render mode
            locationComponent.setRenderMode(RenderMode.NORMAL);
    }

    private void zoomToPosition(LatLng position) {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        if (locationComponent != null && locationComponent.isLocationComponentActivated() && PermissionsManager.areLocationPermissionsGranted(this.getContext())) {
            Location userLoc = locationComponent.getLastKnownLocation();
            if (userLoc == null) {
                return;
            }

            CameraPosition cameraPositionZoomedToUser = new CameraPosition.Builder()
                    .target(position)
                    .zoom(10)
                    .build();

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPositionZoomedToUser), 5000);
        }

    }

    public static LatLng getMerchantCurrentLocation() {
        if (mapboxMap != null) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            if (locationComponent != null) {
                Location userLoc = locationComponent.getLastKnownLocation();
                if (userLoc != null) {
                    double lng = userLoc.getLongitude();
                    double lat = userLoc.getLatitude();
                    return new LatLng(lat, lng);
                }
            }
        }
        return null;
    }

    private LatLng getUserPosition() {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        if (locationComponent != null) {
            Location userLoc = null;
            if (locationComponent.isLocationComponentActivated())
                userLoc = locationComponent.getLastKnownLocation();
            if (userLoc != null) {
                userLngTest = userLoc.getLongitude();
                userLatTest = userLoc.getLatitude();
                return new LatLng(userLatTest, userLngTest);
            }
        }
        return null;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapboxMap != null) {
            outState.putParcelable("lastCameraPos", mapboxMap.getCameraPosition());
        }
    }

    public CameraPosition saveCamera() {
        return mapboxMap.getCameraPosition();
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (markerViewManager != null) {
            markerViewManager.onDestroy();
        }
        if (symbols != null) {
            symbols.onDestroy();
        }
        mapView.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            //if (merchantSymbol != null) zoomToPosition();
        }
    }

}