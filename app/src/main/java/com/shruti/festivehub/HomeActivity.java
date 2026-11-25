
package com.shruti.festivehub;

import android.content.Intent;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

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

    // Carousel variables
    private ViewPager2 eventBannerCarousel;
    private LinearLayout bannerIndicators;

    private final int bannerCount = 3;

    private final Handler sliderHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (eventBannerCarousel != null) {
                int nextPosition =
                        eventBannerCarousel.getCurrentItem() + 1;

                eventBannerCarousel.setCurrentItem(nextPosition, true);

                sliderHandler.postDelayed(this, 3000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize existing views
        initializeViews();


        // Setup carousel
        setupBannerCarousel();

        // Set Click Listeners
        setupCategoryListeners();

        // Event Card Click Listeners

        cardEvent1.setOnClickListener(v -> openEventDetails(
                "Tech Fest",
                "Technical",
                "Discover technology, innovation and creativity through exciting activities and competitions.",
                "To be announced",
                "To be announced",
                "To be announced"
        ));

        cardEvent2.setOnClickListener(v -> openEventDetails(
                "Cultural Festival",
                "Cultural",
                "Celebrate creativity, art, music and cultural performances.",
                "To be announced",
                "To be announced",
                "To be announced"
        ));

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


    // PASTE STEP 6 HERE
    private void openEventDetails(
            String title,
            String category,
            String description,
            String date,
            String time,
            String venue) {

        Intent intent = new Intent(
                HomeActivity.this,
                EventDetailActivity.class
        );

        intent.putExtra("eventTitle", title);
        intent.putExtra("eventCategory", category);
        intent.putExtra("eventDescription", description);
        intent.putExtra("eventDate", date);
        intent.putExtra("eventTime", time);
        intent.putExtra("eventVenue", venue);

        startActivity(intent);
    }

    // Setup Event Banner Carousel
    private void setupBannerCarousel() {

        eventBannerCarousel =
                findViewById(R.id.eventBannerCarousel);

        bannerIndicators =
                findViewById(R.id.bannerIndicators);

        eventBannerCarousel.setAdapter(
                new EventBannerAdapter()
        );

        // Create indicator dots
        createIndicatorDots();

        // Start from the middle for infinite looping
        int startPosition = Integer.MAX_VALUE / 2;
        startPosition = startPosition - (startPosition % bannerCount);

        eventBannerCarousel.setCurrentItem(startPosition, false);

        // Update dots when banner changes
        eventBannerCarousel.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(int position) {
                        updateIndicatorDots(position);
                    }
                }
        );

        updateIndicatorDots(startPosition);
    }

    // Create three indicator dots
    private void createIndicatorDots() {

        bannerIndicators.removeAllViews();

        for (int i = 0; i < bannerCount; i++) {

            View dot = new View(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dpToPx(8),
                            dpToPx(8)
                    );

            params.setMargins(
                    dpToPx(5),
                    0,
                    dpToPx(5),
                    0
            );

            dot.setLayoutParams(params);

            bannerIndicators.addView(dot);
        }
    }

    // Highlight the currently selected dot
    private void updateIndicatorDots(int position) {

        int selectedPosition = position % bannerCount;

        for (int i = 0; i < bannerCount; i++) {

            View dot = bannerIndicators.getChildAt(i);

            GradientDrawable drawable =
                    new GradientDrawable();

            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(dpToPx(20));

            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) dot.getLayoutParams();

            if (i == selectedPosition) {

                drawable.setColor(Color.parseColor("#F28C28"));

                params.width = dpToPx(20);
                params.height = dpToPx(8);

            } else {

                drawable.setColor(Color.parseColor("#D0D0D0"));

                params.width = dpToPx(8);
                params.height = dpToPx(8);
            }

            dot.setBackground(drawable);
            dot.setLayoutParams(params);
        }
    }

    // Convert dp into pixels
    private int dpToPx(int dp) {
        return (int) (dp * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    // Start automatic sliding
    @Override
    protected void onResume() {
        super.onResume();

        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    // Stop automatic sliding when screen is not visible
    @Override
    protected void onPause() {
        super.onPause();

        sliderHandler.removeCallbacks(sliderRunnable);
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
