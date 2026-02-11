package com.llego.shared.data.repositories

import com.apollographql.apollo.ApolloClient
import com.llego.shared.data.auth.TokenManager
import com.llego.shared.data.model.Branch
import com.llego.shared.data.model.Business
import com.llego.shared.data.model.BusinessResult
import com.llego.shared.data.model.BusinessWithBranches
import com.llego.shared.data.model.CreateBranchInput
import com.llego.shared.data.model.CreateBusinessInput
import com.llego.shared.data.model.RegisterBranchInput
import com.llego.shared.data.model.UpdateBranchInput
import com.llego.shared.data.model.UpdateBusinessInput
import com.llego.shared.data.network.GraphQLClient
import kotlinx.coroutines.flow.StateFlow

/**
 * Facade repository that coordinates business and branch domain repositories.
 */
class BusinessRepository(
    client: ApolloClient = GraphQLClient.apolloClient,
    tokenManager: TokenManager
) {
    private val state = BusinessRepositoryState()
    private val businessDomainRepository = BusinessDomainRepository(client, tokenManager, state)
    private val branchDomainRepository = BranchDomainRepository(client, tokenManager, state)

    val currentBusiness: StateFlow<Business?> = state.currentBusiness
    val businesses: StateFlow<List<Business>> = state.businesses
    val branches: StateFlow<List<Branch>> = state.branches
    val currentBranch: StateFlow<Branch?> = state.currentBranch

    suspend fun registerBusiness(
        business: CreateBusinessInput,
        branches: List<RegisterBranchInput>
    ): BusinessResult<Business> {
        return businessDomainRepository.registerBusiness(business, branches)
    }

    suspend fun getBusinesses(): BusinessResult<List<Business>> {
        return businessDomainRepository.getBusinesses()
    }

    suspend fun getBusiness(id: String): BusinessResult<Business> {
        return businessDomainRepository.getBusiness(id)
    }

    suspend fun updateBusiness(
        businessId: String,
        input: UpdateBusinessInput
    ): BusinessResult<Business> {
        return businessDomainRepository.updateBusiness(businessId, input)
    }

    suspend fun deleteBusiness(businessId: String): BusinessResult<Boolean> {
        return businessDomainRepository.deleteBusiness(businessId)
    }

    suspend fun getBusinessesWithBranches(): BusinessResult<List<BusinessWithBranches>> {
        return businessDomainRepository.getBusinessesWithBranches()
    }

    suspend fun registerMultipleBusinesses(
        businesses: List<Pair<CreateBusinessInput, List<RegisterBranchInput>>>
    ): BusinessResult<List<Business>> {
        return businessDomainRepository.registerMultipleBusinesses(businesses)
    }

    suspend fun getBranches(businessId: String? = null): BusinessResult<List<Branch>> {
        return branchDomainRepository.getBranches(businessId)
    }

    suspend fun getBranch(id: String): BusinessResult<Branch> {
        return branchDomainRepository.getBranch(id)
    }

    suspend fun createBranch(input: CreateBranchInput): BusinessResult<Branch> {
        return branchDomainRepository.createBranch(input)
    }

    suspend fun updateBranch(
        branchId: String,
        input: UpdateBranchInput
    ): BusinessResult<Branch> {
        return branchDomainRepository.updateBranch(branchId, input)
    }

    suspend fun deleteBranch(branchId: String): BusinessResult<Boolean> {
        return branchDomainRepository.deleteBranch(branchId)
    }

    fun setCurrentBranch(branch: Branch) {
        state.setCurrentBranch(branch)
    }

    fun clearCurrentBranch() {
        state.setCurrentBranch(null)
    }

    fun clear() {
        state.clear()
    }
}
