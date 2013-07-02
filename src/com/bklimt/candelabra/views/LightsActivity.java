package com.bklimt.candelabra.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.bklimt.candelabra.CandelabraApplication;
import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.Light;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class LightsActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.lights);

    //Spinner presetsSpinner = (Spinner) findViewById(R.id.presets_spinner);
    //presetsSpinner.setAdapter(adapter);
    
    ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    Map<String, Light> lights = ((CandelabraApplication) getApplication()).getLights();
    ArrayList<String> keys = new ArrayList<String>(lights.keySet());
    Collections.sort(keys);
    for (final String key : keys) {
      final Light light = lights.get(key);
      actionBar.addTab(actionBar.newTab().setText("Light " + key).setTabListener(new TabListener() {
        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
          LightFragment fragment = (LightFragment) LightsActivity.this.getFragmentManager()
              .findFragmentById(R.id.light);
          fragment.setLight(light);
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }
      }));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.lights, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.setup: {
        startActivity(new Intent("candelabra.intent.action.SETUP"));
        return true;
      }
    }
    return false;
  }
}