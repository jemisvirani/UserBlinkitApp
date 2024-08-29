package com.eclatsol.userblinkitclone.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.eclatsol.userblinkitclone.R
import com.eclatsol.userblinkitclone.activity.UserMainActivity
import com.eclatsol.userblinkitclone.databinding.FragmentSplashBinding
import com.eclatsol.userblinkitclone.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.jvm.internal.Ref.IntRef

class SplashFragment : Fragment() {

    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: FragmentSplashBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(layoutInflater)
        setStatusBarColor()
        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                viewModel.isCurrentUser.collect{
                    if (it){
                        startActivity(Intent(requireActivity(),UserMainActivity::class.java))
                        requireActivity().finish()
                    }else{
                        findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
                    }
                }
            }
        }, 3000)
        return binding.root
    }

    private fun setStatusBarColor(){
        activity?.window?.apply {
            val statusColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusColors
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}