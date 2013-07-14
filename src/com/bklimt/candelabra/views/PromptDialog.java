package com.bklimt.candelabra.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

import com.bklimt.candelabra.util.Callback;

public class PromptDialog {
  private String text = null;
  private int titleText = android.R.string.untitled;
  private int positiveText = android.R.string.yes;
  private int negativeText = android.R.string.no;

  public PromptDialog() {
  }

  public String getText() {
    return text;
  }
  
  public PromptDialog setText(String newText) {
    text = newText;
    return this;
  }
  
  public PromptDialog setTitleText(int newTitleText) {
    titleText = newTitleText;
    return this;
  }
  
  public PromptDialog setPositiveText(int newPositiveText) {
    positiveText = newPositiveText;
    return this;
  }
  
  public PromptDialog setNegativeText(int newNegativeText) {
    negativeText = newNegativeText;
    return this;
  }

  public void show(Context context, final Callback<DialogInterface> callback) {
    final EditText editText = new EditText(context);
    editText.setText(text);
    editText.selectAll();

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(titleText);
    builder.setView(editText);

    builder.setPositiveButton(positiveText, new OnClickListener() {
      @Override
      public void onClick(final DialogInterface dialog, int which) {
        text = editText.getText().toString().trim();
        if (text.length() == 0) {
          text = null;
        }
        callback.callback(dialog, null);
      }
    });
    builder.setNegativeButton(negativeText, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        setText(null);
        callback.callback(dialog, null);
      }
    });
    builder.show();
  }
}
