package com.example.optica.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.optica.R;
import com.example.optica.ui.appointment.AppointmentFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.analytics.FirebaseAnalytics;

public class ProductDetailFragment extends Fragment {

    private String productId, name, price, imageUrl;
    private String selectedColor = "Negro";
    private ChipGroup sizeGroup, lensGroup;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static ProductDetailFragment newInstance(String id, String name, String price, String imageUrl) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("name", name);
        args.putString("price", price);
        args.putString("imageUrl", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("id");
            name = getArguments().getString("name");
            price = getArguments().getString("price");
            imageUrl = getArguments().getString("imageUrl");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        TextView tvName = view.findViewById(R.id.detailProductName);
        TextView tvPrice = view.findViewById(R.id.detailProductPrice);
        ImageView ivProduct = view.findViewById(R.id.detailProductImage);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        sizeGroup = view.findViewById(R.id.sizeChipGroup);
        lensGroup = view.findViewById(R.id.lensChipGroup);

        tvName.setText(name);
        tvPrice.setText(price);
        
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        logProductView();

        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(ivProduct);

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        setupColorSelection(view);

        view.findViewById(R.id.btnBookWithSpecs).setOnClickListener(v -> 
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AppointmentFragment())
                    .addToBackStack(null)
                    .commit());

        view.findViewById(R.id.btnBuyNow).setOnClickListener(v -> navigateToCheckout());

        return view;
    }

    private void setupColorSelection(View view) {
        View cBlack = view.findViewById(R.id.colorBlack);
        View cBrown = view.findViewById(R.id.colorBrown);
        View cBlue = view.findViewById(R.id.colorBlue);

        cBlack.setOnClickListener(v -> selectedColor = "Negro");
        cBrown.setOnClickListener(v -> selectedColor = "Café");
        cBlue.setOnClickListener(v -> selectedColor = "Azul");
    }

    private void navigateToCheckout() {
        String size = getSelectedChipText(sizeGroup);
        String lens = getSelectedChipText(lensGroup);

        CheckoutFragment checkout = CheckoutFragment.newInstance(
                productId, name, price, imageUrl, selectedColor, size, lens
        );

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, checkout)
                .addToBackStack(null)
                .commit();
    }

    private void logProductView() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, productId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.PRICE, price);
        mFirebaseAnalytics.logEvent("ver_producto", bundle);
    }

    private String getSelectedChipText(ChipGroup group) {
        int id = group.getCheckedChipId();
        if (id != View.NO_ID) {
            Chip chip = group.findViewById(id);
            return chip.getText().toString();
        }
        return "N/A";
    }
}
