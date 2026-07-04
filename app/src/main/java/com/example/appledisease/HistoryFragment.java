package com.example.appledisease;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView     tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        tvEmpty      = view.findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.btnClearAll).setOnClickListener(v -> confirmClearAll());
        loadHistory();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        new Thread(() -> {
            List<DetectionHistory> list = AppDatabase.getInstance(requireContext())
                    .historyDao().getAllHistory();
            requireActivity().runOnUiThread(() -> {
                if (list.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(new HistoryAdapter(list, this::deleteItem));
                }
            });
        }).start();
    }

    private void deleteItem(DetectionHistory item) {
        new Thread(() -> {
            AppDatabase.getInstance(requireContext()).historyDao().delete(item);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show();
                loadHistory();
            });
        }).start();
    }

    private void confirmClearAll() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all history?")
                .setPositiveButton("Yes", (d, w) -> {
                    new Thread(() -> {
                        AppDatabase.getInstance(requireContext()).historyDao().deleteAll();
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "History cleared!", Toast.LENGTH_SHORT).show();
                            loadHistory();
                        });
                    }).start();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
