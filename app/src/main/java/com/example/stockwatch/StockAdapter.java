package com.example.stockwatch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class StockAdapter extends RecyclerView.Adapter<StockViewHolder>
{
    private List<Stock> stockList;
    private MainActivity mainActivity;
    private static final String TAG = "Stock_StockAdapter";

    StockAdapter(List<Stock> noteList, MainActivity mainActivity) {
        this.stockList = noteList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_entry, parent, false);

        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position)
    {
        Stock stock = stockList.get(position);
        Log.d(TAG, "onBindViewHolder: " + stock.getName());

        holder.name.setText(stock.getName());
        holder.symbol.setText(stock.getSymbol());
        holder.price.setText(String.format("%f", stock.getPrice()));

        // color scheme and arrow Up or Down
        if (stock.getPriceChange() > 0)
        {
            holder.name.setTextColor(Color.parseColor("#4CAF50"));
            holder.symbol.setTextColor(Color.parseColor("#4CAF50"));
            holder.price.setTextColor(Color.parseColor("#4CAF50"));
            holder.change.setTextColor(Color.parseColor("#4CAF50"));

            StringBuilder sb = new StringBuilder();
            sb.append("▲ ").append(stock.getPriceChange());
            sb.append("  (");
            sb.append(stock.getChangePercentage());
            sb.append("%)");
            holder.change.setText(sb.toString());
        }
        else if (stock.getPriceChange() < 0)
        {
            holder.name.setTextColor(Color.parseColor("#E91E63"));
            holder.symbol.setTextColor(Color.parseColor("#E91E63"));
            holder.price.setTextColor(Color.parseColor("#E91E63"));
            holder.change.setTextColor(Color.parseColor("#E91E63"));

            StringBuilder sb = new StringBuilder();
            sb.append("▼ ").append(stock.getPriceChange());
            sb.append("  (");
            sb.append(stock.getChangePercentage());
            sb.append("%)");
            holder.change.setText(sb.toString());
        }
        else{
            // if = 0 then default color is used: white
            StringBuilder sb = new StringBuilder();
            sb.append(stock.getPriceChange());
            sb.append("  (");
            sb.append(stock.getChangePercentage());
            sb.append("%)");
            holder.change.setText(sb.toString());
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        return stockList.size();
    }
}
