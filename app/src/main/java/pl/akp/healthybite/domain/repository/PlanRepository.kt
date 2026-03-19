package pl.akp.healthybite.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.akp.healthybite.domain.model.PlanTemplate

interface PlanRepository {
    fun observePlans(): Flow<List<PlanTemplate>>
}
