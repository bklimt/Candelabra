package com.bklimt.candelabra.views;

import bolts.Continuation;
import bolts.Task;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SetupActivity extends Activity {
  private Button saveButton;
  private Button skipButton;
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setup);

    RootViewModel.get().bindToEditText(this, R.id.ip_address_edit, "ipAddress");
    RootViewModel.get().bindToEditText(this, R.id.user_name_edit, "userName");
    RootViewModel.get().bindToEditText(this, R.id.device_type_edit, "deviceType");

    saveButton = (Button) findViewById(R.id.save_button);
    skipButton = (Button) findViewById(R.id.skip_button);
    
    saveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onSaveClicked();
      }
    });
    
    skipButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onSkipClicked();
      }
    });
  }

  private void onSaveClicked() {
    saveButton.setEnabled(false);
    skipButton.setEnabled(false);

    final RootViewModel root = RootViewModel.get();
    root.setEnabled(true);
    root.saveDeviceSettings(SetupActivity.this);

    root.registerUsername().continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> task) throws Exception {
        startActivity(new Intent(SetupActivity.this, StartActivity.class));
        return null;
      }
    }, Task.UI_THREAD_EXECUTOR);
  }
  
  private void onSkipClicked() {
    startActivity(new Intent(SetupActivity.this, LightsActivity.class));
  }
}
