package com.example.and103_thanghtph31577_lab5.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.and103_thanghtph31577_lab5.R;
import com.example.and103_thanghtph31577_lab5.adapter.FruitAdapter;
import com.example.and103_thanghtph31577_lab5.databinding.ActivityHomeBinding;
import com.example.and103_thanghtph31577_lab5.model.Fruit;
import com.example.and103_thanghtph31577_lab5.model.Response;
import com.example.and103_thanghtph31577_lab5.services.HttpRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class HomeActivity extends AppCompatActivity implements FruitAdapter.FruitClick {
    ActivityHomeBinding binding;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private String token;
    private FruitAdapter adapter;
    private List<Fruit> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        list = new ArrayList<>();
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("INFO", MODE_PRIVATE);

        token = sharedPreferences.getString("token", "");
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitResponse);
        userListener();
//        edit();
    }

    private void userListener() {
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, AddFruitActivity.class));
            }
        });
    }


    Callback<Response<ArrayList<Fruit>>> getListFruitResponse = new Callback<Response<ArrayList<Fruit>>>() {
        @Override
        public void onResponse(Call<Response<ArrayList<Fruit>>> call, retrofit2.Response<Response<ArrayList<Fruit>>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() == 200) {
                    ArrayList<Fruit> ds = response.body().getData();
                    list.clear(); // Xóa dữ liệu cũ
                    list.addAll(ds);
                    getData(ds);
                }
            }
        }

        @Override
        public void onFailure(Call<Response<ArrayList<Fruit>>> call, Throwable t) {

        }
    };

    private void getData(ArrayList<Fruit> ds) {
        adapter = new FruitAdapter(this, ds, this);
        binding.rcvFruit.setAdapter(adapter);
    }

    @Override
    public void delete(Fruit fruit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc muốn xóa?");

        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HomeActivity.this, "Không xóa", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                deleteFruit(fruit);
                adapter.notifyDataSetChanged();
                dialogInterface.dismiss();

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteFruit(Fruit fruit) {
        String id = fruit.get_id();
        httpRequest.callAPI().deleteFruit(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    list.remove(fruit);

                    Toast.makeText(HomeActivity.this, "Đã xóa fruit thành công", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(HomeActivity.this, "Xóa fruit không thành công", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {

            }
        });
//
    }


    @Override
    public void edit(Fruit fruit) {
// Lấy thông tin của loại trái cây cần chỉnh sửa
        String fruitId = fruit.get_id();
        // Tạo Intent để chuyển sang EditFruitActivity
        Intent intent = new Intent(HomeActivity.this, EditFruit.class);
        // Đặt dữ liệu của loại trái cây vào Intent
        intent.putExtra("fruitId", fruitId);
        // Chuyển sang EditFruitActivity
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitResponse);
    }
}