package com.bklimt.candelabra;

import com.bklimt.candelabra.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LightActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.light);

		final ColorView colorView = (ColorView) findViewById(R.id.color);
		
		EditColor colorEdit = (EditColor) findViewById(R.id.color_edit);
		colorEdit.addListener(new ColorListener() {
      @Override
      public void onColorChanged(float hue, float saturation, float value) {
        float[] hsv = { hue, saturation, value };
        colorView.setColor(HSVColor.getColor(hsv));
      }
		});
		
		Button setupButton = (Button) findViewById(R.id.setup_button);
		setupButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent("candelabra.intent.action.SETUP"));
      }
		});
	}
}
