package com.example.recetas.gustos.domain

import com.example.recetas.gustos.data.model.Gusto
import com.example.recetas.gustos.data.repository.GustosRepository
import javax.inject.Inject


class FetchGustosUseCase @Inject constructor(
    private val gustosRepository: GustosRepository
) {
    suspend operator fun invoke(): List<Gusto> {
        return gustosRepository.fetchAllGustos()
    }

    class GetUserGustosUseCase @Inject constructor(
        private val gustosRepository: GustosRepository
    ) {
        suspend operator fun invoke(): List<Gusto> {
            return gustosRepository.getUserGustos()
        }
    }

    class AddGustoToUserUseCase @Inject constructor(
        private val gustosRepository: GustosRepository
    ) {
        suspend operator fun invoke(gustoId: Int): Boolean {
            return gustosRepository.addGustoToUser(gustoId)
        }
    }

    class RemoveGustoFromUserUseCase @Inject constructor(
        private val gustosRepository: GustosRepository
    ) {
        suspend operator fun invoke(gustoId: Int): Boolean {
            return gustosRepository.removeGustoFromUser(gustoId)
        }
    }
}