package com.example.stockwatch;

import android.net.Uri;
import android.os.AsyncTask;

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
    private static final String baseUrl = "https://cloud.iexapis.com/stable/stock/";
    private static final String TOKEN = "pk_c961abca8a594bfca6572dfe74132a2e";
    private MainActivity mainActivity;

    AsyncFinanceLoader(MainActivity ma){this.mainActivity = ma;}

    @Override
    protected Stock doInBackground(Stock... stocks) {

        JSONObject jsonObject = getDataOnStock(stocks[0]);
        if (jsonObject != null) parseJson(jsonObject, stocks[0]);

        return stocks[0];
    }

    @Override
    protected void onPostExecute(Stock stock) {
        mainActivity.postAsyncStockInformation(stock);
    }

    /* Retrieve financial data about a stock using its symbol, store these in JSON Object */
    private JSONObject getDataOnStock(Stock stock)
    {
        JSONObject jsonObject = new JSONObject();

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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    return jsonObject;
    }
    /* Find in JSON the data we need and update the Stock instance */
    private void parseJson(JSONObject jsonObject, Stock stock)
    {
        try {
            String companyName = jsonObject.getString("companyName");
            String price = jsonObject.getString("latestPrice");
            String priceChange = jsonObject.getString("change");
            String changePercentage = jsonObject.getString("changePercent");

            stock.setName(companyName);
            stock.setPrice(Double.parseDouble(price));
            stock.setPriceChange(Double.parseDouble(priceChange));
            stock.setChangePercentage(Double.parseDouble(changePercentage));
        }
        catch (JSONException e) {e.printStackTrace();}
    }
}
