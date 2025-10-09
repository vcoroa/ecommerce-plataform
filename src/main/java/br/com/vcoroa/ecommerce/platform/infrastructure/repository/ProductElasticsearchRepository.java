package br.com.vcoroa.ecommerce.platform.infrastructure.repository;

import br.com.vcoroa.ecommerce.platform.domain.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {
}
