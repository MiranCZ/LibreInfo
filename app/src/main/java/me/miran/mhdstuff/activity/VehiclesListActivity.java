package me.miran.mhdstuff.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import me.miran.mhdstuff.R;
import me.miran.mhdstuff.VehicleItemAdapter;
import me.miran.mhdstuff.activity.listview.AbstractListViewActivity;
import me.miran.mhdstuff.parsing.storage.IdStorage;
import me.miran.mhdstuff.parsing.storage.LineStorage;
import me.miran.mhdstuff.parsing.types.Vehicle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VehiclesListActivity extends AbstractListViewActivity {

    private VehicleItemAdapter adapter;
    private List<Vehicle> vehicles;
    private IdStorage storage;
    private final SortInfo sortInfo = new SortInfo();

    public VehiclesListActivity() {
        super(R.string.vehicles, R.layout.activity_vehicles, R.id.recycler_view);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO add showing animation
        addButtonIcon(R.drawable.adjustments, v -> {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View popup = inflater.inflate(R.layout.vehicles_adjust_popup, findViewById(R.id.recycler_view), false);
            sortInfo.setChecked(popup);

            PopupWindow pw = new PopupWindow(popup,
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

            pw.showAtLocation(findViewById(R.id.recycler_view), Gravity.CENTER, 0, 0);

            pw.setOnDismissListener(() -> {
                if (sortInfo.update(popup)) {
                    updateSort();
                }
            });

            View container = (View) pw.getContentView().getParent();
            Context context = pw.getContentView().getContext();

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
            p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            p.dimAmount = 0.75f;
            wm.updateViewLayout(container, p);
        });
    }

    private void updateSort() {
        final int multiplier = sortInfo.ascending ? 1 : -1;
        LineStorage lineStorage = storage.lineStorage();

        switch (sortInfo.sortBy) {

            case LINE -> vehicles.sort(Comparator.comparing(v -> v.line().getSortKey(lineStorage) * multiplier));
            case VEHICLE_NUM -> vehicles.sort(Comparator.comparing(v -> v.id() * multiplier));
            case COURSE -> vehicles.sort(Comparator.comparing(v -> v.course() * multiplier));
            case DELAY -> {
                vehicles.sort(Comparator.comparing(v -> v.line().getSortKey(lineStorage) * multiplier));
                vehicles.sort(Comparator.comparing(v -> v.delay() * multiplier));
            }
        }

        RecyclerView recyclerView = findViewById(recycleViewId);;
        adapter.notifyDataSetChanged();

        runOnUiThread(() -> recyclerView.scrollToPosition(0));
    }

    private static class SortInfo {

        SortBy sortBy = SortBy.LINE;
        boolean ascending = true;

        boolean update(View popup) {
            SortBy newSortBy = SortBy.LINE;
            for (SortBy value : SortBy.values()) {
                if (isChecked(popup, value.id)) {
                    newSortBy = value;
                }
            }

            boolean newAscending = isChecked(popup, R.id.sort_ascending);
            boolean changed = newAscending != ascending || !newSortBy.equals(sortBy);

            ascending = newAscending;
            sortBy = newSortBy;

            return changed;
        }

        private boolean isChecked(View popup, int id) {
            RadioButton button = popup.findViewById(id);
            return button.isChecked();
        }

        private void setChecked(View popup, int id, boolean checked) {
            RadioButton button = popup.findViewById(id);
            button.setChecked(checked);
        }

        public void setChecked(View popup) {
            for (SortBy value : SortBy.values()) {
                setChecked(popup, value.id, value.equals(sortBy));
            }


            setChecked(popup, R.id.sort_ascending, ascending);
            setChecked(popup, R.id.sort_descending, !ascending);
        }

        private enum SortBy {
            LINE(R.id.sort_line_id),
            VEHICLE_NUM(R.id.sort_vehicle_num),
            COURSE(R.id.sort_course),
            DELAY(R.id.sort_delay);

            private final int id;

            SortBy(int id) {
                this.id = id;
            }
        }

    }

    @Override
    protected RecyclerView.Adapter<?> getAdapter(Context context, IdStorage storage) {
        this.storage = storage;
        // TODO implement vehicles
//        vehicles = Vehicle.parseVehicles(SoapHelper.getVehicles(), storage);
        vehicles = new ArrayList<>();
        adapter = new VehicleItemAdapter(vehicles, this);
        updateSort();

        return adapter;
    }

}
