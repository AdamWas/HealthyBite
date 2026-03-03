package pl.akp.healthybite.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.akp.healthybite.domain.model.PlanTemplate
import pl.akp.healthybite.domain.repository.PlansRepository

class PlansRepositoryImpl : PlansRepository {

    override fun getPlans(): Flow<List<PlanTemplate>> {
        return flowOf(emptyList())
    }

    override suspend fun addPlan(plan: PlanTemplate) {
        // Not implemented yet
    }
}

