package com.bklimt.candelabra.views;

import java.util.logging.Logger;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.Preset;
import com.bklimt.candelabra.models.RootViewModel;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.app.Activity;
import android.content.Intent;

public class PresetListActivity extends Activity {
  private Logger log = Logger.getLogger(getClass().getName());
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.preset_list);

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
  }
}
