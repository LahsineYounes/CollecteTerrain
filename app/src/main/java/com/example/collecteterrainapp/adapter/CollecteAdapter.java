package com.example.collecteterrainapp.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.collecteterrainapp.R;
import com.google.firebase.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CollecteAdapter extends RecyclerView.Adapter<CollecteAdapter.ViewHolder> {

    private List<Map<String, Object>> collecteList;
    private int navigationActionId;

    public CollecteAdapter(List<Map<String, Object>> collecteList, int navigationActionId) {
        this.collecteList = collecteList;
        this.navigationActionId = navigationActionId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collecte, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> collecte = collecteList.get(position);

        holder.nomCollecteText.setText((String) collecte.get("nomCollecte"));
        // Assurez-vous que "amount" est bien un Double ou un Long avant de caster en String
        Object amountObj = collecte.get("amount");
        if (amountObj != null) {
             holder.amountText.setText("Montant : " + String.valueOf(amountObj) + " dhs");
        }
       
        holder.commentText.setText("Commentaire : " + collecte.get("comment"));

        Object timestampObj = collecte.get("timestamp");
        if (timestampObj instanceof Timestamp) {
            Timestamp ts = (Timestamp) timestampObj;
            Date date = ts.toDate();
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.dateText.setText("Enregistré le " + df.format(date));
        } else if (timestampObj instanceof Long) { // Cas pour le timestamp local (long)
            Date date = new Date((Long) timestampObj);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.dateText.setText("Enregistré le " + df.format(date));
        } else {
            holder.dateText.setText("Date inconnue");
        }

        if (this.navigationActionId != 0) {
            holder.itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("nomCollecte", (String) collecte.get("nomCollecte"));
                bundle.putFloat("amount", collecte.get("amount") != null ? ((Number) collecte.get("amount")).floatValue() : 0.0f);
                bundle.putString("comment", (String) collecte.get("comment"));
                bundle.putFloat("latitude", collecte.get("latitude") != null ? ((Number) collecte.get("latitude")).floatValue() : 0.0f);
                bundle.putFloat("longitude", collecte.get("longitude") != null ? ((Number) collecte.get("longitude")).floatValue() : 0.0f);
                
                String photoUrl = (String) collecte.get("photoUrl");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    bundle.putString("photoUrl", photoUrl);
                }

                Object tsObj = collecte.get("timestamp");
                if (tsObj instanceof Timestamp) {
                    bundle.putLong("timestamp", ((Timestamp) tsObj).toDate().getTime());
                } else if (tsObj instanceof Long) {
                    bundle.putLong("timestamp", (Long) tsObj);
                }
                Navigation.findNavController(v).navigate(this.navigationActionId, bundle);
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return collecteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nomCollecteText, amountText, commentText, dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nomCollecteText = itemView.findViewById(R.id.nomCollecteText);
            amountText = itemView.findViewById(R.id.amountText);
            commentText = itemView.findViewById(R.id.commentText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}

