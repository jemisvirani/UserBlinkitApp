package com.eclatsol.userblinkitclone.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.eclatsol.userblinkitclone.Constants
import com.eclatsol.userblinkitclone.R
import com.eclatsol.userblinkitclone.Utils
import com.eclatsol.userblinkitclone.adapters.AdapterCartProduct
import com.eclatsol.userblinkitclone.databinding.ActivityOrderPlaceBinding
import com.eclatsol.userblinkitclone.databinding.AddressLayoutBinding
import com.eclatsol.userblinkitclone.viewmodels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class OrderPlaceActivity : AppCompatActivity() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityOrderPlaceBinding
    private lateinit var adapterCartProduct: AdapterCartProduct
    private lateinit var b2BPGRequest: B2BPGRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllCartProducts()
        setStatusBarColor()
        backToUserMainActivity()
        onPlaceOrderClicked()
        initializePhonePay()
    }

    private fun initializePhonePay() {
        val data = JSONObject()
        PhonePe.init(this, PhonePeEnvironment.SANDBOX, Constants.MERCHANT_ID, "")

        data.put("merchantId", Constants.MERCHANT_ID)
        data.put("merchantTransactionId", Constants.merchantTransactionId)
        data.put("amount", 200) //Long. Mandatory
        data.put("mobileNumber", "8839990051") //String. Optional
        data.put("callbackUrl", "https://webhook.site/callback-url")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")

        data.put("paymentInstrument", paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOS", "ANDROID")
        data.put("deviceContext", deviceContext)

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()),
            Base64.NO_WRAP
        )

        val checkSum =
            sha256(payloadBase64 + Constants.apiEndPoint + Constants + Constants.SALT_KEY) + "###1"

        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checkSum)
            .setUrl(Constants.apiEndPoint)
            .build()
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.getAddressStatus().observe(this) { status ->
                if (status) {
                    //payment work
                    getPaymentView()
                } else {
                    val addressLayoutBinding =
                        AddressLayoutBinding.inflate(LayoutInflater.from(this))

                    val alertDialog =
                        AlertDialog.Builder(this).setView(addressLayoutBinding.root).create()
                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener {
                        saveAddress(alertDialog, addressLayoutBinding)
                    }
                }
            }
        }
    }

    val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            checkStatus()
        }
    }

    private fun checkStatus() {
        val xVerify = sha256("/pg/v1/status/${Constants.MERCHANT_ID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constants.MERCHANT_ID,
        )
        lifecycleScope.launch {

            viewModel.checkPayment(headers)
        }
        lifecycleScope.launch {
            viewModel.paymentStatus.collect{status->
                if (status){
                    Utils.showToast(this@OrderPlaceActivity,"Payment done")
                    startActivity(Intent(this@OrderPlaceActivity,UserMainActivity::class.java))
                    finish()
                }else{
                    Utils.showToast(this@OrderPlaceActivity,"Payment not done")
                }
            }
        }

    }

    private fun getPaymentView() {
        try {
            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator").let {
                phonePayView.launch(it)
            }
        } catch (e: PhonePeInitException) {
            Utils.showToast(this, e.message.toString())
        }

    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Processing...")

        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etDescriptiveAddress.text.toString()

        val address = "$userPinCode,$userDistrict($userState),$userAddress $userPhoneNumber"

        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }
        Utils.showToast(this, "Saved...")
        alertDialog.dismiss()
        Utils.hideDialog()
    }

    private fun backToUserMainActivity() {
        binding.tbOrder.setNavigationOnClickListener {
            startActivity(Intent(this, UserMainActivity::class.java))
            finish()
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this) { cartProductList ->

            adapterCartProduct = AdapterCartProduct()
            binding.rvProductsItems.adapter = adapterCartProduct
            adapterCartProduct.differ.submitList(cartProductList)

            var totalPrice = 0

            for (products in cartProductList) {
                val price = products.productPrice?.substring(1)?.toInt()
                val itemCount = products.productCount!!
                totalPrice += (price?.times(itemCount)!!)
            }

            binding.tvSubTotal.text = totalPrice.toString()

            if (totalPrice < 200) {
                binding.tvDeliveryCharge.text = "₹15"
                totalPrice += 15
            }

            binding.tvGrandTotal.text = totalPrice.toString()
        }
    }


    private fun setStatusBarColor() {
        window?.apply {
            val statusColors = ContextCompat.getColor(this@OrderPlaceActivity, R.color.orange)
            statusBarColor = statusColors
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}
