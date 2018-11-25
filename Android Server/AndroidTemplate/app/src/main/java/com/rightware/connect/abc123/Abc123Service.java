// Auto-generated stub code.
// This file can be edited and will not get overwritten by code generation tools.

package com.rightware.connect.abc123;

import java.util.Vector;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rightware.connect.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class Abc123Service extends Abc123ServiceConcept {
    private static final String TAG = "Abc123Service";
    private Context m_context;
//    private RequestQueue m_requestQueue;

//    private FirebaseDatabase database;
//    private DatabaseReference databaseRef;


    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    private RequestQueue m_requestQueue;

    private String m_city;
    public int heartRateMan=0;
    public boolean stressValue=false;
    public boolean accidentValue=false;
    public boolean proximityValue=false;
    public boolean sleepValue=false;
    public int weatherVal=0;





    protected RequestQueue getRequestQueue() {
        if (m_requestQueue == null) {
            Cache cache = new DiskBasedCache(m_context.getCacheDir(), 1024 * 1024); // 1MB cap
            Network network = new BasicNetwork(new HurlStack());
            m_requestQueue = new RequestQueue(cache, network);
            m_requestQueue.start();
        }

        return m_requestQueue;
    }

    public Abc123Service(Context context) {
        m_context = context;

        // By default, fetch Helsinki weather 1s after startup.
        m_city = "Helsinki";

        new Handler().postDelayed(new Runnable() {
            public void run() {
                setCity(m_city);
            }
        }, 1000);
    }

    /**
     * Write weather information to runtime data.
     * @param temperature temperature in celsius
     * @param windspeed m/s
     * @param winddirection weather degrees
     * @param humidity percents
     * @param cloudiness percents
     * @param icon URL to picture describing the feature.
     */

  //  int heartRateMan, boolean stressValue, boolean accidentValue, boolean sleepValue, boolean proximityValue, int weatherVal
    void setupRuntimeDataSearchResult(double temperature, double windspeed, int winddirection, int humidity, int cloudiness, String icon) {
        runtimeData().setValue("default.heartrate", heartRateMan);
        runtimeData().setValue("default.stress", stressValue);
        runtimeData().setValue("default.accident", accidentValue);
        runtimeData().setValue("default.sleepstatus", sleepValue);
        runtimeData().setValue("default.objectproximity", proximityValue);
        runtimeData().setValue("default.element_1", weatherVal);

        Log.d(TAG, "Runtime values updated in abc");

    }

    /**
     * Clear all the weather contents
     */
    void clearRuntimeDataSearchResult() {
        setupRuntimeDataSearchResult(0.0, 0.0, 0, 0, 0, "");
    }

    /**
     * Process JSON response from the weather service.
     * @param result JSON response
     */
    protected void processResults(JSONObject result) {
        Log.d(TAG, "Received JSON document: " + result.toString());

        try {
            JSONObject main = result.getJSONObject("main");

            double temperature = 0.0;
            if (main.has("temp")) {
                temperature = main.getDouble("temp");
            }

            int humidity = 0;
            if (main.has("humidity")) {
                humidity = main.getInt("humidity");
            }

            int cloudiness = heartRateMan;
//            if (result.has("clouds")) {
//                JSONObject clouds = result.getJSONObject("clouds");
//                if (clouds.has("all")) {
//                    cloudiness = clouds.getInt("all");
//                }
//            }

            double windspeed = 0.0;
            int winddirection = 0;
            if (result.has("wind")) {
                JSONObject wind = result.getJSONObject("wind");
                if (wind.has("speed")) {
                    windspeed = wind.getDouble("speed");
                }
                if (wind.has("deg")) {
                    winddirection = wind.getInt("deg");
                }
            }

            String icon = "";
            if (result.has("weather")) {
                JSONArray weather = result.getJSONArray("weather");
                if (weather != null && weather.length() > 0) {
                    if (weather.getJSONObject(0).has("icon")) {
                        icon = weather.getJSONObject(0).getString("icon");
                        if (icon.length() > 0) {
                            icon = "http://openweathermap.org/img/w/" + icon + ".png";
                        }
                    }
                }
            }
            setupRuntimeDataSearchResult(temperature, windspeed, winddirection, humidity, cloudiness, icon);
            runtimeData().setValue("search.state", 2);
            runtimeData().notifyModified();
        } catch (org.json.JSONException ex) {
            ex.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                setCity(m_city);
            }
        }, 1 * 1000);
    }

    /**
     * Process failed response from Weather backend.
     * @param description error description if any.
     */
    protected void processFailure(String description) {
        Log.d(TAG, "FAILED: " + description);

        clearRuntimeDataSearchResult();
//        runtimeData().setValue("search.errordescription", description);
//        runtimeData().setValue("search.state", 4);
        runtimeData().notifyModified();
    }

    /**
     * API function to set the name of the city.
     * @param cityname
     */
    protected void setCity(String cityname) {
        Log.i(TAG, "setCity: " + cityname);

        m_city = cityname;
        if (m_city.isEmpty()) {
            clearRuntimeDataSearchResult();
            runtimeData().notifyModified();
            return;
        }

        /// TODO: Replace with own API key.
        String APPID = "06556d2e556960c173ccf67b32be887f";
        String uri = "http://api.openweathermap.org/data/2.5/weather?q=" + cityname + "&appid=" + APPID + "&units=metric";

        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        uri,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                final JSONObject arg = response;
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        processResults(arg);
                                    }
                                });
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                final String errors = error.toString();
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        processFailure(errors);
                                    }
                                });
                            }
                        });

        // Configure runtime data to incidate ongoing query.
//        runtimeData().setValue("search.criteria", "city");
//        runtimeData().setValue("search.state", 1);
//        runtimeData().setValue("search.data", cityname);
//        runtimeData().setValue("search.errordescription", "");
//        runtimeData().notifyModified();


        //FirebaseStuff

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("HeartRate/value");
        DatabaseReference stressRef = database.getReference("Stress/value");
        DatabaseReference AccidentRef = database.getReference("Accident/value");
        DatabaseReference sleepRef = database.getReference("Sleep/value");
        DatabaseReference proximityRef = database.getReference("Proximity/value");
        DatabaseReference weatherConditionRef = database.getReference("WeatherCondition/value");

        //myRef.setValue("Hello, World!");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int value = dataSnapshot.getValue(Integer.class);
                heartRateMan = value;
                Log.d(TAG, "Firebase Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        stressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                boolean value = dataSnapshot.getValue(boolean.class);
                stressValue = value;
                Log.d(TAG, "Firebase Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        AccidentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                boolean value = dataSnapshot.getValue(boolean.class);
                accidentValue = value;
                Log.d(TAG, "Firebase Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        sleepRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                boolean value = dataSnapshot.getValue(boolean.class);
                sleepValue = value;
                Log.d(TAG, "Firebase Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        weatherConditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int value = dataSnapshot.getValue(Integer.class);
                weatherVal = value;
                Log.d(TAG, "Firebase Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        getRequestQueue().add(request);
    }
}



//
//
////    protected RequestQueue getRequestQueue() {
////        if (m_requestQueue == null) {
////            Cache cache = new DiskBasedCache(m_context.getCacheDir(), 1024 * 1024); // 1MB cap
////            Network network = new BasicNetwork(new HurlStack());
////            m_requestQueue = new RequestQueue(cache, network);
////            m_requestQueue.start();
////        }
////
////        return m_requestQueue;
////    }
//
//    public Abc123Service(Context context) {
//        m_context = context;
//
//        // By default, fetch Helsinki weather 1s after startup.
//        m_city = "Helsinki";
//
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                getValuesFromDb(m_city);
//                setupRuntimeDataSearchResult(heartRateMan,stressValue,accidentValue,sleepValue,proximityValue,weatherVal);
//
//                Log.d(TAG,"inside this should happen every second");
//            }
//        }, 1000);
//    }
//
//
//    void setupRuntimeDataSearchResult(int heartRateMan, boolean stressValue, boolean accidentValue, boolean sleepValue, boolean proximityValue, int weatherVal) {
//        runtimeData().setValue("default.heartrate", heartRateMan);
//        runtimeData().setValue("default.stress", stressValue);
//        runtimeData().setValue("default.accident", accidentValue);
//        runtimeData().setValue("default.sleepstatus", sleepValue);
//        runtimeData().setValue("default.objectproximity", proximityValue);
//        runtimeData().setValue("default.element_1", weatherVal);
//
//        Log.d(TAG, "Runtime values updated in abc");
//    }
//
//    /**
//     * Clear all the weather contents
//     */
//    void clearRuntimeDataSearchResult() {
//        setupRuntimeDataSearchResult(0, false, false, false, false, 0);
//    }
//    /**
//     * Process JSON response from the weather service.
//     * @param result JSON response
//     */
//    protected void processResults(JSONObject result) {
//        Log.d(TAG, "Received JSON document: " + result.toString());
//
//        try {
//            JSONObject main = result.getJSONObject("main");
//
//            double temperature = 0.0;
//            if (main.has("temp")) {
//                temperature = main.getDouble("temp");
//            }
//
//            int humidity = 0;
//            if (main.has("humidity")) {
//                humidity = main.getInt("humidity");
//            }
//
//            int cloudiness = heartRateMan;
////            if (result.has("clouds")) {
////                JSONObject clouds = result.getJSONObject("clouds");
////                if (clouds.has("all")) {
////                    cloudiness = clouds.getInt("all");
////                }
////            }
//
//            double windspeed = 0.0;
//            int winddirection = 0;
//
//
//            String icon = "";
//
//            setupRuntimeDataSearchResult(heartRateMan,stressValue,accidentValue,sleepValue,proximityValue,weatherVal);
//
//            runtimeData().notifyModified();
//        } catch (org.json.JSONException ex) {
//            ex.printStackTrace();
//        }
//
//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                getValuesFromDb(m_city);
//            }
//        }, 1 * 1000);
//    }
//
//    /**
//     * Process failed response from Weather backend.
//     * @param description error description if any.
//     */
//    protected void processFailure(String description) {
//        Log.d(TAG, "FAILED: " + description);
//
//        clearRuntimeDataSearchResult();
//        runtimeData().setValue("search.errordescription", description);
//        runtimeData().setValue("search.state", 4);
//        runtimeData().notifyModified();
//    }
//
//    /**
//     * API function to set the name of the city.
//     * @param cityname
//     */
//    protected void getValuesFromDb(String cityname) {
//        Log.i(TAG, "setCity: " + cityname);
//
//        m_city = cityname;
//        if (m_city.isEmpty()) {
//            clearRuntimeDataSearchResult();
//            runtimeData().notifyModified();
//            return;
//        }
//
//        /// TODO: Replace with own API key.
//        String APPID = "06556d2e556960c173ccf67b32be887f";
//        String uri = "http://api.openweathermap.org/data/2.5/weather?q=" + cityname + "&appid=" + APPID + "&units=metric";
//
////        JsonObjectRequest request =
////                new JsonObjectRequest(
////                        Request.Method.GET,
////                        uri,
////                        null,
////                        new Response.Listener<JSONObject>() {
////                            @Override
////                            public void onResponse(JSONObject response) {
////                                final JSONObject arg = response;
////                                new Handler(Looper.getMainLooper()).post(new Runnable() {
////                                    public void run() {
////                                        processResults(arg);
////                                    }
////                                });
////                            }
////                        },
////                        new Response.ErrorListener() {
////                            @Override
////                            public void onErrorResponse(VolleyError error) {
////                                final String errors = error.toString();
////                                new Handler(Looper.getMainLooper()).post(new Runnable() {
////                                    public void run() {
////                                        processFailure(errors);
////                                    }
////                                });
////                            }
////                        });
//
//        // Configure runtime data to incidate ongoing query.
////        runtimeData().setValue("search.criteria", "city");
////        runtimeData().setValue("search.state", 1);
////        runtimeData().setValue("search.data", cityname);
////        runtimeData().setValue("search.errordescription", "");
////        runtimeData().notifyModified();
//
//
//        //FirebaseStuff
//
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("HeartRate/value");
//        DatabaseReference stressRef = database.getReference("Stress/value");
//        DatabaseReference AccidentRef = database.getReference("Accident/value");
//        DatabaseReference sleepRef = database.getReference("Sleep/value");
//        DatabaseReference proximityRef = database.getReference("Proximity/value");
//        DatabaseReference weatherConditionRef = database.getReference("WeatherCondition/value");
//
//        //myRef.setValue("Hello, World!");
//
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                int value = dataSnapshot.getValue(Integer.class);
//                heartRateMan = value;
//                Log.d(TAG, "Firebase Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });
//
//        stressRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                boolean value = dataSnapshot.getValue(boolean.class);
//                stressValue = value;
//                Log.d(TAG, "Firebase Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });
//
//        AccidentRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                boolean value = dataSnapshot.getValue(boolean.class);
//                accidentValue = value;
//                Log.d(TAG, "Firebase Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });
//
//        sleepRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                boolean value = dataSnapshot.getValue(boolean.class);
//                sleepValue = value;
//                Log.d(TAG, "Firebase Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });
//
//        weatherConditionRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                int value = dataSnapshot.getValue(Integer.class);
//                weatherVal = value;
//                Log.d(TAG, "Firebase Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });
//        setupRuntimeDataSearchResult(heartRateMan,stressValue,accidentValue,sleepValue,proximityValue,weatherVal);
//
////        getRequestQueue().add(request);
//    }
//
//
//}
