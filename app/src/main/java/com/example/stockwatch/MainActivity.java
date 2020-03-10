package com.example.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener
{
    private HashMap<String,String> stockMap = new HashMap<>();
    private ArrayList<Stock> userStockList = new ArrayList<>();

    private RecyclerView recycler;
    private StockAdapter stockAdapter;
    private DatabaseHandler databaseHandler;

    private static int counter = 0;

    private static final String TAG = "Stock_MainActivity";


    /**** Behaviour ****/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);

        /* Async download of stock symbols + names in stockMap */
        new AsyncStockLoader(stockMap).execute();

        /* Get tmp list from the DB */
        databaseHandler = new DatabaseHandler(this);
        final ArrayList<Stock> tmpStockList = databaseHandler.loadStocks();

        /* Set up the recycler view */
        recycler = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(userStockList, this);
        recycler.setAdapter(stockAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        if (appHasNetwork())
        {
            for(Stock stock : userStockList)
                getAsyncStockInformation(stock);
        }
        else
        {
            noNetworkDialog();
            userStockList.addAll(tmpStockList);
            // TODO sort the list but is it really necessary as I will save it sorted
            stockAdapter.notifyDataSetChanged();
        }
    }
    @Override
    protected void onDestroy() {
        databaseHandler.shutDown();
        super.onDestroy();
    }
    /*
    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop: ");
        myJSON.saveFile(getApplicationContext(),getString(R.string.stock_backup_file),userStockList);
        super.onStop();
    }*/


    /**** Menu ****/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        addNewStock();
        return true;
    }


    /**** Clicks ****/
    @Override
    public void onClick(View v) {
    }
    @Override
    public boolean onLongClick(View v)
    {
        final int pos = recycler.getChildLayoutPosition(v);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                databaseHandler.deleteStock(userStockList.get(pos).getSymbol());
                userStockList.remove(pos);
                stockAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setMessage("Do you want to delete this stock?");
        builder.setTitle("Delete stock");

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }


    /*******************/
    private void noNetworkDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage("Stocks cannot be updated without a network connection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private boolean appHasNetwork()
    {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null)
            return netInfo.isConnected();
        else return false;
    }
    private void addNewStock()
    {
        Log.d(TAG, "addNewStock: ");
        userStockList.add(new Stock("name n°" + counter++, "blabla"));
        //getStockInformation(userStockList.get(counter-1));
        stockAdapter.notifyDataSetChanged();

    }
    private void getAsyncStockInformation(@NotNull Stock stock)
    {
        Log.d(TAG, "getAsyncStockInformation: " + stock.getName());
        new AsyncFinanceLoader(this).execute(stock);
    }
    //called by AsyncFiance on post exec
    public void postAsyncStockInformation(@NotNull Stock stock)
    {
        // TODO sort the list of user stocks: can we really sort the list if there are multiple async task running
        // but I don't have this problem cause I already have my names and symbols in my tmp list

        Log.d(TAG, "postAsyncStockInformation: " + stock.getName());
        userStockList.add(stock);
        stockAdapter.notifyDataSetChanged();
    }


}
