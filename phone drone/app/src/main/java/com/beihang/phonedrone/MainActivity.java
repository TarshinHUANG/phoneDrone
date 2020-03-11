package com.beihang.phonedrone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getName();
    private String[] titleList = {"Sensor Information", "USBSerial Test", "Start Project"};
    private String[] descList = {"Click to get sensor infomation of this phone", "Click to test USBSerial","Click to start mission"};
    private TextView titleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
        titleTv.setText(R.string.app_name);
    }

    private void initUI() {
        titleTv = (TextView) findViewById(R.id.titleTv);
        ImageButton returnBtn = (ImageButton) findViewById(R.id.returnBtn);
        ListView listView = (ListView) findViewById(R.id.listView);

        returnBtn.setOnClickListener(this);
        myAdapter adapter = new myAdapter(MainActivity.this, R.layout.list_item_main, titleList, descList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, SensorActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, USBActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.returnBtn) {
            this.finish();
        }
    }

    private class myAdapter extends BaseAdapter {
        private String[] titleList;
        private String[] descList;
        private int resourceId;

        public myAdapter(Context context, int resourceId, String[] title, String[] desc) {
            this.titleList = title;
            this.resourceId = resourceId;
            this.descList = desc;
        }

        @Override
        public int getCount() {
            return titleList.length;
        }

        @Override
        public Object getItem(int position) {
            return titleList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         *
         * @param position
         * @param convertView
         * @param parent
         * @return
         * 这里item不多于是不使用recycle机制
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(MainActivity.this).inflate(resourceId,null);
            TextView titleTv = view.findViewById(R.id.item_titleTv);
            TextView descTv = view.findViewById(R.id.item_descTv);

            titleTv.setText(titleList[position]);
            descTv.setText(descList[position]);
            return view;
        }
    }
}
