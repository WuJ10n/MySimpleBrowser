package com.wuj10n.mysimplebrowser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 收藏夹
 */
public class FavouritesActivity extends AppCompatActivity {

    MyDataBaseHelper myDataBaseHelper;
    SQLiteDatabase mDatabase;
    ListView listView;

    ArrayList<Map<String, String>> result = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setTitle("收藏夹");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        listView = findViewById(R.id.ListView_favourite);
        queryinfo();//简单适配器显示列表

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent mintent=new Intent();
            mintent.putExtra("URL", result.get(i).get("url"));
            setResult(0x04,mintent);
            finish();
        });

        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {


            LayoutInflater inflater = getLayoutInflater();
            final View layout = inflater.inflate(R.layout.dialog_favourite,
                    (ViewGroup) findViewById(R.id.item_fav));
            AlertDialog.Builder inputDialog = new AlertDialog.Builder(FavouritesActivity.this);
            inputDialog.setView(layout);
            inputDialog.setNegativeButton("取消", (dialogInterface, i1) -> {
                dialogInterface.dismiss();
                Toast.makeText(FavouritesActivity.this, "关闭对话框", Toast.LENGTH_SHORT).show();
            });
           inputDialog.create().show();

            layout.findViewById(R.id.button_lookup).setOnClickListener(view1 -> {
                dialogFav_lookup(i);
                Toast.makeText(FavouritesActivity.this, "点击了修改", Toast.LENGTH_SHORT).show();
            });
            layout.findViewById(R.id.button_delete).setOnClickListener(view12 -> {
                dialogFav_delete(i);
                Toast.makeText(FavouritesActivity.this, "点击了删除本条历史记录", Toast.LENGTH_SHORT).show();
            });
            layout.findViewById(R.id.button_clear_all).setOnClickListener(view13 -> {
                dialogFav_clear();
                Toast.makeText(FavouritesActivity.this, "点击了清空收藏夹", Toast.LENGTH_SHORT).show();
            });
            return true;
        });
    }
    public void dialogFav_lookup(int position){
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_edittext,
                (ViewGroup) findViewById(R.id.item_lin_ed));
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(FavouritesActivity.this);
        inputDialog.setTitle("修改收藏的网址");
        inputDialog.setView(layout);
        EditText item_Title=layout.findViewById(R.id.item_addBookmarkTitle);
        EditText item_Url=layout.findViewById(R.id.item_addBookmarkUrl);
        Spinner category =layout.findViewById(R.id.spinner_category);
        item_Title.setText(result.get(position).get("title"));
        item_Url.setText(result.get(position).get("url"));
        inputDialog.setPositiveButton("确定", (dialogInterface, i) -> {
            myDataBaseHelper=new MyDataBaseHelper(FavouritesActivity.this);
            mDatabase=myDataBaseHelper.getWritableDatabase();
            String sql="update bookmark set title=?,url=?,category=? where url=?";
            mDatabase.execSQL(sql,
                    new Object[]{
                            item_Title.getText().toString(),
                            item_Url.getText().toString(),
                            category.getSelectedItem().toString(),
                            result.get(position).get("url")
            });
            mDatabase.close();
            queryinfo();
        });
        inputDialog.setNegativeButton("取消", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Toast.makeText(FavouritesActivity.this, "关闭对话框", Toast.LENGTH_SHORT).show();
        });
        inputDialog.create().show();
    }

    public void dialogFav_delete(int position){
        myDataBaseHelper=new MyDataBaseHelper(FavouritesActivity.this);
        mDatabase=myDataBaseHelper.getWritableDatabase();
        mDatabase.delete("bookmark","url=?",new String[] {String.valueOf(result.get(position).get("url"))});
        mDatabase.close();
        Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();

        queryinfo();
    }
    public void dialogFav_clear(){
        myDataBaseHelper=new MyDataBaseHelper(FavouritesActivity.this);
        mDatabase=myDataBaseHelper.getWritableDatabase();
        Cursor cursor = mDatabase.rawQuery("select * from bookmark", null);
        while (cursor.moveToNext()) {
            //按值查找详细信息
            String url = cursor.getString(2);
            mDatabase.delete("bookmark","url=?",new String[] {String.valueOf(url)});
        }
        cursor.close();
        mDatabase.close();
        queryinfo();
    }

    private void queryinfo() {
        final ArrayList<Map<String, String>> listData= new ArrayList<>();
        myDataBaseHelper=new MyDataBaseHelper(FavouritesActivity.this);
        mDatabase=myDataBaseHelper.getWritableDatabase();
        mDatabase.setLocale(Locale.CHINESE);

        String sql = "select * from bookmark ORDER BY category COLLATE LOCALIZED ASC";
        Cursor cursor = mDatabase.rawQuery(sql,null);

        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put("title", cursor.getString(1));
            map.put("url", cursor.getString(2));
            map.put("category",cursor.getString(3));

            result.add(map);
            listData.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(FavouritesActivity.this,
                listData,
                R.layout.item2,
                new String[]{"title", "url","category"},
                new int[]{R.id.textView_favTitle, R.id.textView_favUrl,R.id.textView_favCategory});
        listView.setAdapter(adapter);
        mDatabase.close();
        cursor.close();
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