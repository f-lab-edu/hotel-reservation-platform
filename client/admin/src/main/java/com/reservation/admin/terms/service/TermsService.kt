package com.reservation.admin.terms.service

import com.reservation.admin.terms.controller.request.TermsCursor
import com.reservation.admin.terms.repository.JpaTermsRepository
import com.reservation.admin.terms.repository.TermsQueryRepository
import com.reservation.admin.terms.repository.dto.SearchTermsResult
import com.reservation.domain.terms.Terms
import com.reservation.domain.terms.enums.TermsCode
import com.reservation.domain.terms.enums.TermsStatus
import com.reservation.querysupport.page.KeysetPage
import com.reservation.support.exception.ErrorCode
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import lombok.extern.log4j.Log4j2
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
@Log4j2
class TermsService {
    private val log: Logger = LoggerFactory.getLogger(TermsService::class.java)

    private val jpaTermsRepository: JpaTermsRepository? = null
    private val termsQueryRepository: TermsQueryRepository? = null

    @Transactional
    fun create(requestCreateTerms: Terms): Long {
        checkActiveTermsExists(requestCreateTerms.code)

        // 약관 Versioning
        var maxVersion = jpaTermsRepository!!.findMaxVersionByCode(requestCreateTerms.code).orElse(NOTHING_VERSION)

        requestCreateTerms.setNewVersionAndIdInitialization(++maxVersion)

        return saveTermsWithIntegrityCheck(requestCreateTerms)
    }

    private fun checkActiveTermsExists(code: TermsCode) {
        val existsActiveTerms = jpaTermsRepository!!.existsByCodeAndStatus(code, TermsStatus.ACTIVE)
        if (existsActiveTerms) {
            throw ErrorCode.BAD_REQUEST.exception("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.")
        }
    }

    private fun saveTermsWithIntegrityCheck(saveTerms: Terms): Long {
        try {
            return jpaTermsRepository!!.save(saveTerms).id
        } catch (e: DataIntegrityViolationException) {
            log.warn(e.message)
            throw ErrorCode.CONFLICT.exception("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요")
        }
    }

    @Transactional
    fun update(requestNewVersionTerms: Terms): Long {
        // 가장 최신 약관 버전이 맞는지 확인한다
        var maxVersion = checkUpdateTermsVersion(requestNewVersionTerms.id)

        // 기존 약관을 deprecate 한다
        requestNewVersionTerms.deprecate()
        jpaTermsRepository!!.saveAndFlush(requestNewVersionTerms)

        // New Versioning 세팅 및 ID 초기화
        requestNewVersionTerms.setNewVersionAndIdInitialization(++maxVersion)

        return saveTermsWithIntegrityCheck(requestNewVersionTerms)
    }

    private fun checkUpdateTermsVersion(id: Long): Int {
        val findTerms = jpaTermsRepository!!.findById(id)
            .orElseThrow { ErrorCode.NOT_FOUND.exception("약관이 존재하지 않습니다.") }

        // 같은 약관 코드 중 가창 최신 약관 버전을 가져온다
        val maxVersion = jpaTermsRepository.findMaxVersionByCode(findTerms.code)
            .orElseThrow { ErrorCode.CONFLICT.exception("올바른 약관 버전을 찾을 수 없습니다.") }

        if (findTerms.version != maxVersion) {
            throw ErrorCode.BAD_REQUEST.exception("과거 버전의 약관은 수정할 수 없습니다.")
        }

        return maxVersion
    }

    fun findById(id: Long): Terms {
        return termsQueryRepository!!.findWithClausesById(id)
            .orElseThrow { ErrorCode.NOT_FOUND.exception("존재하지 않는 약관입니다.") }
    }

    fun searchTermsFormatPage(
        searchCodeOrNull: TermsCode?,
        isLatest: Boolean,
        pageRequest: PageRequest?
    ): Page<SearchTermsResult> {
        return termsQueryRepository!!.searchTermsFormatPage(searchCodeOrNull, isLatest, pageRequest)
    }

    fun searchTermsFormatCursor(
        searchCodeOrNull: TermsCode?,
        isLatest: Boolean,
        size: Int,
        cursors: List<TermsCursor?>?
    ): KeysetPage<SearchTermsResult, TermsCursor> {
        return termsQueryRepository!!.findTermsByKeysetCondition(searchCodeOrNull, isLatest, size, cursors)
    }

    companion object {
        private const val NOTHING_VERSION = 0
    }
}
