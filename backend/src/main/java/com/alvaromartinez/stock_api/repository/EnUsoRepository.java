package com.alvaromartinez.stock_api.repository;

import com.alvaromartinez.stock_api.model.EnUso;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository de EnUso. De momento nos basta con lo que JpaRepository da
 * gratis: findById() (localizar el rollo abierto en consumir) y save().
 */
public interface EnUsoRepository extends JpaRepository<EnUso, Long> {

}
