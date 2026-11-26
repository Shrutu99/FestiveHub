
package com.shruti.festivehub;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private EditText etSearchEvent;

    private TextView tvAllCategory;
    private TextView tvTechnical;
    private TextView tvCultural;
    private TextView tvSports;
    private TextView tvWorkshop;
    private TextView tvNoEvents;
    private TextView tvViewAll;

    private LinearLayout navProfile;
    private LinearLayout layoutEvents;

    private String selectedCategory = "All";

    private FirebaseFirestore db;

    private final List<EventModel> eventList = new ArrayList<>();

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

        db = FirebaseFirestore.getInstance();

        initializeViews();

        setupBannerCarousel();

        setupCategoryListeners();

        // Open Student Profile
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    HomeActivity.this,
                    ProfileActivity.class
            );
            startActivity(intent);
        });

        // Search functionality
        setupSearch();

        // View All functionality
        tvViewAll.setOnClickListener(v -> {
            etSearchEvent.setText("");
            selectCategory("All");
            filterEvents();
        });

        // Load Admin-created events
        loadEvents();
    }

    // Load events from Firestore
    private void loadEvents() {

        tvNoEvents.setVisibility(View.GONE);
        layoutEvents.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading events...");
        loadingText.setGravity(Gravity.CENTER);
        loadingText.setPadding(10, 30, 10, 30);

        layoutEvents.addView(loadingText);

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    eventList.clear();
                    layoutEvents.removeAllViews();

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        EventModel event = new EventModel();

                        event.eventId = document.getId();

                        event.title =
                                document.getString("eventTitle");

                        event.category =
                                document.getString("eventCategory");

                        event.description =
                                document.getString("eventDescription");

                        event.date =
                                document.getString("eventDate");

                        event.time =
                                document.getString("eventTime");

                        event.venue =
                                document.getString("eventVenue");

                        eventList.add(event);
                    }

                    filterEvents();
                })
                .addOnFailureListener(e -> {

                    layoutEvents.removeAllViews();

                    tvNoEvents.setText(
                            "Unable to load events. Please try again."
                    );

                    tvNoEvents.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            HomeActivity.this,
                            "Failed to load events: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // Setup Banner Carousel
    private void setupBannerCarousel() {

        eventBannerCarousel = findViewById(R.id.eventBannerCarousel);
        bannerIndicators = findViewById(R.id.bannerIndicators);

        EventBannerAdapter adapter = new EventBannerAdapter();

        eventBannerCarousel.setAdapter(adapter);
        eventBannerCarousel.setOffscreenPageLimit(1);

        // Start near the middle for continuous sliding
        int startPosition = Integer.MAX_VALUE / 2;
        startPosition -= startPosition % bannerCount;

        eventBannerCarousel.setCurrentItem(startPosition, false);

        // Create indicator dots
        bannerIndicators.removeAllViews();

        for (int i = 0; i < bannerCount; i++) {

            View dot = new View(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dpToPx(8),
                            dpToPx(8)
                    );

            params.setMargins(
                    dpToPx(5), 0,
                    dpToPx(5), 0
            );

            dot.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.LTGRAY);

            dot.setBackground(drawable);

            bannerIndicators.addView(dot);
        }

        updateBannerIndicators(startPosition);

        // Update dots when banner changes
        eventBannerCarousel.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {

                    @Override
                    public void onPageSelected(int position) {
                        updateBannerIndicators(position);
                    }
                }
        );
    }

    // Update selected banner indicator
    private void updateBannerIndicators(int position) {

        int selectedPosition = position % bannerCount;

        for (int i = 0; i < bannerIndicators.getChildCount(); i++) {

            View dot = bannerIndicators.getChildAt(i);

            GradientDrawable drawable = new GradientDrawable();

            drawable.setShape(GradientDrawable.OVAL);

            if (i == selectedPosition) {
                drawable.setColor(Color.parseColor("#F28C28"));
            } else {
                drawable.setColor(Color.LTGRAY);
            }

            dot.setBackground(drawable);
        }
    }


    // Event data model
    private static class EventModel {

        String eventId;
        String title;
        String category;
        String description;
        String date;
        String time;
        String venue;
    }

    // Initialize views
    private void initializeViews() {

        etSearchEvent = findViewById(R.id.etSearchEvent);

        navProfile = findViewById(R.id.navProfile);

        layoutEvents = findViewById(R.id.layoutEvents);

        tvAllCategory = findViewById(R.id.tvAllCategory);
        tvTechnical = findViewById(R.id.tvTechnical);
        tvCultural = findViewById(R.id.tvCultural);
        tvSports = findViewById(R.id.tvSports);
        tvWorkshop = findViewById(R.id.tvWorkshop);

        tvNoEvents = findViewById(R.id.tvNoEvents);
        tvViewAll = findViewById(R.id.tvViewAll);
    }

    // Category click listeners
    private void setupCategoryListeners() {

        tvAllCategory.setOnClickListener(
                v -> selectCategory("All")
        );

        tvTechnical.setOnClickListener(
                v -> selectCategory("Technical")
        );

        tvCultural.setOnClickListener(
                v -> selectCategory("Cultural")
        );

        tvSports.setOnClickListener(
                v -> selectCategory("Sports")
        );

        tvWorkshop.setOnClickListener(
                v -> selectCategory("Workshop")
        );
    }

    // Change selected category
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

    // Search functionality
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

    // Filter events by category and search
    private void filterEvents() {

        layoutEvents.removeAllViews();

        String searchText = etSearchEvent.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        int visibleCount = 0;

        for (EventModel event : eventList) {

            String title = safe(event.title);
            String category = safe(event.category);
            String description = safe(event.description);
            String venue = safe(event.venue);

            boolean categoryMatches =
                    selectedCategory.equals("All")
                            || selectedCategory.equalsIgnoreCase(category);

            boolean searchMatches =
                    title.toLowerCase(Locale.ROOT).contains(searchText)
                            || category.toLowerCase(Locale.ROOT).contains(searchText)
                            || description.toLowerCase(Locale.ROOT).contains(searchText)
                            || venue.toLowerCase(Locale.ROOT).contains(searchText);

            if (categoryMatches && searchMatches) {

                addEventCard(event);
                visibleCount++;
            }
        }

        if (visibleCount == 0) {

            tvNoEvents.setText(
                    eventList.isEmpty()
                            ? "No events available yet."
                            : "No events found."
            );

            tvNoEvents.setVisibility(View.VISIBLE);

        } else {

            tvNoEvents.setVisibility(View.GONE);
        }
    }

    // Create dynamic event card
    private void addEventCard(EventModel event) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0, dpToPx(5), 0, dpToPx(15)
        );

        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(12));
        card.setCardElevation(dpToPx(3));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(0);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);

        // Event banner placeholder
        TextView banner = new TextView(this);

        banner.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(135)
                )
        );

        banner.setGravity(Gravity.CENTER);
        banner.setText(safe(event.category).toUpperCase(Locale.ROOT)
                + " EVENT");
        banner.setTextColor(Color.WHITE);
        banner.setTextSize(19);
        banner.setTypeface(null, Typeface.BOLD);

        int bannerColor = Color.parseColor("#12345A");

        if ("Cultural".equalsIgnoreCase(event.category)) {
            bannerColor = Color.parseColor("#F28C28");
        } else if ("Sports".equalsIgnoreCase(event.category)) {
            bannerColor = Color.parseColor("#287A69");
        } else if ("Workshop".equalsIgnoreCase(event.category)) {
            bannerColor = Color.parseColor("#6C63A8");
        }

        banner.setBackgroundColor(bannerColor);

        // Event information
        LinearLayout details = new LinearLayout(this);

        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(
                dpToPx(15),
                dpToPx(15),
                dpToPx(15),
                dpToPx(15)
        );

        TextView tvTitle = new TextView(this);

        tvTitle.setText(safe(event.title));
        tvTitle.setTextColor(Color.parseColor("#273043"));
        tvTitle.setTextSize(17);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvCategory = createDetailText(
                "Category: " + safe(event.category)
        );

        TextView tvDate = createDetailText(
                "Date: " + safe(event.date)
        );

        TextView tvTime = createDetailText(
                "Time: " + safe(event.time)
        );

        TextView tvVenue = createDetailText(
                "Location: " + safe(event.venue)
        );

        details.addView(tvTitle);
        details.addView(tvCategory);
        details.addView(tvDate);
        details.addView(tvTime);
        details.addView(tvVenue);

        cardContent.addView(banner);
        cardContent.addView(details);

        card.addView(cardContent);

        // Open event details
        card.setOnClickListener(v ->
                openEventDetails(event)
        );

        layoutEvents.addView(card);
    }

    // Create event detail text
    private TextView createDetailText(String value) {

        TextView textView = new TextView(this);

        textView.setText(value);
        textView.setTextColor(Color.GRAY);
        textView.setTextSize(13);
        textView.setPadding(0, dpToPx(7), 0, 0);

        return textView;
    }

    // Open Event Detail Activity
    private void openEventDetails(EventModel event) {

        Intent intent = new Intent(
                HomeActivity.this,
                EventDetailActivity.class
        );

        intent.putExtra("eventId", event.eventId);
        intent.putExtra("eventTitle", event.title);
        intent.putExtra("eventCategory", event.category);
        intent.putExtra("eventDescription", event.description);
        intent.putExtra("eventDate", event.date);
        intent.putExtra("eventTime", event.time);
        intent.putExtra("eventVenue", event.venue);

        startActivity(intent);
    }

    // Safe string handling
    private String safe(String value) {

        return value == null || value.trim().isEmpty()
                ? "Not available"
                : value;
    }

    // Convert dp to pixels
    private int dpToPx(int dp) {

        return (int) (dp * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    // Reload events when returning to Home
    @Override
    protected void onResume() {
        super.onResume();

        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, 3000);

        if (db != null && layoutEvents != null) {
            loadEvents();
        }
    }

    // Stop automatic sliding
    @Override
    protected void onPause() {
        super.onPause();

        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
