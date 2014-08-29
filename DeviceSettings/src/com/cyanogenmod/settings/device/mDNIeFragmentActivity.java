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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import com.cyanogenmod.settings.device.Utils;
import com.cyanogenmod.settings.device.R;

public class mDNIeFragmentActivity extends PreferenceFragment implements OnPreferenceChangeListener {

    private mDNIeScenario mmDNIeScenario;
    private mDNIeMode mmDNIeMode;
    private mDNIeOutdoor mmDNIeOutdoor;
    private TouchscreenSensitivity mTouchscreenSensitivity;
    private CheckBoxPreference mMirroring;
    private CheckBoxPreference mRemoteDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.mdnie_preferences);
        
        Utils.initializeGSFDB();

        mmDNIeScenario = (mDNIeScenario) findPreference(DeviceSettings.KEY_MDNIE_SCENARIO);
        mmDNIeScenario.setEnabled(mDNIeScenario.isSupported());

        mmDNIeMode = (mDNIeMode) findPreference(DeviceSettings.KEY_MDNIE_MODE);
        mmDNIeMode.setEnabled(mDNIeMode.isSupported());

        mmDNIeOutdoor = (mDNIeOutdoor) findPreference(DeviceSettings.KEY_MDNIE_OUTDOOR);
        mmDNIeOutdoor.setEnabled(mDNIeOutdoor.isSupported());

        mTouchscreenSensitivity = (TouchscreenSensitivity) findPreference(DeviceSettings.KEY_TOUCHSCREEN_SENSITIVITY);
        mTouchscreenSensitivity.setEnabled(mTouchscreenSensitivity.isSupported());
        
        mMirroring = (CheckBoxPreference) findPreference(DeviceSettings.KEY_MIRRORING);
        mMirroring.setEnabled(Utils.mirroringIsSupported());
        mMirroring.setChecked(Utils.overrideEnabled(DeviceSettings.GSF_MIRRORING_ENABLED));
        mMirroring.setOnPreferenceChangeListener(this);
        
        mRemoteDisplay = (CheckBoxPreference) findPreference(DeviceSettings.KEY_REMOTE_DISPLAY);
        mRemoteDisplay.setEnabled(Utils.mirroringIsSupported());
        mRemoteDisplay.setChecked(Utils.overrideEnabled(DeviceSettings.GSF_REMOTE_DISPLAY_ENABLED));
        mRemoteDisplay.setOnPreferenceChangeListener(this);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        final boolean checkboxEnabled = newValue.toString().equals("true");
        
        if (key.equals(DeviceSettings.KEY_MIRRORING)) {
            setOverride(DeviceSettings.GSF_MIRRORING_ENABLED, checkboxEnabled);
            return true;
        }
        else if (key.equals(DeviceSettings.KEY_REMOTE_DISPLAY)) {
            setOverride(DeviceSettings.GSF_REMOTE_DISPLAY_ENABLED, checkboxEnabled);
            return true;
        }
        
        return false;
    }

}
