package com.laodev.focus.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegionModel {

    public String id = "";
    public String name = "";
    public String district = "";
    public String province = "";

    public List<Coordinate> coordinateList = new ArrayList<>();

    public RegionModel() {
        id = "";
        name = "";
        district = "";
        province = "";
        coordinateList = new ArrayList<>();
    }

    public RegionModel(JSONObject object) {
        try {
            id = object.getString("place_id");
            name = object.getString("display_name");

            JSONObject geoJson = object.getJSONObject("geojson");
            JSONArray coordinate = geoJson.getJSONArray("coordinates");
            JSONArray coorArray = coordinate.getJSONArray(0);

            coordinateList.clear();
            for (int i = 0; i < coorArray.length(); i++) {
                Coordinate coordinateModel = new Coordinate(coorArray.getJSONArray(i));
                coordinateList.add(coordinateModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class Coordinate {
        public double lat;
        public double lon;

        public Coordinate() {
            lat = 0.0;
            lon = 0.0;
        }

        public Coordinate(JSONArray object) {
            try {
                lon = object.getDouble(0);
                lat = object.getDouble(1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
