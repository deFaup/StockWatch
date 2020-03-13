package com.example.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class AsyncFinanceLoader extends AsyncTask<Stock, Void, Stock>
{
    private static final String TAG = "Stock_AsyncFinanceLoader";
    private static final String baseUrl = "https://cloud.iexapis.com/stable/stock/";
    private static final String TOKEN = "pk_c961abca8a594bfca6572dfe74132a2e";
    private MainActivity mainActivity;

    AsyncFinanceLoader(MainActivity ma){this.mainActivity = ma;}

    @Override
    protected Stock doInBackground(Stock... stocks) {

        JSONObject jsonObject = getDataOnStock(stocks[0]);
        if (jsonObject != null)
        {
            int erno = parseJson(jsonObject, stocks[0]);
            if (erno == -1) return null;
        }
        return stocks[0];
    }

    @Override
    protected void onPostExecute(Stock stock)
    {
        if (stock != null)
            mainActivity.postAsyncStockInformation(stock);
    }

    /* Retrieve financial data about a stock using its symbol, store these in JSON Object */
    private JSONObject getDataOnStock(Stock stock)
    {
        JSONObject jsonObject = null;

        String dataUrl = baseUrl.concat(stock.getSymbol()).concat("/quote?token=").concat(TOKEN);
        Uri dataUri = Uri.parse(dataUrl);
        String urlToUse = dataUri.toString();

        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw (new Exception());

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            jsonObject = new JSONObject(sb.toString());
            return jsonObject;
        }
        catch (Exception e) {
            e.printStackTrace();
            return jsonObject;
        }
    }
    /* Find in JSON the data we need and update the Stock instance */
    private int parseJson(JSONObject jsonObject, Stock stock)
    {
        try {
            Log.d(TAG, "parseJson: \n" + jsonObject.toString());
            String companyName = jsonObject.getString("companyName");
            String price = jsonObject.getString("latestPrice");
            String priceChange = jsonObject.getString("change");
            String changePercentage = jsonObject.getString("changePercent");

            stock.setName(companyName);
            if(price.compareTo("null")==0) stock.setPrice(0.0);
            else stock.setPrice(Double.parseDouble(price));

            if(priceChange.compareTo("null")==0) stock.setPriceChange(0.0);
            else stock.setPriceChange(Double.parseDouble(priceChange));

            if(changePercentage.compareTo("null")==0) stock.setChangePercentage(0.0);
            else stock.setChangePercentage(Double.parseDouble(changePercentage));

            return 0;
        }
        catch (JSONException e) {
            //TODO make Toast if the JSON is missing the fields I'm looking for
            System.out.println(TAG + ":" + e.getStackTrace());
            return -1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
