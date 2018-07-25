package com.cuneytayyildiz.android.consent.sdk.helper.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
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

    private val publisherId: String = ""
    private val policyURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        labelTextView = findViewById(R.id.label_gdpr_status)
        buttonChangeConsentSettings = findViewById(R.id.button_change_consent_settings)
        buttonOpenSettings = findViewById(R.id.button_open_settings)

        val consentSDK = ConsentSDKHelper(this, publisherId, policyURL, "", BuildConfig.DEBUG, true)
        consentSDK.checkConsent(object : ConsentCallback {
            override fun onResult(isRequestLocationInEeaOrUnknown: Boolean) {
                if (isRequestLocationInEeaOrUnknown) {
                    Log.e("ConsentSDKHelper", "User is from EU region")

                    labelTextView.text = getString(R.string.user_within_eea_text, getPersonalizedOrNotSuffix(ConsentSDKHelper.isUserLocationWithinEea(this@MainActivity)))

                    consentSDK.requestConsent(object : ConsentStatusCallback {
                        override fun onResult(isRequestLocationInEeaOrUnknown: Boolean, isConsentPersonalized: Boolean) {
                            labelTextView.text = getString(R.string.user_within_eea_text, getPersonalizedOrNotSuffix(isConsentPersonalized))
                        }
                    })
                } else {
                    Log.e("ConsentSDKHelper", "User is not from EU region")
                    container.removeView(buttonChangeConsentSettings)
                }
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

        buttonOpenSettings.setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }
    }

    fun getPersonalizedOrNotSuffix(result: Boolean): String {
        return if (result) "Personalized" else "Not Personalized"
    }
}
