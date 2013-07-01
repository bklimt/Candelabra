package com.bklimt.candelabra;

import com.bklimt.candelabra.R;

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
    for (int i = 1; i <= 3; ++i) {
      final int light = i;
      actionBar.addTab(actionBar.newTab().setText("Light " + i).setTabListener(new TabListener() {
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