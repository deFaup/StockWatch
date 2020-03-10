package com.example.stockwatch;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/* stocks.json is an array of stocks
[
    {"name":"company n°0","symbol":"$$$"},
    {"name":"company n°1","symbol":"$$$"}
]
*/
class myJSON {

    private static final String TAG = "Stock_myJSON";

    public static ArrayList<Stock> loadFile(Context context, String file) {

        Log.d(TAG, "loadFile: Loading JSON File in an array of Stock");
        ArrayList<Stock> stockList = new ArrayList<>();

        try {
            InputStream is = context.openFileInput(file);

            //ObjectInputStream in = new ObjectInputStream(is);
            //JSONArray stockArray = (JSONArray)in.readObject();
            //is.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            is.close();

            JSONArray stocks = new JSONArray(sb.toString());

            for (int i=0; i < stocks.length(); ++i)
            {
                JSONObject jStock = (JSONObject) stocks.get(i);
                String name = jStock.getString(context.getString(R.string.JSONname));
                String symbol = jStock.getString(context.getString(R.string.JSONsymbol));
                Stock newStock = new Stock(name, symbol);
                stockList.add(newStock);
            }

        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockList;
    }

    public static void saveFile(Context context, String file, ArrayList<Stock> stockList)
    {
        Log.d(TAG, "saveFile: ");
        try {
            FileOutputStream fos = context.openFileOutput(file, Context.MODE_PRIVATE);

            JSONArray stocks = new JSONArray();

            for(Stock stock : stockList) {
                JSONObject jStock = new JSONObject();
                jStock.put(context.getString(R.string.JSONname), stock.getName());
                jStock.put(context.getString(R.string.JSONsymbol), stock.getSymbol());
                stocks.put(jStock);
            }
            fos.write(stocks.toString().getBytes());
            fos.close();

            Log.d(TAG, "saveProduct: JSON:\n" + stocks.toString());
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
