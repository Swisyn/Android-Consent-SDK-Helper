package com.cuneytayyildiz.android.consent.sdk.helper.callbacks

interface ConsentStatusCallback {
      fun onResult(isRequestLocationInEeaOrUnknown: Boolean, isConsentPersonalized: Boolean)
}