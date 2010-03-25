/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase.activity;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ase.AseAnalytics;
import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.IntentBuilders;
import com.google.ase.R;
import com.google.ase.ScriptStorageAdapter;

/**
 * Presents available scripts and returns the selected one.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptPicker extends ListActivity {

  private List<File> mScriptList;
  private ScriptPickerAdapter mAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, R.layout.script_manager);
    mScriptList = ScriptStorageAdapter.listScripts(false);
    mAdapter = new ScriptPickerAdapter();
    mAdapter.registerDataSetObserver(new ScriptListObserver());
    setListAdapter(mAdapter);
    setResult(RESULT_CANCELED); // Default to canceled if we were started for result.
    AseAnalytics.trackActivity(this);
  }

  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    final File script = (File) list.getItemAtPosition(position);

    if (Intent.ACTION_PICK.equals(getIntent().getAction())) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setItems(new CharSequence[] { "Start in Terminal", "Start in Background" },
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Intent intent = null;
              if (which == 0) {
                intent = IntentBuilders.buildStartInTerminalIntent(script.getName());
              } else {
                intent = IntentBuilders.buildStartInBackgroundIntent(script.getName());
              }
              if (intent != null) {
                setResult(RESULT_OK, intent);
              } else {
                setResult(RESULT_CANCELED, null);
              }
              finish();
            }
          });
      builder.show();
      return;
    }

    if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setItems(new CharSequence[] { "Start in Terminal", "Start in Background" },
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Parcelable iconResource =
                  Intent.ShortcutIconResource
                      .fromContext(ScriptPicker.this, R.drawable.ase_logo_48);
              Intent intent = null;
              if (which == 0) {
                intent = IntentBuilders.buildTerminalShortcutIntent(script.getName(), iconResource);
              } else {
                intent =
                    IntentBuilders.buildBackgroundShortcutIntent(script.getName(), iconResource);
              }
              if (intent != null) {
                setResult(RESULT_OK, intent);
              } else {
                setResult(RESULT_CANCELED, null);
              }
              finish();
            }
          });
      builder.show();
      return;
    }

    if (com.twofortyfouram.Intent.ACTION_EDIT_SETTING.equals(getIntent().getAction())) {
      Intent intent = new Intent();
      intent.putExtra(Constants.EXTRA_SCRIPT_NAME, script.getName());
      // Set the description of the action.
      if (script.getName().length() > com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH) {
        intent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, script.getName().substring(0,
            com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH));
      } else {
        intent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, script.getName());
      }
      setResult(RESULT_OK, intent);
      AseLog.v("Returned launch intent for " + script.getName() + " to Locale: " + intent.toURI());
      finish();
      return;
    }
  }

  private class ScriptListObserver extends DataSetObserver {
    @Override
    public void onInvalidated() {
      mScriptList = ScriptStorageAdapter.listScripts(false);
    }
  }

  private class ScriptPickerAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      return mScriptList.size();
    }

    @Override
    public Object getItem(int position) {
      return mScriptList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView view = new TextView(ScriptPicker.this);
      view.setPadding(2, 2, 2, 2);
      view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
      view.setText(mScriptList.get(position).getName());
      return view;
    }
  }
}