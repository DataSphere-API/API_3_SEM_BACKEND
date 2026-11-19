package com.datasphere.fusex.fatura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemFaturaRepository extends JpaRepository<ItemFaturaModel, Integer> {
    List<ItemFaturaModel> findByFaturaId(Long faturaId);

    void deleteByFaturaIdAndEspelhoItemIdIn(Long faturaId, List<Integer> espelhoItemIds);

    // Status das faturas em que o atendimento entrou, da mais nova para a mais antiga.
    @Query("""
            select f.status
            from FaturaModel f, ItemFaturaModel i, EspelhoItemModel e
            where i.faturaId = f.id and e.id = i.espelhoItemId and e.atendimentoId = :atendimentoId
            order by f.id desc
            """)
    List<StatusFatura> listarStatusDasFaturasDoAtendimento(@Param("atendimentoId") Long atendimentoId);
}