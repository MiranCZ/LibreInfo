package com.example.mhdstuff.activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mhdstuff.ItemAdapter;
import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.activity.data.DelaysDataHolder;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.storage.StopStorage;
import com.example.mhdstuff.parsing.types.Stop;
import com.example.mhdstuff.util.Container;
import com.example.mhdstuff.util.FuzzySearch;
import com.example.mhdstuff.util.request.RequestHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private List<Stop> allItems;
    private List<Stop> filteredItems;
    private SearchView searchView;
    private FuzzySearch<Stop> search;

    private boolean favorFavourites = true;

    public SearchActivity() {
        super(R.string.departures, R.layout.activity_search);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            var delays = RequestHelper.getRouteDelays();

            runOnUiThread(() -> DelaysDataHolder.setDelays(delays));
        }).start();

        recyclerView = findViewById(R.id.recycler_view_items);
        searchView = findViewById(R.id.search_view);

        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setQueryHint("Zadejte zastávku");

        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        searchPlate.setBackgroundColor(Color.TRANSPARENT);

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        searchIcon.setColorFilter(ContextCompat.getColor(this, R.color.light_blue), PorterDuff.Mode.SRC_ATOP);

        setupHeart();

        new Thread(() -> {
            StopStorage storage = IdStorage.getStopStorage();

            allItems = new ArrayList<>(storage.getAllStops());

            allItems.sort(Comparator.comparing(s -> s.name.toLowerCase()));
            search = storage.getSearcher();

            filteredItems = new ArrayList<>(allItems);
            filteredItems.sort(Comparator.comparing(s -> s.isFavourite() ? 0 : 1)); // FIXME useless overhead

            adapter = new ItemAdapter(filteredItems, this);

            runOnUiThread(() -> recyclerView.setAdapter(adapter));
        }).start();


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        recyclerView.addItemDecoration(divider);

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

    private void setupHeart() {
        Container<View> heartFullCont = new Container<>();
        View heartEmpty = addButtonIcon(R.drawable.heart_regular, v -> {
            v.setVisibility(View.GONE);
            heartFullCont.item.setVisibility(View.VISIBLE);

            favorFavourites = true;
            runOnUiThread(this::sortAndSubmitAll);
        });

        View heartFull = addButtonIcon(R.drawable.heart_solid, v -> {
            heartEmpty.setVisibility(View.VISIBLE);
            v.setVisibility(View.GONE);

            favorFavourites = false;
            runOnUiThread(this::sortAndSubmitAll);
        }, false);

        heartFullCont.item = heartFull;

        heartEmpty.setVisibility(View.GONE);
    }

    private void filterItems(String query) {
        if (filteredItems == null) return;

        System.out.println("quering "+query);

        if (query.isEmpty()) {
            sortAndSubmitAll(true);
        } else {
            long millis = System.currentTimeMillis();

            List<Stop> results = search.getResults(query);

            List<Stop> favourite = new ArrayList<>();

            for (Iterator<Stop> iterator = results.iterator(); iterator.hasNext(); ) {
                Stop result = iterator.next();
                if (result.isFavourite() && favorFavourites) {
                    favourite.add(result);
                    iterator.remove();
                }
            }
            results.addAll(0, favourite);

            adapter.submitList(results);

            System.out.println("done in "+(System.currentTimeMillis()-millis) + " ; "+results.size());
        }

//        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // FIXME this is not the most efficient
        if (adapter != null) {
            // in case favourite stop was edited

            String query = searchView.getQuery().toString();
            sortAndSubmitAll(false);

            filterItems(query);
            adapter.notifyDataSetChanged();
        }
    }

    private void sortAndSubmitAll() {
        sortAndSubmitAll(false);
    }

    private void sortAndSubmitAll(boolean hardUpdate) {
        List<Stop> favourite = new ArrayList<>();
        List<Stop> rest = new ArrayList<>();

        for (Stop stop : allItems) {
            if (stop.isFavourite()) {
                if (favorFavourites) favourite.add(stop);
            } else {
                rest.add(stop);
            }
        }

        System.out.println("SORTED "+ favourite.size() + " ; "+rest.size());
        System.out.println(rest.getFirst());
        rest.addAll(0, favourite);

        if (hardUpdate) {
            adapter.submitListAndReload(rest);
        } else {
            adapter.submitList(rest);
        }
    }

}