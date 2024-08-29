package com.eclatsol.userblinkitclone.api

import com.eclatsol.userblinkitclone.models.CheckStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface ApiInterface {

    @GET("apis/pg-sandbox/pg/v1/status/{merchantId}/{merchantTransactionId}")
    suspend fun checkStatus(@HeaderMap headers : Map<String,String>,
                            @Path("merchantId") merchantId : String,
                            @Path("merchantTransactionId") merchantTransactionId : String,
                            ) : Response<CheckStatus>
}