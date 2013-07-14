package com.bklimt.candelabra.views;

import java.util.logging.Logger;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.backbone.CollectionListener;
import com.bklimt.candelabra.backbone.ModelListener;
import com.bklimt.candelabra.backbone.Visitor;
import com.bklimt.candelabra.models.Light;
import com.bklimt.candelabra.models.LightSet;
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

public class LightsActivity extends Activity implements CollectionListener<Light> {
  private Logger log = Logger.getLogger(getClass().getName());

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lights);

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
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.lights, menu);

    menu.findItem(R.id.warning).setVisible(!RootViewModel.get().isEnabled());

    RootViewModel.get().addListener(new ModelListener() {
      @Override
      public void onChanged(String key, Object value) {
        menu.findItem(R.id.warning).setVisible(!RootViewModel.get().isEnabled());
      }
    });

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
      case R.id.open: {
        startActivity(new Intent(this, PresetListActivity.class));
        return true;
      }
      case R.id.rename_light: {
        showRenameLightDialog();
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
    final PromptDialog saveDialog = new PromptDialog();
    saveDialog.setTitleText(R.string.save_preset);
    saveDialog.setPositiveText(R.string.save);
    saveDialog.setNegativeText(R.string.cancel);
    saveDialog.show(this, new Callback<DialogInterface>() {
      @Override
      public void callback(final DialogInterface dialog, Exception error) {
        if (saveDialog.getText() == null) {
          dialog.dismiss();
          return;
        }

        final String name = saveDialog.getText();
        if (RootViewModel.get().getPresets().findById(name) == null) {
          RootViewModel.get().savePreset(LightsActivity.this, name);
          dialog.dismiss();
          return;
        }

        confirmOverwrite(new Callback<Boolean>() {
          @Override
          public void callback(Boolean result, Exception error) {
            if (Boolean.TRUE.equals(result)) {
              RootViewModel.get().savePreset(LightsActivity.this, name);
              dialog.dismiss();
            }
          }
        });
      }
    });
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

  private void showRenameLightDialog() {
    LightFragment fragment = (LightFragment) getFragmentManager().findFragmentById(R.id.light);
    final Light light = fragment.getLight();
    
    final PromptDialog renameDialog = new PromptDialog();
    renameDialog.setTitleText(R.string.rename_light);
    renameDialog.setPositiveText(R.string.save);
    renameDialog.setNegativeText(R.string.cancel);
    renameDialog.setText(light.getName());
    renameDialog.show(this, new Callback<DialogInterface>() {
      @Override
      public void callback(final DialogInterface dialog, Exception error) {
        if (renameDialog.getText() == null) {
          dialog.dismiss();
          return;
        }
        String newName = renameDialog.getText();
        if (newName.length() > 32) {
          newName = newName.substring(0, 32);
        }
        light.setName(newName);
        dialog.dismiss();
      }
    });
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