package com.cuneytayyildiz.android.consent.sdk.helper.preference

import android.content.Context
import android.os.Build
import android.support.annotation.StringRes
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

class ConsentSDKHelperPreference
@JvmOverloads
constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : CheckBoxPreference(context, attrs, defStyleAttr) {

    init {
        widgetLayoutResource = R.layout.preference_switch_layout
    }

    override fun onAttached() {
        super.onAttached()
        title = context.getString(R.string.preference_opt_out_title)
        summary = context.getString(R.string.preference_opt_out_summary)
        isChecked = !ConsentSDKHelper.isConsentPersonalized(context.applicationContext)
    }

    override fun onClick() {
        if (!isChecked) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.preference_opt_out_confirmation_dialog_title)
                    .setMessage(fromHtml(R.string.preference_opt_out_confirmation_dialog_message))
                    .setPositiveButton(R.string.preference_opt_out_confirmation_dialog_ok) { dialog, _ ->
                        dialog.dismiss()
                        isChecked = true
                        ConsentSDKHelper.setConsentPersonalized(context, ConsentSDKHelper.NON_PERSONALIZED)
                        Log.e("Ads", ConsentSDKHelper.isConsentPersonalized(context).toString())
                    }
                    .setNegativeButton(R.string.preference_opt_out_confirmation_dialog_cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
        } else {
            isChecked = false
            ConsentSDKHelper.setConsentPersonalized(context, ConsentSDKHelper.PERSONALIZED)
            Log.e("Ads", ConsentSDKHelper.isConsentPersonalized(context).toString())
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        (holder?.findViewById(android.R.id.title) as TextView).textSize = 16f
    }

    private fun fromHtml(@StringRes resId: Int): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(context.getString(resId), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(context.getString(resId))
        }
    }
}