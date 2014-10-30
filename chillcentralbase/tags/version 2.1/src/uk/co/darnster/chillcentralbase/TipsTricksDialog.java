/*
 * Copyright (C) 2011-2013, Karsten Priegnitz
 *
 * Permission to use, copy, modify, and distribute this piece of software
 * for any purpose with or without fee is hereby granted, provided that
 * the above copyright notice and this permission notice appear in the
 * source code of all copies.
 *
 * It would be appreciated if you mention the author in your change log,
 * contributors list or the like.
 *
 * @author: Karsten Priegnitz
 * @see: http://code.google.com/p/android-change-log/
 * 
 * 2/8/13 - Dlog added
 */

package uk.co.darnster.chillcentralbase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import uk.co.darnster.chillcentralbase.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.webkit.WebView;

public class TipsTricksDialog {

    private final Context context;
    private String title;
    
    /**
     * Constructor
     *
     * Retrieves the version names and stores the new version name in
     * SharedPreferences
     *
     * @param context
     */
    public TipsTricksDialog(Context context, String dialogTitle) {
        this.context = context;
        title = dialogTitle;
    }

 
    /**
     * @return an AlertDialog with a full change log displayed
     */
    public AlertDialog getFullTipsDialog(boolean start) {
        return this.getDialog(true);
    }

    private AlertDialog getDialog(boolean full) {  //changed visibility so I can call direct and pass in bool
    	
        WebView wv = new WebView(this.context);

        wv.setBackgroundColor(Color.BLACK);
                
        wv.loadDataWithBaseURL(null, this.getLog(full), "text/html", "UTF-8",
                null);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(
                        this.context, android.R.style.Theme_Dialog));
        builder.setTitle(title)
                .setView(wv)
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher)
                // OK button
                .setPositiveButton(
                        context.getResources().getString(
                                R.string.changelog_ok_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                    dialog.dismiss();
                            }
                        });

        return builder.create();
    }

 
    /**
     * @return HTML displaying the changes since the previous installed version
     *         of your app (what's new)
     */
    public String getLog() {
        return this.getLog(false);
    }

    /**
     * @return HTML which displays full change log
     */
    public String getFullLog() {
        return this.getLog(true);
    }

    /** modes for HTML-Lists (bullet, numbered) */
    private enum Listmode {
        NONE, ORDERED, UNORDERED,
    };

    private Listmode listMode = Listmode.NONE;
    private StringBuffer sb = null;

    private String getLog(boolean full) {
        // read changelog.txt file
        sb = new StringBuffer();
        try {
            InputStream ins = context.getResources().openRawResource(
                    R.raw.tips_tricks);
            BufferedReader br = new BufferedReader(new InputStreamReader(ins, "ISO-8859-1"));

            String line = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                char marker = line.length() > 0 ? line.charAt(0) : 0;                  
                    switch (marker) {
                    case '%':
                        // line contains version title
                        this.closeList();
                        sb.append("<div class='title'>"
                                + line.substring(1).trim() + "</div>\n");
                        break;
                    case '_':
                        // line contains version title
                        this.closeList();
                        sb.append("<div class='subtitle'>"
                                + line.substring(1).trim() + "</div>\n");
                        break;
                    case '!':
                        // line contains free text
                        this.closeList();
                        sb.append("<div class='freetext'>"
                                + line.substring(1).trim() + "</div>\n");
                        break;
                    case '#':
                        // line contains numbered list item
                        this.openList(Listmode.ORDERED);
                        sb.append("<li>" + line.substring(1).trim() + "</li>\n");
                        break;
                    case '*':
                        // line contains bullet list item
                        this.openList(Listmode.UNORDERED);
                        sb.append(line.substring(1).trim() + "<BR/><BR/>\n");
                        break;
                    default:
                        // no special character: just use line as is
                        this.closeList();
                        sb.append(line + "\n");
                    }
                
            }
            this.closeList();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private void openList(Listmode listMode) {
        if (this.listMode != listMode) {
            closeList();
            if (listMode == Listmode.ORDERED) {
                sb.append("<div class='list'><ol>\n");
            } else if (listMode == Listmode.UNORDERED) {
                sb.append("<div class='list'>");
            }
            this.listMode = listMode;
        }
    }

    private void closeList() {
        if (this.listMode == Listmode.ORDERED) {
            sb.append("</ol></div>\n");
        } else if (this.listMode == Listmode.UNORDERED) {
            sb.append("</ul></div>\n");
        }
        this.listMode = Listmode.NONE;
    }

}