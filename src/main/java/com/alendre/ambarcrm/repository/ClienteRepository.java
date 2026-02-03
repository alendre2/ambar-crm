package com.alendre.ambarcrm.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alendre.ambarcrm.model.Cliente;
import com.alendre.ambarcrm.model.StatusCliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    // Busca por nome (Ordenado)
    List<Cliente> findByNomeContainingIgnoreCaseOrderByIdDesc(String nome);
    
    // Busca por STATUS (Ordenado) - NOVO MÉTODO
    List<Cliente> findByStatusOrderByIdDesc(StatusCliente status);
    
    // Busca todos (Ordenado)
    List<Cliente> findAllByOrderByIdDesc();
}