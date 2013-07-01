package com.bklimt.candelabra;

import com.bklimt.candelabra.R;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LightsActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.lights);

    Button setupButton = (Button) findViewById(R.id.setup_button);
    setupButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent("candelabra.intent.action.SETUP"));
      }
    });

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
}
