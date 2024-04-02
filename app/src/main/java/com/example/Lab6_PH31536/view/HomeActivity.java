package com.example.Lab6_PH31536.view;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Lab6_PH31536.adapter.FruitAdapter;
import com.example.Lab6_PH31536.databinding.ActivityHomeBinding;
import com.example.Lab6_PH31536.model.Fruit;
import com.example.Lab6_PH31536.model.Response;
import com.example.Lab6_PH31536.services.HttpRequest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class HomeActivity extends AppCompatActivity implements FruitAdapter.FruitClick {
    ActivityHomeBinding binding;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private String token;
    private FruitAdapter adapter;
    private ArrayList<Fruit> list = new ArrayList<>();

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("INFO",MODE_PRIVATE);

        token = sharedPreferences.getString("token","");

        recyclerView = binding.rcvFruit; // Initialize RecyclerView
        adapter = new FruitAdapter(this, list,this); // Khởi tạo adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Cài đặt layout manager
        recyclerView.setAdapter(adapter); // Set adapter

        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitResponse);
        userListener();
    }

    private void userListener () {
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this , AddFruitActivity.class));
            }
        });
    }

//    private void getData (ArrayList<Fruit> ds) {
//        adapter.setData(ds); // Update adapter data
//    }

    Callback<Response<ArrayList<Fruit>>> getListFruitResponse = new Callback<Response<ArrayList<Fruit>>>() {
        @Override
        public void onResponse(Call<Response<ArrayList<Fruit>>> call, retrofit2.Response<Response<ArrayList<Fruit>>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() == 200) {
                    ArrayList<Fruit> ds = response.body().getData();
                    list.clear(); // Clear existing list
                    list.addAll(ds); // Add new data to the list
                    adapter.notifyDataSetChanged(); // Notify adapter data changed
                }
            }
        }

        @Override
        public void onFailure(Call<Response<ArrayList<Fruit>>> call, Throwable t) {
            // Handle failure
            Log.e(TAG, "onFailure: " + t.getMessage());
        }
    };

    @Override
    public void delete(Fruit fruit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm delete");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            httpRequest.callAPI()
                    .deleteFruit(fruit.get_id())
                    .enqueue(new Callback<Response<Fruit>>() {
                        @Override
                        public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
                            if (response.isSuccessful()) {
                                if (response.body().getStatus() == 200) {
                                    // Xóa thành công, cập nhật lại danh sách dữ liệu và cập nhật RecyclerView
                                    list.remove(fruit);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(HomeActivity.this, response.body().getMessenger(), Toast.LENGTH_SHORT).show();
                                } else {
                                    // Xử lý lỗi từ server
                                    Toast.makeText(HomeActivity.this, "Xóa không thành công: " + response.body().getMessenger(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Xử lý lỗi khi không thể kết nối tới server
                                Toast.makeText(HomeActivity.this, "Xóa không thành công: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Response<Fruit>> call, Throwable t) {
                            // Xử lý lỗi khi gặp lỗi trong quá trình xóa
                            Toast.makeText(HomeActivity.this, "Xóa không thành công: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    @Override
    public void edit(Fruit fruit) {
        // Handle edit action here if needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitResponse);
    }
}
