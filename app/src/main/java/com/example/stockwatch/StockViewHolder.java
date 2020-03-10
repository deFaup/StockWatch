package com.example.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class StockViewHolder extends RecyclerView.ViewHolder
{
    public TextView name, symbol, price, change;

    public StockViewHolder(View itemView)
    {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        symbol = itemView.findViewById(R.id.symbol);
        price = itemView.findViewById(R.id.price);
        change = itemView.findViewById(R.id.change);
    }
}
