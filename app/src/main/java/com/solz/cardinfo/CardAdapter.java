package com.solz.cardinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.solz.cardinfo.Data.AppDatabase;
import com.solz.cardinfo.Data.Card;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> mCardEntries;
    private Context mContext;


    public CardAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_view, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        final Card cardEntry = mCardEntries.get(position);

        String name = cardEntry.getCardName();
        String number = cardEntry.getCardNumber();
        int imageRes = cardEntry.getImageResource();

        holder.cardName.setText(name);
        holder.cardNumber.setText(number);
        holder.cardImageType.setImageResource(imageRes);

    }

    public void setTasks(List<Card> cardEntries) {
        mCardEntries = cardEntries;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mCardEntries == null) {
            return 0;
        }
        return mCardEntries.size();
    }






    class CardViewHolder extends RecyclerView.ViewHolder{

        ImageView cardImageType;
        TextView cardName;
        TextView cardNumber;
        ImageView delete;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

            cardImageType = itemView.findViewById(R.id.card_type_view);
            cardName = itemView.findViewById(R.id.card_name);
            cardNumber = itemView.findViewById(R.id.card_number);
            delete = itemView.findViewById(R.id.delete);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeAt(getAdapterPosition());
                }
            });
        }

        public void removeAt(int position){
            //mTransEntries.remove(position);

            Thread thread = new Thread(){
                @Override
                public void run() {
                    final AppDatabase mDb = AppDatabase.getInstance(mContext.getApplicationContext());
                    mDb.taskDao().deleteTask(mCardEntries.remove(getAdapterPosition()));
                }
            };
            thread.start();
            notifyItemRangeChanged(position,mCardEntries.size());
        }


    }
}
