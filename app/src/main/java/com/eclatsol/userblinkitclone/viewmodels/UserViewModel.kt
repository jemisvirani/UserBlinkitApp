package com.eclatsol.userblinkitclone.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eclatsol.userblinkitclone.Constants
import com.eclatsol.userblinkitclone.Utils
import com.eclatsol.userblinkitclone.api.ApiUtilities
import com.eclatsol.userblinkitclone.models.Product
import com.eclatsol.userblinkitclone.models.Users
import com.eclatsol.userblinkitclone.roomdb.CartProductDao
import com.eclatsol.userblinkitclone.roomdb.CartProducts
import com.eclatsol.userblinkitclone.roomdb.CartProductsDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    //initializations
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("My_Pref", MODE_PRIVATE)
    val cartProductDao: CartProductDao =
        CartProductsDatabase.getDatabaseInstance(application).getCartProductDao()

    private val _paymentStatus = MutableStateFlow<Boolean>(false)
    val paymentStatus = _paymentStatus

    //Room DB
    fun insertCartProduct(product: CartProducts) = viewModelScope.launch(Dispatchers.IO) {
        cartProductDao.insertCartProduct(product)
    }

    fun getAll(): LiveData<List<CartProducts>> {
        return cartProductDao.getAllCartProducts()
    }

    fun updateCartProduct(product: CartProducts) = viewModelScope.launch(Dispatchers.IO) {
        cartProductDao.updateCartProduct(product)
    }

    fun deleteCartProduct(productId: String) = viewModelScope.launch(Dispatchers.IO) {
        cartProductDao.deleteCartProduct(productId)
    }

    fun updateItemCount(product: Product,itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins")
            .child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)

        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory/${product.productCategory}/${product.productRandomId}")
            .child("itemCount").setValue(itemCount)

        FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductType/${product.productType}/${product.productRandomId}")
            .child("itemCount").setValue(itemCount)

    }

    //Firebase call
    fun fetchAllTheProduct(): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                Log.e("prodData", "onDataChange: ${snapshot.value}")

                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        db.addValueEventListener(eventListener)

        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory/${category}/")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                Log.e("prodData", "onDataChange: ${snapshot.value}")

                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        db.addValueEventListener(eventListener)

        awaitClose { db.removeEventListener(eventListener) }
    }

    fun saveUserAddress(address : String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users")
            .child(Utils.getCurrentUserId().toString()).child("userAddress").setValue(address)
    }

    //sharePreferences
    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalCartItemCount(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun saveAddressStatus(){
        sharedPreferences.edit().putBoolean("addressStatus",true).apply()
    }

    fun getAddressStatus() : MutableLiveData<Boolean>{
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }

    //retrofit

    suspend fun checkPayment(headers : Map<String,String>){
        val res = ApiUtilities.statusApi.checkStatus(headers,Constants.MERCHANT_ID,Constants.merchantTransactionId)
        _paymentStatus.value = res.body() != null && res.body()!!.success
    }

}