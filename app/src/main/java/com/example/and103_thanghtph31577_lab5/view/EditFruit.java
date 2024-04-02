package com.example.and103_thanghtph31577_lab5.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.and103_thanghtph31577_lab5.R;
import com.example.and103_thanghtph31577_lab5.databinding.ActivityEditFruitBinding;
import com.example.and103_thanghtph31577_lab5.model.Distributor;
import com.example.and103_thanghtph31577_lab5.model.Fruit;
import com.example.and103_thanghtph31577_lab5.model.Response;
import com.example.and103_thanghtph31577_lab5.services.HttpRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class EditFruit extends AppCompatActivity {
    ActivityEditFruitBinding binding;

    private HttpRequest httpRequest;
    private String fruitId;
    private ArrayList<File> ds_image;
    private String id_Distributor;
    private ArrayList<Distributor> distributorArrayList;

    Distributor distributor;
//    private Uri currentImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditFruitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ds_image = new ArrayList<>();
        httpRequest = new HttpRequest();

        // Lấy fruitId từ Intent
        fruitId = getIntent().getStringExtra("fruitId");

        // Gọi phương thức để lấy thông tin chi tiết của trái cây từ API
        getFruitDetail(fruitId);


        binding.avatar.setOnClickListener(v -> openGallery());


        binding.btnRegister.setOnClickListener(v -> submitEditFruit());
    }




    private void getFruitDetail(String fruitId) {
        httpRequest.callAPI().getFruitDetail(fruitId).enqueue(new Callback<Response<Fruit>>() {
            @Override
            public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
                if (response.isSuccessful()) {
                    Fruit fruit = response.body().getData();
                    // Hiển thị thông tin chi tiết của trái cây lên giao diện
                    displayFruitDetail(fruit);
                    configSpinner();
                } else {
                    // Hiển thị thông báo lỗi nếu không thành công
                    Toast.makeText(EditFruit.this, "Failed to get fruit detail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response<Fruit>> call, Throwable t) {
                Toast.makeText(EditFruit.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e("EditFruit", "onFailure: " + t.getMessage());
            }
        });
    }

    // Phương thức để hiển thị thông tin chi tiết của trái cây lên giao diện
    private void displayFruitDetail(Fruit fruit) {
        // Hiển thị thông tin chi tiết của trái cây lên giao diện
        binding.edName.setText(fruit.getName());
        binding.edQuantity.setText(fruit.getQuantity());
        binding.edPrice.setText(fruit.getPrice());
        binding.edStatus.setText(fruit.getStatus());
        binding.edDescription.setText(fruit.getDescription());

        String url  = fruit.getImage().get(0);
        String newUrl = url.replace("localhost", "10.0.2.2");
        if (fruit.getImage() != null && !fruit.getImage().isEmpty()) {
            Glide.with(EditFruit.this)
                    .load(newUrl)
                    .thumbnail(Glide.with(EditFruit.this).load(R.drawable.baseline_broken_image_24))
                    .into(binding.avatar);

        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        getImage.launch(intent);
    }

    // Xử lý kết quả trả về từ việc chọn ảnh
    ActivityResultLauncher<Intent> getImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Uri tempUri = null;
                        Intent data = o.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                File file = createFileFromUri(imageUri, "image" + i);
                                // Chèn ảnh vào vị trí đầu tiên của mảng
                                tempUri = imageUri;
                                ds_image.clear();
                                ds_image.add(file);
                            }
                        } else if (data.getData() != null) {
                            Uri imageUri = data.getData();
                            File file = createFileFromUri(imageUri, "image");
                            tempUri = imageUri;
                            // Chèn ảnh vào vị trí đầu tiên của mảng
                            ds_image.clear();
                            ds_image.add(file);
                        }

                            // Hiển thị ảnh đại diện của trái cây bằng ảnh mới
                            if (tempUri != null) {
                                Glide.with(EditFruit.this)
                                        .load(ds_image.get(0))
                                        .thumbnail(Glide.with(EditFruit.this).load(R.drawable.baseline_broken_image_24))
                                        .centerCrop()
                                        .circleCrop()
                                        .skipMemoryCache(true)
                                        .into(binding.avatar);

                        }
                    }
                }
            });

    private File createFileFromUri(Uri path, String name) {
        File _file = new File(EditFruit.this.getCacheDir(), name + ".png");
        try {
            InputStream in = EditFruit.this.getContentResolver().openInputStream(path);
            OutputStream out = new FileOutputStream(_file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            Log.d("123123", "createFileFormUri: " + _file);
            return _file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Xử lý logic khi người dùng nhấn vào nút "Submit"
    private void submitEditFruit() {

        // Lấy thông tin được chỉnh sửa từ các trường EditText
        String name = binding.edName.getText().toString();
        String quantity = binding.edQuantity.getText().toString();
        String price = binding.edPrice.getText().toString();
        String status = binding.edStatus.getText().toString();
        String description = binding.edDescription.getText().toString();


        Fruit updatedFruit = new Fruit( name, quantity, status, price, description, distributor, "aaaaa", "aaaaa");

        ArrayList<MultipartBody.Part> _ds_image = new ArrayList<>();
        ds_image.forEach(file1 -> {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"),file1);
            MultipartBody.Part multipartBodyPart = MultipartBody.Part.createFormData("image", file1.getName(),requestFile);
            _ds_image.add(multipartBodyPart);
        });
        httpRequest.callAPI().updateFruit(fruitId,updatedFruit, _ds_image).enqueue(responseFruit);


        finish();
    }
    Callback<Response<Fruit>> responseFruit = new Callback<Response<Fruit>>() {
        @Override
        public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
            if (response.isSuccessful()) {
                Log.d("123123", "onResponse: " + response.body().getStatus());
                if (response.body().getStatus()==200) {
                    Toast.makeText(EditFruit.this, "Update  thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

        @Override
        public void onFailure(Call<Response<Fruit>> call, Throwable t) {
            Toast.makeText(EditFruit.this, "Update  thành công", Toast.LENGTH_SHORT).show();
            Log.e("zzzzzzzzzz", "onFailure: "+t.getMessage());
        }
    };
    private void configSpinner() {
        httpRequest.callAPI().getListDistributor().enqueue(getDistributorAPI);
        binding.spDistributor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                id_Distributor = distributorArrayList.get(position).getId();
                distributor = distributorArrayList.get(position);
                Log.d("123123", "onItemSelected: " + id_Distributor);
                // Kiểm tra nếu không phải là do người dùng chọn mới
                binding.spDistributor.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    Callback<Response<ArrayList<Distributor>>> getDistributorAPI = new Callback<Response<ArrayList<Distributor>>>() {
        @Override
        public void onResponse(Call<Response<ArrayList<Distributor>>> call, retrofit2.Response<Response<ArrayList<Distributor>>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() == 200) {
                    distributorArrayList = response.body().getData();
                    String[] items = new String[distributorArrayList.size()];

                    for (int i = 0; i< distributorArrayList.size(); i++) {
                        items[i] = distributorArrayList.get(i).getName();
                    }
                    ArrayAdapter<String> adapterSpin = new ArrayAdapter<>(EditFruit.this, android.R.layout.simple_spinner_item, items);
                    adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding.spDistributor.setAdapter(adapterSpin);
                }
            }
        }

        @Override
        public void onFailure(Call<Response<ArrayList<Distributor>>> call, Throwable t) {
            t.getMessage();
        }

    };
}