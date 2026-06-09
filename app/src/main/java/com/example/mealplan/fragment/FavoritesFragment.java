package com.example.mealplan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.example.mealplan.R;
import com.example.mealplan.adapter.FavoriteAdapter;
import com.example.mealplan.database.FavoriteDao;
import com.example.mealplan.model.FavoriteMeal;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private View layoutEmpty;
    private TextView tvFavCount;
    private FavoriteAdapter adapter;
    private FavoriteDao favoriteDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvFavorites  = view.findViewById(R.id.rv_favorites);
        layoutEmpty  = view.findViewById(R.id.layout_empty);
        tvFavCount   = view.findViewById(R.id.tv_fav_count);
        favoriteDao  = new FavoriteDao(requireContext());

        adapter = new FavoriteAdapter(requireContext(), (meal, position) -> {
            // Snackbar undo — hapus dulu dari adapter, restore kalau undo
            adapter.removeItem(position);
            checkEmpty();

            Snackbar snackbar = Snackbar.make(
                    requireView(),
                    meal.getMealName() + " dihapus dari favorit",
                    Snackbar.LENGTH_LONG
            );

            snackbar.setAction("Batalkan", v -> {
                // Restore item ke adapter (insert balik ke DB)
                executor.execute(() -> {
                    favoriteDao.insert(meal);
                    requireActivity().runOnUiThread(() -> loadFavorites());
                });
            });

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar s, int event) {
                    // Kalau snackbar dismiss bukan karena undo → hapus permanent dari DB
                    if (event != DISMISS_EVENT_ACTION) {
                        executor.execute(() -> favoriteDao.delete(meal.getMealId()));
                    }
                }
            });

            snackbar.setActionTextColor(getResources().getColor(R.color.primary_light, null));
            snackbar.show();
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFavorites.setAdapter(adapter);
        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    public void loadFavorites() {
        executor.execute(() -> {
            List<FavoriteMeal> list = favoriteDao.getAll();
            requireActivity().runOnUiThread(() -> {
                adapter.setFavorites(list);
                tvFavCount.setText(list.size() + " resep");
                checkEmpty();
            });
        });
    }

    private void checkEmpty() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }
}
