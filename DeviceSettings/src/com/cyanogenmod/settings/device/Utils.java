/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.lang.Process;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.widget.Toast;

public class Utils {

    /**
     * Write a string value to the specified file.
     * @param filename      The filename
     * @param value         The value
     */
    public static void writeValue(String filename, String value) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Read a string value from the specified file.
     * @param filename        The filename
     */
    public static String readValue(String filename) {
        try {
            InputStream in = new FileInputStream(filename);
            InputStreamReader instr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(instr);
            String line = null;
            while ((line = reader.readLine()) != null) {
                return line.replace("\n", "");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return "";
    }

    /**
     * Write the "color value" to the specified file. The value is scaled from
     * an integer to an unsigned integer by multiplying by 2.
     * @param filename      The filename
     * @param value         The value of max value Integer.MAX
     */
    public static void writeColor(String filename, int value) {
        writeValue(filename, String.valueOf((long) value * 2));
    }
    
    /**
     * Check if mirroring is supported.
     *
     */
    public static boolean mirroringIsSupported() {
        try {
            File submixFile = new File(DeviceSettings.SUBMIX_FILE);
            
            if (submixFile.exists()){
                return true;
            }
        } catch (IOException e) {
        }
        
        return false;
    }
    
    /**
     * Initialize GSFDB for Chromecast.
     */
    public static boolean initializeGSFDB() {
        int bytesRead = 0;
        byte[] buffer = new byte[4096];
        boolean gsfMirroringEnabledExists = false;
        boolean gsfRemoteDisplayEnabledExists = false;
        
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        InputStream inputStream = su.getInputStream();
        
        outputStream.writeBytes("sqlite3 " + DeviceSettings.GSF_DB_FILE + " \"SELECT count(name) FROM " + DeviceSettings.GSF_OVERRIDES_TABLE + " WHERE name='" + DeviceSettings.GSF_MIRRORING_ENABLED + "';\"\n");
        
        while (inputStream.available() <= 0) {
            try { Thread.sleep(3000); } catch(Exception ex) {}
        }

        while (inputStream.available() > 0) {
            bytesRead = inputStream.read(buffer);
            if ( bytesRead <= 0 ) break;
            String seg = new String(buffer,0,bytesRead);   
            gsfMirroringEnabledExists = seg.equals("0") ? false : (seg.equals("1") ? true : false);
        }
        
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        su.waitFor();
        
        bytesRead = 0;
        buffer = new byte[4096];
        su = Runtime.getRuntime().exec("su");
        outputStream = new DataOutputStream(su.getOutputStream());
        inputStream = su.getInputStream();
        
        outputStream.writeBytes("sqlite3 " + DeviceSettings.GSF_DB_FILE + " \"SELECT count(name) FROM " + DeviceSettings.GSF_OVERRIDES_TABLE + " WHERE name='" + DeviceSettings.GSF_REMOTE_DISPLAY_ENABLED + "';\"\n");
        
        while (inputStream.available() <= 0) {
            try {
                Thread.sleep(3000);
            } catch (Exception ex) {
            }
        }

        while (inputStream.available() > 0) {
            bytesRead = inputStream.read(buffer);
            if (bytesRead <= 0) 
                break;
            String seg = new String(buffer,0,bytesRead);   
            gsfRemoteDisplayEnabledExists = seg.equals("0") ? false : (seg.equals("1") ? true : false);
        }
        
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        su.waitFor();
        
        if (!gsfMirroringEnabledExists || !gsfRemoteDisplayEnabledExists) {
            su = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(su.getOutputStream());
        }
        
        if (!gsfMirroringEnabledExists) {
            outputStream.writeBytes("sqlite3 " + DeviceSettings.GSF_DB_FILE + " \"INSERT INTO " + DeviceSettings.GSF_OVERRIDES_TABLE +" (name, value) VALUES ('" + DeviceSettings.GSF_MIRRORING_ENABLED + "', 'false');\"\n");
        }
        
        if (!gsfRemoteDisplayEnabledExists) {
            outputStream.writeBytes("sqlite3 " + DeviceSettings.GSF_DB_FILE + " \"INSERT INTO " + DeviceSettings.GSF_OVERRIDES_TABLE +" (name, value) VALUES ('" + DeviceSettings.GSF_REMOTE_DISPLAY_ENABLED + "', 'false');\"\n");
        }
        
        if (!gsfMirroringEnabledExists || !gsfRemoteDisplayEnabledExists) {
            outputStream = terminateApps(outputStream);
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        }
     }
     
     /**
      * Check if override is enabled.
      */
     public static boolean overrideEnabled(String name) {
        int bytesRead = 0;
        byte[] buffer = new byte[4096];
        boolean overrideEnabled = false;
        
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        InputStream inputStream = su.getInputStream();
        
        outputStream.writeBytes("sqlite3 " + DeviceSettings.GSF_DB_FILE + " \"SELECT value FROM " + DeviceSettings.GSF_OVERRIDES_TABLE + " WHERE name='" + name + "';\"\n");
        
        while (inputStream.available() <= 0) {
            try { Thread.sleep(3000); } catch(Exception ex) {}
        }

        while (inputStream.available() > 0) {
            bytesRead = inputStream.read(buffer);
            if ( bytesRead <= 0 ) break;
            String seg = new String(buffer,0,bytesRead);   
            overrideEnabled = seg.equals("false") ? false : (seg.equals("true") ? true : false);
        }
        
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        su.waitFor();
        
        return overrideEnabled;
     }
     
     /**
     * Set override value.
     */
     public static boolean setOverride(String name, boolean enabled) {
        int bytesRead = 0;
        byte[] buffer = new byte[4096];
        boolean overrideEnabled = false;
        
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        
        outputStream.writeBytes("sqlite3 " + DeviceSettings.GSF_DB_FILE + " \"UPDATE " + DeviceSettings.GSF_OVERRIDES_TABLE + " SET value='" + Boolean.toString(overrideEnabled) + "' WHERE name='" + name + "';\"\n");
        
        outputStream = terminateApps(outputStream);
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        su.waitFor();
        
        return overrideEnabled;
     }
     
     /**
     * Add force-stop commands to outputStream after changing override value.
     */
     public static DataOutputStream terminateApps(DataOutputStream outputStream) {
        outputStream.writeBytes("am force-stop " + DeviceSettings.GSF_PACKAGE + "\n");
        outputStream.writeBytes("am force-stop " + DeviceSettings.GMS_PACKAGE + "\n");
        outputStream.writeBytes("am force-stop " + DeviceSettings.CHROMECAST_PACKAGE + "\n");
        
        return outputStream;
     }

    /**
     * Check if the specified file exists.
     * @param filename      The filename
     * @return              Whether the file exists or not
     */
    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }


    public static void showDialog(Context ctx, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
              alertDialog.dismiss();
           }
        });
        alertDialog.show();
    }
    
    public static void showToast(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }
}
