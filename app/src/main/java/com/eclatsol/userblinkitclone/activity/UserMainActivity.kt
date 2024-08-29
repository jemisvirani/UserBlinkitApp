package com.eclatsol.userblinkitclone.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.eclatsol.userblinkitclone.CartListener
import com.eclatsol.userblinkitclone.R
import com.eclatsol.userblinkitclone.adapters.AdapterCartProduct
import com.eclatsol.userblinkitclone.databinding.ActivityUserMainBinding
import com.eclatsol.userblinkitclone.databinding.BsCartProductBinding
import com.eclatsol.userblinkitclone.databinding.ItemViewProductBinding
import com.eclatsol.userblinkitclone.roomdb.CartProducts
import com.eclatsol.userblinkitclone.viewmodels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class UserMainActivity : AppCompatActivity(), CartListener {
    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityUserMainBinding
    private lateinit var cartProductList: List<CartProducts>
    private lateinit var adapterCartProduct: AdapterCartProduct
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllCartProducts()
        getTotalItemCountInCart()
        onCartClicked()
        onNextButtonClicked()
    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this,OrderPlaceActivity::class.java))
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this) {
            cartProductList = it
        }
    }

    private fun onCartClicked() {
        binding.llItemCart.setOnClickListener {
            val bsCartProductBinding = BsCartProductBinding.inflate(LayoutInflater.from(this))
            val bs = BottomSheetDialog(this)
            bs.setContentView(bsCartProductBinding.root)

            bsCartProductBinding.tvNumberOfProductCount.text =  binding.tvNumberOfProductCount.text
            bsCartProductBinding.btnNext.setOnClickListener {
                startActivity(Intent(this,OrderPlaceActivity::class.java))
            }
            adapterCartProduct = AdapterCartProduct()
            bsCartProductBinding.rvProductsItems.adapter = adapterCartProduct
            adapterCartProduct.differ.submitList(cartProductList)

            bs.show()
        }
    }

    private fun getTotalItemCountInCart() {
        viewModel.fetchTotalCartItemCount().observe(this) {
            if (it > 0) {
                binding.llCart.visibility = View.VISIBLE
                binding.tvNumberOfProductCount.text = it.toString()
            } else {
                binding.llCart.visibility = View.GONE
            }
        }
    }

    override fun showCartLayout(itemCount: Int) {
        val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if (updatedCount > 0) {
            binding.llCart.visibility = View.VISIBLE
            binding.tvNumberOfProductCount.text = updatedCount.toString()
        } else {
            binding.llCart.visibility = View.GONE
            binding.tvNumberOfProductCount.text = "0"
        }
    }

    override fun savingCartItemCount(itemCount: Int) {
        viewModel.fetchTotalCartItemCount().observe(this) {
            viewModel.savingCartItemCount(it + itemCount)
        }
    }
}