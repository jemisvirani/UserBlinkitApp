package com.eclatsol.userblinkitclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.eclatsol.userblinkitclone.FilteringProducts
import com.eclatsol.userblinkitclone.databinding.ItemViewProductBinding
import com.eclatsol.userblinkitclone.models.ItemImageSlider
import com.eclatsol.userblinkitclone.models.Product

class AdapterProduct(
    val context: Context,
    val onAddButtonClicked: (Product, ItemViewProductBinding) -> Unit,
    val onIncrementButtonClicked: (Product, ItemViewProductBinding) -> Unit,
    val onDecrementButtonClicked: (Product, ItemViewProductBinding) -> Unit
) : RecyclerView.Adapter<AdapterProduct.ProductViewHolder>(),Filterable {

    class ProductViewHolder(val binding: ItemViewProductBinding) : ViewHolder(binding.root){

    }

    val diffUtil = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(ItemViewProductBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = differ.currentList[position]

        holder.binding.apply {
            val productImage = product.productImageUris
//            Log.e("AdapterData", "onBindViewHolder: ${product.productImageUris}")
//            Log.e("AdapterData", "onBindViewHolder Replace: $replaceText")
            for(i in 0 until productImage?.size!!){
                val item= mutableListOf<ItemImageSlider>()
//                Log.e("ProductAdapterData", "onBindViewHolder Replace: $data")
                item.addAll(listOf(ItemImageSlider(product.productImageUris!![i].toString().replace("[",""))))

                val viewPagerAdapter = AutoImageSliderAdapter(context, item)
                holder.binding.ivImageSlider.adapter = viewPagerAdapter
                viewPagerAdapter.autoslide(holder.binding.ivImageSlider)

                tvProductTitle.text = product.productTitle
                tvProductQuantity.text = product.productQuantity.toString() + product.productUnit
                tvProductPrice.text = "₹" + product.productPrice

                if (product.itemCount!! > 0){
                    tvProductCount.text = product.itemCount.toString()
                    tvAdd.visibility = View.GONE
                    llProductCount.visibility = View.VISIBLE
                }

                tvAdd.setOnClickListener {
                    onAddButtonClicked(product,this)
                }
                tvIncrementCount.setOnClickListener {
                    onIncrementButtonClicked(product,this)
                }
                tvDecrementCount.setOnClickListener {
                    onDecrementButtonClicked(product,this)
                }
            }
        }



    }


    val filter : FilteringProducts? = null
    var originalList = ArrayList<Product>()
    override fun getFilter(): Filter {

        if (filter == null) return FilteringProducts(this,originalList)
        return filter
    }


}