package com.example.collecteterrainapp.manager.adapters; // Note: j'ai mis l'adapter dans un sous-package adapters

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Pour charger les images par URL
import com.example.collecteterrainapp.R;
import com.example.collecteterrainapp.shared.models.User;
import java.util.List;

public class RepresentativeAdapter extends RecyclerView.Adapter<RepresentativeAdapter.ViewHolder> {

    private List<User> representativeList;

    public RepresentativeAdapter(List<User> representativeList) {
        this.representativeList = representativeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_representative, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User representative = representativeList.get(position);

        holder.nameText.setText(representative.getDisplayName());
        holder.collecteCountText.setText(String.format("%d collectes", representative.getCollecteCount()));

        // Utiliser Glide pour charger la photo si l'URL est disponible
        if (representative.getPhotoUrl() != null && !representative.getPhotoUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                 .load(representative.getPhotoUrl())
                 .placeholder(R.drawable.ic_profile_placeholder) // Placeholder
                 .error(R.drawable.ic_profile_placeholder) // Image d'erreur
                 .circleCrop() // Pour afficher en cercle si désiré
                 .into(holder.photoImage);
        } else {
            holder.photoImage.setImageResource(R.drawable.ic_profile_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            // Nous passons le displayName. Si l'UID est plus fiable et disponible, préférez l'UID.
            bundle.putString("representative_username", representative.getDisplayName()); 
            // bundle.putString("representative_uid", representative.getUid()); // Alternative
            Navigation.findNavController(v).navigate(R.id.action_teamStatsFragment_to_representativeDetailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return representativeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImage;
        TextView nameText;
        TextView collecteCountText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImage = itemView.findViewById(R.id.representative_photo_item);
            nameText = itemView.findViewById(R.id.representative_name_item);
            collecteCountText = itemView.findViewById(R.id.representative_collecte_count_item);
        }
    }
} 