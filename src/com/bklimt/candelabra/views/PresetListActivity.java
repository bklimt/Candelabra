package com.bklimt.candelabra.views;

import java.util.logging.Logger;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.Preset;
import com.bklimt.candelabra.models.RootViewModel;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;

public class PresetListActivity extends Activity {
  private Logger log = Logger.getLogger(getClass().getName());
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preset_list);

    final PresetAdapter adapter = new PresetAdapter(this, RootViewModel.get().getPresets());
    ListView list = (ListView) findViewById(R.id.preset_list);
    list.setAdapter(adapter);

    list.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Preset preset = adapter.getItem(position);
        log.info("Set preset " + preset.getName());
        RootViewModel.get().applyPreset(preset);
        startActivity(new Intent(PresetListActivity.this, LightsActivity.class));
      }
    });
    
    list.setOnItemLongClickListener(new OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Preset preset = adapter.getItem(position);
        showDeleteDialog(preset);
        return true;
      }
    });
  }

  private void showDeleteDialog(final Preset preset) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.delete);
    builder.setIcon(R.drawable.alerts_and_states_warning);

    builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
      @Override
      public void onClick(final DialogInterface dialog, int which) {
        RootViewModel.get().deletePreset(PresetListActivity.this, preset);
        dialog.dismiss();
      }
    });
    builder.setNegativeButton(android.R.string.no, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    builder.show();
  }
}
