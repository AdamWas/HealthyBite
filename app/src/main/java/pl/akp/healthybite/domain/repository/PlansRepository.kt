package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.domain.model.PlanTemplate

interface PlansRepository {
    fun getPlans(): Flow<List<PlanTemplate>>
    suspend fun addPlan(plan: PlanTemplate)
}

