package com.cuneytayyildiz.android.consent.sdk.helper.preference


import android.content.Context
import android.os.Build
import android.support.v7.app.AlertDialog
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.PreferenceViewHolder
import android.text.Html
import android.text.Spanned
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import com.cuneytayyildiz.android.consent.sdk.helper.ConsentSDKHelper
import com.cuneytayyildiz.android.consent.sdk.helper.R
import com.cuneytayyildiz.android.consent.sdk.helper.callbacks.ConsentStatusCallback

class ConsentSDKHelperPreference
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : CheckBoxPreference(context, attrs, defStyleAttr) {

    private var consentSDKHelper: ConsentSDKHelper? = null
    private var publisherId: String? = null
    private var privacyPolicyURL: String? = null
    private var titleTextSize: Float

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConsentSDKHelperPreference)
        publisherId = typedArray.getString(R.styleable.ConsentSDKHelperPreference_csdk_publisher_id)
        privacyPolicyURL = typedArray.getString(R.styleable.ConsentSDKHelperPreference_csdk_privacy_policy_url)
        titleTextSize = typedArray.getDimension(R.styleable.ConsentSDKHelperPreference_csdk_title_size, 16f)
        typedArray.recycle()

        publisherId?.let { publisherId ->
            privacyPolicyURL?.let { privacyPolicyURL ->
                consentSDKHelper = ConsentSDKHelper(context, publisherId, privacyPolicyURL)
            }
        }
                ?: run { Log.e("ConsentSDKHelperPref", "Publisher ID or Privacy Policy URL is missing in preference tags. (csdk_publisher_id / csdk_privacy_policy_url)") }

        widgetLayoutResource = R.layout.preference_switch_layout
        title = context.getString(R.string.preference_opt_out_title)
        summary = context.getString(R.string.preference_opt_out_summary)
    }

    override fun onAttached() {
        super.onAttached()
        isChecked = !ConsentSDKHelper.isConsentPersonalized(context.applicationContext)
    }

    override fun onClick() {
        if (ConsentSDKHelper.isConsentPersonalized(context)) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.preference_opt_out_confirmation_dialog_title)
                    .setMessage(context.getString(R.string.preference_opt_out_confirmation_dialog_message).toHtml())
                    .setPositiveButton(R.string.preference_opt_out_confirmation_dialog_ok) { dialog, _ ->
                        dialog.dismiss()

                        requestConsent()

                    }
                    .setNegativeButton(R.string.preference_opt_out_confirmation_dialog_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        } else {
            requestConsent()
        }
    }

    private fun requestConsent() {
        consentSDKHelper?.requestConsent(object : ConsentStatusCallback {
            override fun onResult(isRequestLocationInEeaOrUnknown: Boolean, isConsentPersonalized: Boolean) {
                isChecked = !isConsentPersonalized
            }
        })
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        (holder?.findViewById(android.R.id.title) as TextView).textSize = titleTextSize
    }

    fun String.toHtml(): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(this)
        }
    }
}