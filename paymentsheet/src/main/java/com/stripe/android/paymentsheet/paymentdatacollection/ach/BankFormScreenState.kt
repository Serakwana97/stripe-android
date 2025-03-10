package com.stripe.android.paymentsheet.paymentdatacollection.ach

import android.os.Parcelable
import com.stripe.android.core.strings.ResolvableString
import com.stripe.android.core.strings.resolvableString
import com.stripe.android.paymentsheet.R
import kotlinx.parcelize.Parcelize
import com.stripe.android.model.PaymentMethod as PaymentMethodModel

@Parcelize
internal data class BankFormScreenState(
    private val isPaymentFlow: Boolean,
    private val promoText: String? = null,
    private val _isProcessing: Boolean = false,
    val linkedBankAccount: LinkedBankAccount? = null,
    val error: ResolvableString? = null,
) : Parcelable {

    val isProcessing: Boolean
        get() = _isProcessing && linkedBankAccount == null

    val promoBadgeState: PromoBadgeState?
        get() = if (promoText != null && linkedBankAccount != null) {
            PromoBadgeState(promoText, eligible = linkedBankAccount.eligibleForIncentive)
        } else {
            null
        }

    val promoDisclaimerText: ResolvableString?
        get() = promoText?.let {
            if (linkedBankAccount?.eligibleForIncentive == false) {
                return null
            } else if (isPaymentFlow) {
                resolvableString(R.string.stripe_paymentsheet_bank_payment_promo_for_payment, it)
            } else {
                resolvableString(R.string.stripe_paymentsheet_bank_payment_promo_for_setup, it)
            }
        }

    fun processing(): BankFormScreenState {
        return copy(_isProcessing = true)
    }

    @Parcelize
    data class LinkedBankAccount(
        val resultIdentifier: ResultIdentifier,
        val bankName: String?,
        val last4: String?,
        val intentId: String?,
        val financialConnectionsSessionId: String?,
        val mandateText: ResolvableString,
        val isVerifyingWithMicrodeposits: Boolean,
        val eligibleForIncentive: Boolean = false,
    ) : Parcelable

    data class PromoBadgeState(
        val promoText: String,
        val eligible: Boolean,
    )

    sealed interface ResultIdentifier : Parcelable {
        @Parcelize
        data class Session(val id: String) : ResultIdentifier

        @Parcelize
        data class PaymentMethod(val paymentMethod: PaymentMethodModel) : ResultIdentifier
    }
}

internal fun BankFormScreenState.updateWithMandate(
    mandate: ResolvableString?,
): BankFormScreenState {
    return if (linkedBankAccount != null && mandate != null) {
        copy(linkedBankAccount = linkedBankAccount.copy(mandateText = mandate))
    } else {
        this
    }
}

internal fun BankFormScreenState.updateWithLinkedBankAccount(
    account: BankFormScreenState.LinkedBankAccount,
): BankFormScreenState {
    return copy(
        linkedBankAccount = account,
        _isProcessing = false,
        error = null,
    )
}
