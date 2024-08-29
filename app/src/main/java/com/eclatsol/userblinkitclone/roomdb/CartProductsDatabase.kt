package com.eclatsol.userblinkitclone.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartProducts::class], version = 1, exportSchema = false)
abstract class CartProductsDatabase : RoomDatabase(){
    abstract fun getCartProductDao() : CartProductDao

    companion object{

        @Volatile
        private var INSTANCE : CartProductsDatabase?= null

        fun getDatabaseInstance(context : Context) : CartProductsDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(context,CartProductsDatabase::class.java,"CartProduct").build()
                INSTANCE = instance
                instance
            }
        }
    }
}