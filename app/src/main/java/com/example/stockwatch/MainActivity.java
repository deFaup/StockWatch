package com.example.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener
{
    private HashMap<String,String> stockMap = new HashMap<>();  //(symbol,name)
    private ArrayList<Stock> userStockList = new ArrayList<>();

    private ArrayList<String> keySet = new ArrayList<>();

    private RecyclerView recycler;
    private StockAdapter stockAdapter;
    private DatabaseHandler databaseHandler;
    private SwipeRefreshLayout swiper;

    private static int counter = 10;

    private static final String TAG = "Stock_MainActivity";


    /**** Behaviour ****/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);

        /* Set up the recycler view */
        recycler = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(userStockList, this);
        recycler.setAdapter(stockAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        /* Swiper */
        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doAsyncRefreshStockInformation();
            }
        });

        /* Get tmp list from the DB */
        databaseHandler = new DatabaseHandler(this);
        final ArrayList<Stock> tmpStockList = databaseHandler.loadStocks();

        if (appHasNetwork())
        {
            /* Async download of stock symbols + names in stockMap */
            new AsyncStockLoader(stockMap).execute();

            for(Stock stock : tmpStockList)
                getAsyncStockInformation(stock, this);
        }
        else
        {
            userStockList.addAll(tmpStockList);
            // TODO sort the list but is it really necessary as I will save it sorted
            stockAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "onCreate: END");

    }
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume: ");

        if (!appHasNetwork())
            noNetworkDialog();
        else
        {
            if(stockMap.isEmpty()) new AsyncStockLoader(stockMap).execute();

        }

        //else refresh

    }
    @Override
    protected void onDestroy()
    {
        databaseHandler.shutDown();
        super.onDestroy();
    }



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
    public void onClick(View v)
    {
        int i = recycler.getChildLayoutPosition(v);
        String stockMarketURL = "http://www.marketwatch.com/investing/stock/"
                .concat(userStockList.get(i).getSymbol());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(stockMarketURL));
        startActivity(intent);
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


    /**** Dialogs ****/
    private void noNetworkDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage("Stocks cannot be updated without a network connection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void duplicateDialog(String symbol)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Duplicate Stock");
        builder.setMessage(String.format("Stock symbol %s is already displayed", symbol));

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void symbolNotFoundDialog(String symbol)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format("Symbol Not Found: %s", symbol));
        builder.setMessage("This stock symbol is unknown");

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void selectFromListDialog(final CharSequence[] symbolArray)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");
        final MainActivity main = this;

        builder.setItems(symbolArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which)
            {
                String title = symbolArray[which].toString();
                int i =0;
                for(; i < title.length(); ++i)
                    if (title.charAt(i) == '-') break;

                String symbol = title.substring(0, i-1);
                String name = stockMap.get(symbol);
                getAsyncStockInformation(new Stock(name, symbol),main);
            }
        });

        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }
    private void addStockEntryDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (et.getText().toString().isEmpty()) return;
                searchFunction(et.getText().toString().toUpperCase());
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });

        builder.setTitle("Stock selection");
        builder.setMessage("Please enter a stock symbol:");

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void searchFunction(String symbol)
    {
        ArrayList<String> symbols = new ArrayList<>();

        for(String key : stockMap.keySet())
            if (key.contains(symbol)) symbols.add(key.concat(" - ").concat(stockMap.get(key)));

        if(symbols.size()==0)
            symbolNotFoundDialog(symbol);

        else if (symbols.size()==1){
            String symbolWithName = symbols.get(0);
            int i = 0;
            for(; i < symbolWithName.length(); ++i)
                if (symbolWithName.charAt(i) == '-') break;

            String symbolOnly = symbolWithName.substring(0, i-1);
            getAsyncStockInformation(new Stock(
                    stockMap.get(symbolOnly), symbolOnly),this);
        }


        else {
            final CharSequence[] symbolArray = new CharSequence[symbols.size()];
            for (int i = 0; i < symbols.size(); ++i)
                symbolArray[i] = symbols.get(i);
            selectFromListDialog(symbolArray);
        }
    }


    /****  ****/
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
        if(!appHasNetwork()) {noNetworkDialog(); return;}
        addStockEntryDialog();
    }
    /*
        To insert something in the layout and database main == this
        To update the layout and database main == null
     */
    private void getAsyncStockInformation(@NotNull Stock stock,MainActivity main)
    {
        Log.d(TAG, "getAsyncStockInformation: " + stock.getName());
        new AsyncFinanceLoader(main).execute(stock);
    }
    public void postAsyncStockInformation(@NotNull Stock stock)
    {
        Log.d(TAG, "postAsyncStockInformation: " + stock.getName());
        if (!isStockPresent(stock.getSymbol()))
        {
            userStockList.add(stock);
            userStockList.sort(new Comparator<Stock>() {
                @Override
                public int compare(Stock o1, Stock o2) {
                    return o1.getSymbol().compareTo(o2.getSymbol());
                }
            });
            databaseHandler.addStock(stock);
        }
        else
            duplicateDialog(stock.getSymbol());

        stockAdapter.notifyDataSetChanged();
    }
    private void doAsyncRefreshStockInformation()
    {
        final ArrayList<Stock> tmpStockList = databaseHandler.loadStocks();

        if (appHasNetwork())
        {
            /* Async download of stock symbols + names in stockMap */
            userStockList.clear();
            for(Stock stock : tmpStockList)
                getAsyncStockInformation(stock, this);
        }
        else
            noNetworkDialog();

        stockAdapter.notifyDataSetChanged();
        swiper.setRefreshing(false);
    }
    private boolean isStockPresent(String symbol)
    {
        for(Stock item : userStockList)
        {
            if (item.getSymbol().compareTo(symbol) == 0) return true;
        }
        return false;
    }
}
