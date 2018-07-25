package com.cuneytayyildiz.android.consent.sdk.helper

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import com.cuneytayyildiz.android.consent.sdk.helper.callbacks.ConsentCallback
import com.cuneytayyildiz.android.consent.sdk.helper.callbacks.ConsentInformationCallback
import com.cuneytayyildiz.android.consent.sdk.helper.callbacks.ConsentStatusCallback
import com.cuneytayyildiz.android.consent.sdk.helper.callbacks.LocationIsEeaOrUnknownCallback
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import java.net.MalformedURLException
import java.net.URL


class ConsentSDKHelper(private val context: Context, private val publisherId: String, private val privacyPolicyURL: String, private val admobTestDeviceId: String = "", private var DEBUG: Boolean = false, private var isEEA: Boolean = false) {

    private lateinit var form: ConsentForm

    companion object {
        private const val ads_preference = "consent_sdk_ads_preference"
        private const val user_preference = "consent_sdk_user_preference"
        const val PERSONALIZED = true
        const val NON_PERSONALIZED = false

        fun isConsentPersonalized(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getBoolean(ads_preference, PERSONALIZED)
        }

        fun isUserLocationWithinEea(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getBoolean(user_preference, false)
        }

        fun setConsentPersonalized(context: Context, value: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context.applicationContext).edit().putBoolean(ads_preference, value).apply()
        }

        fun setUserLocationWithinEea(context: Context, value: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context.applicationContext).edit().putBoolean(user_preference, value).apply()
        }

        fun getAdRequest(context: Context, isDebug: Boolean = false, admobTestDeviceId: String = ""): AdRequest {
            if (isConsentPersonalized(context)) {
                return when {
                    isDebug -> AdRequest.Builder()
                            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                            .addTestDevice(admobTestDeviceId)
                            .build()
                    else -> AdRequest.Builder()
                            .build()
                }
            } else {
                return when {
                    isDebug -> AdRequest.Builder()
                            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                            .addTestDevice(admobTestDeviceId)
                            .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                            .build()
                    else -> AdRequest.Builder()
                            .addNetworkExtrasBundle(AdMobAdapter::class.java, getNonPersonalizedAdsBundle())
                            .build()
                }
            }
        }

        private fun getNonPersonalizedAdsBundle(): Bundle {
            return Bundle().apply {
                putString("npa", "1")
            }
        }
    }

    fun checkConsent(callback: ConsentCallback) {
        initConsentInformation(object : ConsentInformationCallback {
            override fun onResult(consentInformation: ConsentInformation, consentStatus: ConsentStatus?) {
                when (consentStatus) {
                    ConsentStatus.UNKNOWN -> {
                        if (consentInformation.isRequestLocationInEeaOrUnknown) {
                            requestConsent(object : ConsentStatusCallback {
                                override fun onResult(isRequestLocationInEeaOrUnknown: Boolean, isConsentPersonalized: Boolean) {
                                    callback.onResult(isRequestLocationInEeaOrUnknown)
                                }
                            })
                        } else {
                            setConsentPersonalized(context, PERSONALIZED)
                            callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown)
                        }
                    }
                    ConsentStatus.NON_PERSONALIZED -> {
                        setConsentPersonalized(context, NON_PERSONALIZED)
                        callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown)
                    }
                    else -> {
                        setConsentPersonalized(context, PERSONALIZED)
                        callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown)
                    }
                }

                setUserLocationWithinEea(context, consentInformation.isRequestLocationInEeaOrUnknown)
            }

            override fun onFailed(consentInformation: ConsentInformation, reason: String?) {
                setUserLocationWithinEea(context, consentInformation.isRequestLocationInEeaOrUnknown)

                callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown)
            }
        })
    }

    fun requestConsent(callback: ConsentStatusCallback?) {
        var privacyUrl: URL? = null
        try {
            privacyUrl = URL(privacyPolicyURL)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }

        form = ConsentForm.Builder(context, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        form.show()
                    }

                    override fun onConsentFormError(reason: String?) {
                        isRequestLocationIsEeaOrUnknown(object : LocationIsEeaOrUnknownCallback {
                            override fun onResult(isRequestLocationInEeaOrUnknown: Boolean) {
                                callback?.onResult(isRequestLocationInEeaOrUnknown, false)
                            }
                        })
                    }

                    override fun onConsentFormOpened() {}

                    override fun onConsentFormClosed(consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                        val isConsentPersonalized = when (consentStatus) {
                            ConsentStatus.NON_PERSONALIZED -> NON_PERSONALIZED
                            else -> PERSONALIZED
                        }

                        setConsentPersonalized(context, isConsentPersonalized)

                        isRequestLocationIsEeaOrUnknown(object : LocationIsEeaOrUnknownCallback {
                            override fun onResult(isRequestLocationInEeaOrUnknown: Boolean) {
                                callback?.onResult(isRequestLocationInEeaOrUnknown, isConsentPersonalized)
                            }
                        })
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build()
        form.load()
    }

    private fun isRequestLocationIsEeaOrUnknown(callback: LocationIsEeaOrUnknownCallback) {
        initConsentInformation(object : ConsentInformationCallback {
            override fun onResult(consentInformation: ConsentInformation, consentStatus: ConsentStatus?) {
                callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown)
            }

            override fun onFailed(consentInformation: ConsentInformation, reason: String?) {
                callback.onResult(false)
            }
        })
    }

    private fun initConsentInformation(callback: ConsentInformationCallback) {
        val consentInformation = ConsentInformation.getInstance(context.applicationContext)
        if (DEBUG) {
            if (admobTestDeviceId.isNotBlank()) {
                consentInformation.addTestDevice(admobTestDeviceId)
            }

            consentInformation.debugGeography = if (isEEA) DebugGeography.DEBUG_GEOGRAPHY_EEA else DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA
        }

        val publisherIds = arrayOf(publisherId)
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                callback.onResult(consentInformation, consentStatus)
            }

            override fun onFailedToUpdateConsentInfo(reason: String) {
                callback.onFailed(consentInformation, reason)
            }
        })
    }
}