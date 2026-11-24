
package com.shruti.festivehub;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearchEvent;

    private TextView tvAllCategory;
    private TextView tvTechnical;
    private TextView tvCultural;
    private TextView tvSports;
    private TextView tvWorkshop;
    private TextView tvNoEvents;
    private TextView tvViewAll;

    private MaterialCardView cardEvent1;
    private MaterialCardView cardEvent2;

    private TextView tvEventTitle1;
    private TextView tvEventTitle2;

    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Views
        initializeViews();

        // Set Click Listeners
        setupCategoryListeners();

        // Search Functionality
        setupSearch();

        // View All Functionality
        tvViewAll.setOnClickListener(v -> {
            etSearchEvent.setText("");
            selectCategory("All");
            filterEvents();
        });

        // Display All Events Initially
        filterEvents();
    }

    private void initializeViews() {

        etSearchEvent = findViewById(R.id.etSearchEvent);

        tvAllCategory = findViewById(R.id.tvAllCategory);
        tvTechnical = findViewById(R.id.tvTechnical);
        tvCultural = findViewById(R.id.tvCultural);
        tvSports = findViewById(R.id.tvSports);
        tvWorkshop = findViewById(R.id.tvWorkshop);

        tvNoEvents = findViewById(R.id.tvNoEvents);
        tvViewAll = findViewById(R.id.tvViewAll);

        cardEvent1 = findViewById(R.id.cardEvent1);
        cardEvent2 = findViewById(R.id.cardEvent2);

        tvEventTitle1 = findViewById(R.id.tvEventTitle1);
        tvEventTitle2 = findViewById(R.id.tvEventTitle2);
    }

    // Category Click Listeners
    private void setupCategoryListeners() {

        tvAllCategory.setOnClickListener(v -> selectCategory("All"));

        tvTechnical.setOnClickListener(v -> selectCategory("Technical"));

        tvCultural.setOnClickListener(v -> selectCategory("Cultural"));

        tvSports.setOnClickListener(v -> selectCategory("Sports"));

        tvWorkshop.setOnClickListener(v -> selectCategory("Workshop"));
    }

    // Change Selected Category
    private void selectCategory(String category) {

        selectedCategory = category;

        TextView[] categories = {
                tvAllCategory,
                tvTechnical,
                tvCultural,
                tvSports,
                tvWorkshop
        };

        String[] categoryNames = {
                "All",
                "Technical",
                "Cultural",
                "Sports",
                "Workshop"
        };

        for (int i = 0; i < categories.length; i++) {

            if (categoryNames[i].equals(category)) {

                categories[i].setBackgroundResource(
                        R.drawable.category_selected
                );

                categories[i].setTextColor(
                        ContextCompat.getColor(this, R.color.white)
                );

            } else {

                categories[i].setBackgroundResource(
                        R.drawable.category_unselected
                );

                categories[i].setTextColor(
                        ContextCompat.getColor(this, R.color.primary)
                );
            }
        }

        filterEvents();
    }

    // Search Events
    private void setupSearch() {

        etSearchEvent.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                filterEvents();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // Apply Category and Search Filters Together
    private void filterEvents() {

        String searchText = etSearchEvent.getText()
                .toString()
                .trim()
                .toLowerCase();

        // Event 1: Tech Fest
        boolean showEvent1 =
                matchesFilter("Tech Fest", "Technical", searchText);

        // Event 2: Cultural Festival
        boolean showEvent2 =
                matchesFilter("Cultural Festival", "Cultural", searchText);

        cardEvent1.setVisibility(
                showEvent1 ? View.VISIBLE : View.GONE
        );

        cardEvent2.setVisibility(
                showEvent2 ? View.VISIBLE : View.GONE
        );

        // Show message if no events match
        if (showEvent1 || showEvent2) {

            tvNoEvents.setVisibility(View.GONE);

        } else {

            tvNoEvents.setVisibility(View.VISIBLE);
        }
    }

    // Check Whether Event Matches Selected Filters
    private boolean matchesFilter(
            String eventName,
            String eventCategory,
            String searchText) {

        boolean categoryMatches =
                selectedCategory.equals("All")
                        || selectedCategory.equals(eventCategory);

        boolean searchMatches =
                eventName.toLowerCase().contains(searchText);

        return categoryMatches && searchMatches;
    }
}
