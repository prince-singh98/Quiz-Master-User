package app.example.myquizapp;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

public class CatGridAdapter extends BaseAdapter {

    private List<CategoryModel> catList;

    public CatGridAdapter(List<CategoryModel> catList) {
        this.catList = catList;
    }

    @Override
    public int getCount() {
        return catList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View view;

        if (convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout,parent,false);
        }else {
            view = convertView;
        }

        //intent to go to setsActivity after category item oClick...
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SplashActivity.selectedCategoryIndex = position;
                Intent intent = new Intent(parent.getContext(),SetsActivity.class);
                parent.getContext().startActivity(intent);
            }
        });



        ((TextView) view.findViewById(R.id.catName)).setText(catList.get(position).getName());

        //for random colors
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
        view.setBackgroundColor(color);

        return view;
    }
}
