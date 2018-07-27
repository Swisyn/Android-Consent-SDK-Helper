package com.cuneytayyildiz.android.consent.sdk.helper.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.PreferenceFragmentCompat
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
            val adChoicePreference = findPreference("preference_ad_choice") as CheckBoxPreference

            if (!ConsentSDKHelper.isUserLocationWithinEea(it)) {
                adChoicePreference.isVisible = false
            }
        }
    }
}
