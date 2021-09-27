package app.example.myquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class SplashActivity extends AppCompatActivity {
    private TextView appName;
    public static List<CategoryModel> catList = new ArrayList<>();
    public static int selectedCategoryIndex = 0;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        appName = findViewById(R.id.appName);

        //font variable
        Typeface typeface = ResourcesCompat.getFont(this,R.font.blacklist);
        appName.setTypeface(typeface);

        Animation anim = AnimationUtils.loadAnimation(this,R.anim.myanim);
        appName.setAnimation(anim);

        firestore = FirebaseFirestore.getInstance();

        //going to mainActivity
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadData();

            }
        }).start();
    }

    private void loadData() {
        catList.clear();//to clear already available data if present on start


        firestore.collection("quiz").document("Categories")
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    //categories document
                    DocumentSnapshot doc = task.getResult();

                    if (doc.exists()){
                        long count = (long)doc.get("COUNT");

                        for (int i=1; i<=count; i++){
                            String catName = doc.getString("CAT" + String.valueOf(i) + "_NAME");
                            String catId = doc.getString("CAT" + String.valueOf(i) + "_ID");

                            catList.add(new CategoryModel(catId , catName));

                        }

                        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }else{
                        //that means no category available
                        Toast.makeText(SplashActivity.this, "No Category, Document exists.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else{
                    Toast.makeText(SplashActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}