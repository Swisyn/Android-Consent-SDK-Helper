package com.cuneytayyildiz.android.consent.sdk.helper.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragmentCompat
import com.cuneytayyildiz.android.consent.sdk.helper.ConsentSDKHelper

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.settings)

        activity?.let {
            val adChoicePreference = findPreference("preference_ad_choice") as? CheckBoxPreference

            if (!ConsentSDKHelper.isUserLocationWithinEea(it)) {
                adChoicePreference?.isVisible = false
            }
        }
    }
}
