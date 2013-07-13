package com.bklimt.candelabra.views;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.backbone.CollectionListener;
import com.bklimt.candelabra.backbone.Visitor;
import com.bklimt.candelabra.models.Preset;
import com.bklimt.candelabra.models.PresetSet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PresetAdapter extends ArrayAdapter<Preset> implements CollectionListener<Preset> {
  public PresetAdapter(Context context, PresetSet presets) {
    super(context, R.layout.view_preset);
    presets.each(new Visitor<Preset>() {
      @Override
      public void visit(Preset model) {
        add(model);
      }
    });
    presets.addListener(this);
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {
    if (view == null) {
      view = View.inflate(getContext(), R.layout.view_preset, null);
    }

    Preset preset = getItem(position);
    
    TextView nameView = (TextView) view;
    nameView.setText(preset.getName());
    
    // Drawable deleteIcon = getContext().getResources().getDrawable(R.drawable.navigation_cancel);
    // deleteIcon.setBounds(0, 0, 64, 64);
    // nameView.setCompoundDrawables(null, null, deleteIcon, null);
    
    return view;
  }

  @Override
  public void onAdd(Preset item) {
    add(item);
  }

  @Override
  public void onRemove(Preset item) {
    remove(item);
  }
}
