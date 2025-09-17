package com.example.mhdstuff;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.util.FuzzySearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Stop> allItems;
    private List<Stop> filteredItems;
    private SearchView searchView;
    private FuzzySearch<Stop> search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // For the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Items");
        }

        recyclerView = findViewById(R.id.recycler_view_items);
        searchView = findViewById(R.id.search_view);

        allItems = MainActivity.storage.stopStorage().getAllStops();
        allItems.sort(Comparator.comparing(s -> s.name().toLowerCase()));
        search = new FuzzySearch<>(allItems, Stop::name);


        filteredItems = new ArrayList<>(allItems);
        System.out.println(allItems.size() + " ALL STOPS SIZE");

        adapter = new ItemAdapter(filteredItems);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(divider);

        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // You can perform an action when the user submits the search
                // For now, we'll just filter
                filterItems(query);
                return false; // Return true if you handled the action
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter as the user types
                filterItems(newText);
                return true;
            }
        });
    }

    private void filterItems(String query) {
        System.out.println("quering "+query);
        filteredItems.clear();
        if (query.isEmpty()) {

            filteredItems.addAll(allItems);
        } else {
            long millis = System.currentTimeMillis();

            List<Stop> results = search.getResults(query);
            filteredItems.addAll(results);


            System.out.println("done in "+(System.currentTimeMillis()-millis) + " ; "+results.size());
        }

        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
    }

    // Handle the Up button (back button in ActionBar)
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}