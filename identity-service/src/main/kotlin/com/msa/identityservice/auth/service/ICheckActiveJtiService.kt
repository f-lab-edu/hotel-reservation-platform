package com.msa.identityservice.auth.service

import com.msa.supportmodule.auth.token.dto.TokenAuthInfo


interface ICheckActiveJtiService {
    fun checkActiveJti(): TokenAuthInfo
}
