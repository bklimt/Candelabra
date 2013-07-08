package com.bklimt.candelabra.views;

import java.util.logging.Logger;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.backbone.CollectionListener;
import com.bklimt.candelabra.backbone.ModelListener;
import com.bklimt.candelabra.backbone.Visitor;
import com.bklimt.candelabra.models.Light;
import com.bklimt.candelabra.models.LightSet;
import com.bklimt.candelabra.models.Preset;
import com.bklimt.candelabra.models.RootViewModel;
import com.bklimt.candelabra.util.Callback;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class LightsActivity extends Activity implements CollectionListener<Light> {
  private Logger log = Logger.getLogger(getClass().getName());
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.lights);

    Spinner presetsSpinner = (Spinner) findViewById(R.id.presets_spinner);
    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1);
    presetsSpinner.setAdapter(adapter);

    adapter.add("");
    
    RootViewModel.get().getPresets().each(new Visitor<Preset>() {
      @Override
      public void visit(Preset preset) {
        adapter.add(preset.getName());
      }
    });

    presetsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
        String presetName = adapter.getItem(index);
        if (presetName.equals("")) {
          return;
        }
        
        RootViewModel root = RootViewModel.get();
        Preset preset = root.getPresets().findById(presetName);
        log.info("Selected preset " + preset.getName());
        RootViewModel.get().applyPreset(preset);
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

    final ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    LightSet lights = RootViewModel.get().getLights();
    log.info("Adding tabs for all " + lights.size() + " lights.");
    lights.each(new Visitor<Light>() {
      @Override
      public void visit(Light light) {
        log.info("Starting with light " + light.getId() + " " + light.getName());
        addLight(light);
      }
    });
    log.info("Finished adding tabs for all lights.");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.lights, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.setup: {
        startActivity(new Intent(this, SetupActivity.class));
        return true;
      }
      case R.id.save: {
        showSaveDialog();
        return true;
      }
    }
    return false;
  }

  @Override
  public void onAdd(Light light) {
    log.info("Added light " + light.getId() + " " + light.getName());
    addLight(light);
  }

  @Override
  public void onRemove(Light light) {
    log.info("Removed light " + light.getId() + " " + light.getName());
  }

  private void showSaveDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.save_preset);
    
    final EditText editText = new EditText(this);
    builder.setView(editText);
    
    builder.setPositiveButton(R.string.save, new OnClickListener() {
      @Override
      public void onClick(final DialogInterface dialog, int which) {
        final String name = editText.getText().toString().trim();
        if (name.length() > 0) {
          if (RootViewModel.get().getPresets().findById(name) == null) {
            RootViewModel.get().savePreset(name);
            dialog.dismiss();
            return;
          }
          confirmOverwrite(new Callback<Boolean>() {
            @Override
            public void callback(Boolean result, Exception error) {
              if (Boolean.TRUE.equals(result)) {
                RootViewModel.get().savePreset(name);
                dialog.dismiss();
              }
            }
          });
        }
      }
    });
    builder.setNegativeButton(R.string.cancel, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    builder.show();
  }
  
  private void confirmOverwrite(final Callback<Boolean> callback) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.overwrite_preset);
    builder.setPositiveButton(R.string.save, new OnClickListener() {
      @Override
      public void onClick(final DialogInterface dialog, int which) {
        callback.callback(Boolean.TRUE, null);
        dialog.dismiss();
      }
    });
    builder.setNegativeButton(R.string.cancel, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        callback.callback(Boolean.FALSE, null);
        dialog.dismiss();
      }
    });
    builder.show();
  }
  
  private void addLight(final Light light) {
    runOnUiThread(new Runnable() {
      public void run() {

        ActionBar actionBar = getActionBar();
        final Tab tab = actionBar.newTab();
        tab.setText(light.getName());
        
        light.addListener(new ModelListener() {
          @Override
          public void onChanged(String key, Object value) {
            if (key.equals("name")) {
              tab.setText((String) value);
            }
          }
        });
        
        tab.setTabListener(new TabListener() {
          @Override
          public void onTabReselected(Tab tab, FragmentTransaction ft) {
            log.info("Reselected tab for light " + light.getId() + " " + light.getName());
            LightFragment fragment = (LightFragment) LightsActivity.this.getFragmentManager()
                .findFragmentById(R.id.light);
            fragment.setLight(light);
          }

          @Override
          public void onTabSelected(Tab tab, FragmentTransaction ft) {
            log.info("Selected tab for light " + light.getId() + " " + light.getName());
            LightFragment fragment = (LightFragment) LightsActivity.this.getFragmentManager()
                .findFragmentById(R.id.light);
            fragment.setLight(light);
          }

          @Override
          public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            log.info("Unselected tab for light " + light.getId() + " " + light.getName());
          }
        });
        actionBar.addTab(tab);
      }
    });
  }
}