package app.example.myquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;


import static app.example.myquizapp.SplashActivity.catList;

public class CategoryActivity extends AppCompatActivity {

    GridView catGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        catGrid = findViewById(R.id.categoryGridview);


        CatGridAdapter adapter = new CatGridAdapter(catList);
        catGrid.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            CategoryActivity.this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}