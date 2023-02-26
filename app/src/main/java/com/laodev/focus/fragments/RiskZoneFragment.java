package com.laodev.focus.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.laodev.focus.R;
import com.laodev.focus.Utils.ApiManager;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.models.MapModel;
import com.laodev.focus.models.Users;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapWeight;

public class RiskZoneFragment extends Fragment{

    private MainActivity activity;
    private ProgressDialog dialog;

    private static final String EARTHQUAKE_SOURCE_URL = "https://www.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson";

    private static final String EARTHQUAKE_SOURCE_ID = "earthquakes";
    private static final String HEATMAP_LAYER_ID = "earthquakes-heat";
    private static final String HEATMAP_LAYER_SOURCE = "earthquakes";
    private static final String CIRCLE_LAYER_ID = "earthquakes-circle";
    private MapView mapView;

    private MapboxMap mapboxMap;
    private List<Users> covidUsers = new ArrayList<>();


    public RiskZoneFragment(MainActivity context) {
        activity = context;
        activity.toolBar.setTitle(activity.getString(R.string.bottom_risk_zone));

        initWithEvent();
    }

    private void initWithEvent() {
        activity.setOnBackPressed(new MainActivity.MainOnBackPressed() {
            @Override
            public void onBackPressed() {
                activity.exitDialog();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frgView = inflater.inflate(R.layout.fragment_risk_zone, container, false);

        dialog = ProgressDialog.show(getActivity(), "", getString(R.string.alert_connect_server));
        dialog.show();

        mapView = frgView.findViewById(R.id.map_risk_zone);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                RiskZoneFragment.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.DARK, style -> {
                    FireManager.getAllUsers(new FireManager.GetAllUsersListener() {
                        @Override
                        public void onFound(List<Users> userInfos) {
                            covidUsers.clear();
                            for (Users user: userInfos) {
                                if (user.userCovid.equals(Constants.COVID_INFECTED)) {
                                    covidUsers.add(user);
                                }
                            }
                            if (covidUsers.size() > 0) {
                                addEarthquakeSource(style);
                                addHeatmapLayer(style);
                                addCircleLayer(style);
                            }
                        }

                        @Override
                        public void onNotExist() {

                        }

                        @Override
                        public void onNotFound(String error) {

                        }
                    });

                });
            }
        });

        return frgView;
    }

    private void addEarthquakeSource(@NonNull Style loadedMapStyle) {
        ApiManager.onAPIConnectionResponse(EARTHQUAKE_SOURCE_URL, null, ApiManager.APIMethod.GET, new ApiManager.APIManagerCallback() {
            @Override
            public void onEventCallBack(JSONObject obj) {
                try {
                    JSONArray featureAry = obj.getJSONArray("features");
                    List<MapModel> models = new ArrayList<>();
                    for (int i = 0; i < featureAry.length(); i++) {
                        if (i > covidUsers.size() - 1) {
                            break;
                        }
                        Users user = covidUsers.get(i);

                        JSONObject object = featureAry.getJSONObject(i);
                        MapModel model = new MapModel(object);
//
//                        Random r = new Random();
//                        int i1 = r.nextInt(10000);
//                        int i2 = r.nextInt(10000);
//                        double lat = -(19.0 + (28.0 - 19.0) * i1 / 10000);
//                        double lon = -(54.0 + (63.0 - 54.0) * i2 / 10000);

                        if (user.longitude.length() == 0) {
                            continue;
                        }

                        model.geometry.coordinates[0] = Double.parseDouble(user.longitude);
                        model.geometry.coordinates[1] = Double.parseDouble(user.latitude);
                        model.geometry.coordinates[2] = 2.0;

                        models.add(model);
                    }

                    Gson gson = new Gson();
                    String json = gson.toJson(models);

                    String geoString = "{\"type\": \"FeatureCollection\",\"crs\": {\"type\": \"name\",\"properties\": {\"name\": \"urn:ogc:def:crs:OGC:1.3:CRS84\"} }," +
                            "\"features\": " + json + "}";
                    loadedMapStyle.addSource(new GeoJsonSource(EARTHQUAKE_SOURCE_ID, geoString));

                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEventInternetError(Exception e) {

            }

            @Override
            public void onEventServerError(Exception e) {

            }
        });
    }

    private void addHeatmapLayer(@NonNull Style loadedMapStyle) {
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, EARTHQUAKE_SOURCE_ID);
        layer.setMaxZoom(9);
        layer.setSourceLayer(HEATMAP_LAYER_SOURCE);
        layer.setProperties(
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0), rgba(33, 102, 172, 0),
                                literal(0.2), rgb(103, 169, 207),
                                literal(0.4), rgb(209, 229, 240),
                                literal(0.6), rgb(253, 219, 199),
                                literal(0.8), rgb(239, 138, 98),
                                literal(1), rgb(178, 24, 43)
                        )
                ),

                heatmapWeight(
                        interpolate(
                                linear(), get("mag"),
                                stop(0, 0),
                                stop(6, 1)
                        )
                ),

                heatmapIntensity(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 1),
                                stop(9, 3)
                        )
                ),

                heatmapRadius(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 2),
                                stop(9, 20)
                        )
                ),

                heatmapOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 1),
                                stop(9, 0)
                        )
                )
        );

        loadedMapStyle.addLayerAbove(layer, "waterway-label");
    }

    private void addCircleLayer(@NonNull Style loadedMapStyle) {
        CircleLayer circleLayer = new CircleLayer(CIRCLE_LAYER_ID, EARTHQUAKE_SOURCE_ID);
        circleLayer.setProperties(
                circleRadius(
                        interpolate(
                                linear(), zoom(),
//                                literal(7), interpolate(
//                                        linear(), get("mag"),
//                                        stop(1, 1),
//                                        stop(6, 4)
//                                ),
//                                literal(16), interpolate(
//                                        linear(), get("mag"),
//                                        stop(1, 5),
//                                        stop(6, 50)
//                                )
                                stop(1, 1),
                                stop(12, 6),
                                stop(13, 0),
                                stop(14, 0)
                        )
                ),

                circleColor(
                        interpolate(
                                linear(), get("mag"),
                                literal(1), rgba(33, 102, 172, 0),
                                literal(2), rgb(103, 169, 207),
                                literal(3), rgb(209, 229, 240),
                                literal(4), rgb(253, 219, 199),
                                literal(5), rgb(239, 138, 98),
                                literal(6), rgb(178, 24, 43)
                        )
                ),

                circleOpacity(
                        interpolate(
                                linear(), zoom(),
                                stop(7, 0),
                                stop(8, 1)
                        )
                ),
                circleStrokeColor(
                        interpolate(
                                linear(), zoom(),
                                literal(7), rgb(255, 255, 255),
                                literal(8), rgb(255, 255, 255),
                                literal(9), rgb(255, 255, 255),
                                literal(10), rgb(255, 255, 0),
                                literal(11), rgb(255, 255, 0),
                                literal(12), rgb(255, 255, 0),
                                literal(13), rgba(255, 255, 0, 0.8),
                                literal(14), rgba(255, 255, 0, 0.6),
                                literal(15), rgba(255, 255, 0, 0.4),
                                literal(16), rgba(255, 255, 0, 0.2)
                        )
                ),
                circleStrokeWidth(
                        interpolate(
                                linear(), zoom(),
                                stop(12, 1),
                                stop(13, 20),
                                stop(14, 50),
                                stop(15, 100),
                                stop(16, 200),
                                stop(17, 500),
                                stop(18, 1000)
                        )
                )
        );

        loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
