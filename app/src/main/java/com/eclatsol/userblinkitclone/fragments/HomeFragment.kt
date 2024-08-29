package com.eclatsol.userblinkitclone.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.eclatsol.userblinkitclone.Constants
import com.eclatsol.userblinkitclone.R
import com.eclatsol.userblinkitclone.adapters.AdapterCategory
import com.eclatsol.userblinkitclone.databinding.FragmentHomeBinding
import com.eclatsol.userblinkitclone.models.Category
import com.eclatsol.userblinkitclone.viewmodels.UserViewModel


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val userViewModel: UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAllCategories()
        navigatingToSearchFragment()
        get()
        return binding.root
    }

    private fun navigatingToSearchFragment() {
        binding.searchCv.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()
        for (i in 0 until Constants.allProductsCategoryIcon.size){
            categoryList.add(Category(
                Constants.allProductsCategory[i],
                Constants.allProductsCategoryIcon[i]))
        }

        binding.rvCategories.adapter = AdapterCategory(categoryList,::onCategoryIconClicked)
    }

    private fun onCategoryIconClicked(category: Category){
        val bundle = Bundle()
        bundle.putString("category",category.tittle)
        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment,bundle)
    }

    private fun get(){
        userViewModel.getAll().observe(viewLifecycleOwner){
            for (i in it){
                Log.e("vvv", i.productTitle.toString())
                Log.e("vvv", i.productCount.toString())
            }
        }
    }


    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusColors = ContextCompat.getColor(requireContext(), R.color.orange)
            statusBarColor = statusColors
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }


}