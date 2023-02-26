package com.laodev.focus.models;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapModel {

    public String type = "";
    public PropertyModel properties = new PropertyModel();
    public GeometryModel geometry = new GeometryModel();

    public MapModel(JSONObject jsonObject) {
        try {
            type = jsonObject.getString("type");
            properties = new PropertyModel(jsonObject.getJSONObject("properties"));
            geometry = new GeometryModel(jsonObject.getJSONObject("geometry"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class PropertyModel {
        public String id = "";
        public String mag = "";
        public String time = "";
        public String felt = "";
        public String tsunami = "";

        public PropertyModel() {
            id = "";
            mag = "";
            time = "";
            felt = "";
            tsunami = "";
        }

        public PropertyModel(JSONObject obj) {
            try {
                id = obj.getString("id");
                mag = obj.getString("mag");
                time = obj.getString("time");
                felt = obj.getString("felt");
                tsunami = obj.getString("tsunami");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public class GeometryModel {
        public String type = "";
        public double[] coordinates = new double[3];

        public GeometryModel() {
            type = "";
            coordinates = new double[3];
        }

        public GeometryModel(JSONObject object) {
            try {
                type = object.getString("type");
                JSONArray ary = object.getJSONArray("coordinates");
                coordinates[0] = ary.getDouble(0);
                coordinates[1] = ary.getDouble(1);
                coordinates[2] = ary.getDouble(2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
