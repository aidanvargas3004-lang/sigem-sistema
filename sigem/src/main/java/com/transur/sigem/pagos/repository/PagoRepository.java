package com.transur.sigem.pagos.repository;

import com.transur.sigem.pagos.entity.EstadoPago;
import com.transur.sigem.pagos.entity.MetodoPago;
import com.transur.sigem.pagos.entity.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente"})
    List<Pago> findAllByOrderByFechaPagoDescIdDesc();

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente"})
    List<Pago> findByContratoIdOrderByFechaPagoDescIdDesc(Long contratoId);

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente"})
    List<Pago> findByContratoClienteIdOrderByFechaPagoDescIdDesc(Long clienteId);

    @Override
    @EntityGraph(attributePaths = {"contrato", "contrato.cliente"})
    Optional<Pago> findById(Long id);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.contrato.id = :contratoId AND p.estado IN :estados")
    BigDecimal sumarPorContrato(@Param("contratoId") Long contratoId, @Param("estados") Collection<EstadoPago> estados);

    /** Filas [contratoId, estado, suma] para calcular el estado de pago de muchos contratos en una sola consulta. */
    @Query("SELECT p.contrato.id, p.estado, SUM(p.monto) FROM Pago p GROUP BY p.contrato.id, p.estado")
    List<Object[]> totalesPorContratoYEstado();

    boolean existsByMetodoAndNumeroOperacionIgnoreCaseAndEstadoIn(MetodoPago metodo, String numeroOperacion,
                                                                  Collection<EstadoPago> estados);

    long countByEstado(EstadoPago estado);
}
