package com.wuj10n.mysimplebrowser;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HistoryActivity extends AppCompatActivity {
    MyDataBaseHelper myDataBaseHelper;
    SQLiteDatabase mDatabase;
    private ArrayList arrayList=new ArrayList();
    ArrayList<Map<String, String>> result = new ArrayList<>();
    String []URL=new String[]{};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("历史记录");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ListView listView = findViewById(R.id.ListView_history);
        myDataBaseHelper=new MyDataBaseHelper(HistoryActivity.this);
        mDatabase=myDataBaseHelper.getWritableDatabase();
        String sql = "select * from history order by _id desc";
        Cursor cursor = mDatabase.rawQuery(sql,null);

        ArrayList<Map<String, String>> listData = convertCursorToList(cursor);//将cursor数据类型转换为list
        SimpleAdapter adapter = new SimpleAdapter(HistoryActivity.this,
                listData,
                R.layout.item,
                new String[]{"title", "url"},
                new int[]{R.id.textView_title, R.id.textView_url});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent mintent=new Intent();
                mintent.putExtra("URL",result.get(i).get("url"));
                setResult(0x02,mintent);
                finish();

            }
        });
    }

    private ArrayList<Map<String, String>> convertCursorToList(Cursor cursor) {

        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put("title", cursor.getString(1));
            map.put("url", cursor.getString(2));

            arrayList.add(cursor.getString(0));
            result.add(map);
        }
        return result;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDatabase!=null){
            mDatabase.close();
        }
        if (myDataBaseHelper!=null) {
            myDataBaseHelper.close();
        }
    }
}