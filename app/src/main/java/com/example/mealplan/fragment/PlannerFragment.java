package com.example.mealplan.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.example.mealplan.R;
import com.example.mealplan.adapter.PlannerAdapter;
import com.example.mealplan.database.FavoriteDao;
import com.example.mealplan.database.PlannerDao;
import com.example.mealplan.model.FavoriteMeal;
import com.example.mealplan.model.PlannerItem;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.TextView;
import com.example.mealplan.adapter.RecipePickerAdapter;

public class PlannerFragment extends Fragment {

    private RecyclerView rvPlanner;
    private PlannerAdapter plannerAdapter;
    private PlannerDao plannerDao;
    private FavoriteDao favoriteDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planner, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPlanner   = view.findViewById(R.id.rv_planner);
        plannerDao  = new PlannerDao(requireContext());
        favoriteDao = new FavoriteDao(requireContext());

        plannerAdapter = new PlannerAdapter(requireContext(),
                new PlannerAdapter.OnPlannerActionListener() {
                    @Override
                    public void onAddClick(String day) {
                        showFavoritePicker(day);
                    }

                    @Override
                    public void onDeleteMealClick(PlannerItem item) {
                        executor.execute(() -> {
                            plannerDao.deleteById(item.getId());
                            List<PlannerItem> updated = plannerDao.getAll();
                            runOnUi(() -> {
                                plannerAdapter.setPlannerItems(updated);

                                Snackbar snackbar = Snackbar.make(
                                        requireView(),
                                        item.getMealName() + " dihapus dari " + item.getDayOfWeek(),
                                        Snackbar.LENGTH_LONG);

                                snackbar.setAction("Batalkan", v -> {
                                    executor.execute(() -> {
                                        plannerDao.insert(item);
                                        List<PlannerItem> restored = plannerDao.getAll();
                                        runOnUi(() ->
                                                plannerAdapter.setPlannerItems(restored));
                                    });
                                });

                                snackbar.setActionTextColor(
                                        getResources().getColor(R.color.primary_light, null));
                                snackbar.show();
                            });
                        });
                    }
                });

        rvPlanner.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPlanner.setAdapter(plannerAdapter);
        loadPlanner();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlanner();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void loadPlanner() {
        executor.execute(() -> {
            List<PlannerItem> items = plannerDao.getAll();
            runOnUi(() -> plannerAdapter.setPlannerItems(items));
        });
    }

    private void showFavoritePicker(String day) {
        executor.execute(() -> {
            List<FavoriteMeal> favorites = favoriteDao.getAll();
            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;

                if (favorites.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Belum ada favorit")
                            .setMessage("Tambahkan resep ke favorit terlebih dahulu dari halaman utama.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                View dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_recipe_picker, null);
                TextView tvTitle = dialogView.findViewById(R.id.tv_picker_title);
                RecyclerView rv = dialogView.findViewById(R.id.rv_recipe_picker);
                tvTitle.setText("Pilih resep untuk " + day);
                rv.setLayoutManager(new LinearLayoutManager(requireContext()));

                androidx.appcompat.app.AlertDialog dialog =
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setView(dialogView)
                                .setNegativeButton("Batal", null)
                                .create();

                RecipePickerAdapter pickerAdapter = new RecipePickerAdapter(
                        requireContext(), favorites, chosen -> {
                    dialog.dismiss();
                    PlannerItem item = new PlannerItem(
                            day,
                            chosen.getMealId(),
                            chosen.getMealName(),
                            chosen.getMealThumb());
                    executor.execute(() -> {
                        plannerDao.insert(item);
                        List<PlannerItem> updated = plannerDao.getAll();
                        requireActivity().runOnUiThread(() ->
                                plannerAdapter.setPlannerItems(updated));
                    });
                });
                rv.setAdapter(pickerAdapter);
                dialog.show();
            });
        });
    }

    private void runOnUi(Runnable action) {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (isAdded()) action.run();
        });
    }
}