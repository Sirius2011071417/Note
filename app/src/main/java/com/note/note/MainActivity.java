package com.note.note;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.note.db.NoteDb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView listview;
    private SimpleAdapter simple_adapter;
    private Button addNote;
    private NoteDb DbHelper;
    private SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitView();
    }
    @Override
    protected void onStart() {
        super.onStart();
        RefreshNotesList();
    }
    public void InitView() {
        listview = (ListView) findViewById(R.id.listview);
        addNote = (Button) findViewById(R.id.btn_editnote);
        DbHelper = new NoteDb(this);
        DB = DbHelper.getReadableDatabase();

        listview.setOnItemClickListener(this);
        listview.setOnItemLongClickListener(this);
        addNote.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("content", "");
                bundle.putInt("state", 0);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Map<String, String> map = (HashMap<String, String>) adapterView.getItemAtPosition(i);
        Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("content", map.get("content"));
        bundle.putInt("state", 1);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final int n = i;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除该日志");
        builder.setMessage("确认删除吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String, Object> map = (HashMap<String, Object>) listview.getItemAtPosition(n);
                int id = (int) map.get("id");
                DB.delete("note", "id=?", new String[]{id+""});
                RefreshNotesList();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create();
        builder.show();
        return true;
    }

    public void RefreshNotesList() {

        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        /*
        int size = dataList.size();
        if (size > 0) {
            //每次清空,并更新simp_adapter
            dataList.removeAll(dataList);
            simple_adapter.notifyDataSetChanged();
        }
        */
        //从数据库读取最新信息
        Cursor cursor = DB.query("note", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("content"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("content", name);
            map.put("short_content", name.length()<=10?name:name.substring(0,10));
            map.put("date", date);
            dataList.add(map);
        }
        simple_adapter = new SimpleAdapter(this, dataList, R.layout.note_item,
                new String[]{"short_content", "date"}, new int[]{
                R.id.content, R.id.date});
        listview.setAdapter(simple_adapter);
    }
}
