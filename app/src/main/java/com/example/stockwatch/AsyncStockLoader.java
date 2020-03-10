package com.example.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

class AsyncStockLoader extends AsyncTask<Void, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private HashMap<String,String> stockMap;
    private static final String DATA_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private static final String TAG = "Stock_AsyncStockLoader";


    AsyncStockLoader(HashMap<String,String> stockMap) {this.stockMap = stockMap;}

    @Override
    protected Void doInBackground(Void... voids)
    {
        StringBuilder sb = new StringBuilder();
        int res = downloadStocks(sb);

        //if (res != 0)
            //return "ERROR_DOWNLOADING_STOCK_SYMBOLS";

        HashMap<String,String> map_ = parseJSON(sb.toString());
        stockMap.putAll(map_);

        return null;
    }

    private HashMap<String,String> parseJSON(String s)
    {
        // s is a JSON Array of JSON Objects
        HashMap<String,String> stockMap = new HashMap<>();

        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++)
            {
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");

                stockMap.put(name, symbol);
            }
            return stockMap;
        }
        catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private int downloadStocks(StringBuilder sb)
    {
        Uri dataUri = Uri.parse(DATA_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "downloadStocks from: " + urlToUse);

        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Log.d(TAG, "doInBackground: ResponseCode: " + conn.getResponseCode());

            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return -1;

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "downloadStocks res: " + sb.toString());

        }
        catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return -1;
        }

        return 0;
    }


}
