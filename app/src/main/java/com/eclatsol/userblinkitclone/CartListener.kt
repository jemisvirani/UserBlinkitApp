package com.eclatsol.userblinkitclone

interface CartListener {
    fun showCartLayout(itemCount : Int)

    fun savingCartItemCount(itemCount: Int)
}