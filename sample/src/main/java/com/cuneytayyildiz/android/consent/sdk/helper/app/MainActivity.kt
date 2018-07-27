package com.cuneytayyildiz.android.consent.sdk.helper.app

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.cuneytayyildiz.android.consent.sdk.helper.ConsentSDKHelper
import com.cuneytayyildiz.android.consent.sdk.helper.callbacks.ConsentCallback
import com.cuneytayyildiz.android.consent.sdk.helper.callbacks.ConsentStatusCallback

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var labelTextView: TextView
    private lateinit var buttonChangeConsentSettings: Button
    private lateinit var buttonOpenSettings: Button
    private lateinit var checkBoxEuRegion: CheckBox

    private lateinit var publisherId: String
    private lateinit var policyURL: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        labelTextView = findViewById(R.id.label_gdpr_status)
        buttonChangeConsentSettings = findViewById(R.id.button_change_consent_settings)
        buttonOpenSettings = findViewById(R.id.button_open_settings)
        checkBoxEuRegion = findViewById(R.id.checkbox_eu_region)
        checkBoxEuRegion.isChecked = PreferenceManager.getDefaultSharedPreferences(this@MainActivity).getBoolean("isEEA", true)

        publisherId = getString(R.string.csdk_admob_publisher_id)
        policyURL = getString(R.string.csdk_privacy_policy_url)

        val consentSDK = ConsentSDKHelper(this, publisherId, policyURL, "", BuildConfig.DEBUG, checkBoxEuRegion.isChecked)
        consentSDK.checkConsent(object : ConsentCallback {
            override fun onResult(isRequestLocationInEeaOrUnknown: Boolean) {
                labelTextView.text = getString(R.string.user_within_eea_text, getPersonalizedOrNotSuffix(ConsentSDKHelper.isConsentPersonalized(this@MainActivity)))
            }
        })

        buttonChangeConsentSettings.setOnClickListener {
            consentSDK.requestConsent(object : ConsentStatusCallback {
                override fun onResult(isRequestLocationInEeaOrUnknown: Boolean, isConsentPersonalized: Boolean) {
                    if (isRequestLocationInEeaOrUnknown) {
                        labelTextView.text = getString(R.string.user_within_eea_text, getPersonalizedOrNotSuffix(isConsentPersonalized))
                    } else {
                        labelTextView.text = getString(R.string.user_not_within_eea_text)
                    }
                }
            })
        }

        checkBoxEuRegion.setOnCheckedChangeListener { _, b -> PreferenceManager.getDefaultSharedPreferences(this@MainActivity).edit().putBoolean("isEEA", b).apply() }
        buttonOpenSettings.setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }

    }

    fun getPersonalizedOrNotSuffix(result: Boolean): String {
        return if (result) "Personalized" else "Not Personalized"
    }
}
