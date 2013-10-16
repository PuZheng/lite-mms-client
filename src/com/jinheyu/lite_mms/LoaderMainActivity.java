package com.jinheyu.lite_mms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class LoaderMainActivity extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader_main_activity);

        Button buttonStartUnload = (Button) findViewById(R.id.buttonStartUnload);
        Button buttonStartLoad = (Button) findViewById(R.id.buttonStartLoad);

        buttonStartUnload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoaderMainActivity.this, SelectUnloadSessionActivity.class);
                startActivity(intent);
            }
        });

        buttonStartLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoaderMainActivity.this, SelectDeliverySessionActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.loader_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                new LogoutDialog(this).show();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(LoaderMainActivity.this, MyPreferenceActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}