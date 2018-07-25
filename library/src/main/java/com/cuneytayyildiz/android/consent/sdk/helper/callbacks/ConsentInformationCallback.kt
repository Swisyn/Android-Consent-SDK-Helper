package com.cuneytayyildiz.android.consent.sdk.helper.callbacks

import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus



interface ConsentInformationCallback {
      fun onResult(consentInformation: ConsentInformation, consentStatus: ConsentStatus?)
      fun onFailed(consentInformation: ConsentInformation, reason: String?)
}