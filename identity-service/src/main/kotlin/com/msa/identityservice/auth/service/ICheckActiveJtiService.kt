package com.msa.identityservice.auth.service

import com.msa.identityservice.auth.token.dto.TokenAuthInfo


interface ICheckActiveJtiService {
    fun checkActiveJti(): TokenAuthInfo
}
