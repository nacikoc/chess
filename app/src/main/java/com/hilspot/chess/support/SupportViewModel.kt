package com.hilspot.chess.support

import android.app.Activity
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams

/**
 * Uygulama içi bağış ("tip jar").
 *
 * Bağışlar tamamen isteğe bağlıdır ve HİÇBİR özelliği açmaz — uygulamanın
 * tamamı herkese ücretsizdir. Bu yüzden ürünler tüketilebilir (consumable)
 * olarak işlenir: kullanıcı isterse tekrar bağış yapabilir ve saklanacak bir
 * "satın alındı" durumu yoktur.
 *
 * Ürün kimlikleri Play Console'da bire bir aynı olmalıdır.
 */
class SupportViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        /** Play Console → Uygulama içi ürünler'de bu kimliklerle oluşturulmalı. */
        val PRODUCT_IDS = listOf("tip_small", "tip_medium", "tip_large")
    }

    sealed interface UiState {
        data object Loading : UiState
        data class Ready(val tiers: List<Tier>) : UiState
        /** Play yok, ürünler tanımsız veya bağlantı kurulamadı. */
        data object Unavailable : UiState
    }

    /** Fiyat metni doğrudan Play'den gelir; para birimi kullanıcıya göre değişir. */
    data class Tier(val id: String, val name: String, val price: String, val details: ProductDetails)

    var state by mutableStateOf<UiState>(UiState.Loading); private set
    var thanks by mutableStateOf(false); private set
    var purchasePending by mutableStateOf(false); private set

    private val purchaseListener = PurchasesUpdatedListener { result, purchases ->
        purchasePending = false
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { handlePurchase(it) }
        }
        // Kullanıcı vazgeçtiyse (USER_CANCELED) sessizce geç: bağış zorunlu değil,
        // vazgeçmek bir hata değildir.
    }

    private val client: BillingClient = BillingClient.newBuilder(app)
        .setListener(purchaseListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    init {
        connect()
    }

    private fun connect() {
        state = UiState.Loading
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    consumeLeftovers()
                } else {
                    state = UiState.Unavailable
                }
            }

            override fun onBillingServiceDisconnected() {
                state = UiState.Unavailable
            }
        })
    }

    fun retry() = connect()

    private fun queryProducts() {
        val products = PRODUCT_IDS.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        client.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(products).build()
        ) { result, details ->
            // Play Billing 8'den beri geri çağrı ProductDetails listesi yerine
            // QueryProductDetailsResult veriyor (getirilemeyen ürünleri de
            // ayrıca bildiriyor); bizi ilgilendiren getirilebilenler.
            val fetched = details.productDetailsList
            if (result.responseCode != BillingClient.BillingResponseCode.OK || fetched.isEmpty()) {
                state = UiState.Unavailable
                return@queryProductDetailsAsync
            }
            // Play'in döndürdüğü sırayı değil, kendi ucuzdan pahalıya sıramızı koru.
            val byId = fetched.associateBy { it.productId }
            val tiers = PRODUCT_IDS.mapNotNull { id ->
                val d = byId[id] ?: return@mapNotNull null
                val price = d.oneTimePurchaseOfferDetails?.formattedPrice ?: return@mapNotNull null
                Tier(id = id, name = d.name, price = price, details = d)
            }
            state = if (tiers.isEmpty()) UiState.Unavailable else UiState.Ready(tiers)
        }
    }

    /** Önceki oturumda tüketilmeden kalmış bağışları temizler. */
    private fun consumeLeftovers() {
        client.queryPurchasesAsync(
            com.android.billingclient.api.QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { _, purchases -> purchases.forEach { handlePurchase(it, silent = true) } }
    }

    fun donate(activity: Activity, tier: Tier) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(tier.details)
                        .build()
                )
            )
            .build()
        purchasePending = true
        val result = client.launchBillingFlow(activity, params)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            purchasePending = false
        }
    }

    private fun handlePurchase(purchase: Purchase, silent: Boolean = false) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        // Tüket: bağış tekrarlanabilir olsun ve Play'de askıda kalmasın.
        // consume, onaylanmamış satın almayı da kapatır; ayrıca acknowledge
        // gerekmez ama emin olmak için onaylanmamışsa önce onaylıyoruz.
        if (!purchase.isAcknowledged) {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
            ) { /* sonucu önemli değil; tüketme zaten kapatacak */ }
        }
        client.consumeAsync(
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        ) { _, _ -> }
        if (!silent) thanks = true
    }

    fun dismissThanks() {
        thanks = false
    }

    override fun onCleared() {
        runCatching { client.endConnection() }
    }
}
