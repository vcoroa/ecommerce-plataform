package br.com.vcoroa.ecommerce.platform.application.service;

import br.com.vcoroa.ecommerce.platform.application.dto.criteria.ProductSearchCriteria;
import br.com.vcoroa.ecommerce.platform.application.dto.response.ProductDTO;
import br.com.vcoroa.ecommerce.platform.domain.entity.ProductDocument;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final ElasticsearchOperations elasticsearchOperations;

    public Page<ProductDTO> search(ProductSearchCriteria criteria) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            mustQueries.add(Query.of(q -> q
                .match(m -> m
                    .field("name")
                    .query(criteria.getName())
                    .fuzziness("AUTO")
                )
            ));
        }

        if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
            filterQueries.add(Query.of(q -> q
                .term(t -> t
                    .field("category")
                    .value(criteria.getCategory())
                )
            ));
        }

        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            filterQueries.add(Query.of(q -> q
                .range(r -> {
                    var rangeQuery = r.field("price");
                    if (criteria.getMinPrice() != null) {
                        rangeQuery.gte(co.elastic.clients.json.JsonData.of(criteria.getMinPrice()));
                    }
                    if (criteria.getMaxPrice() != null) {
                        rangeQuery.lte(co.elastic.clients.json.JsonData.of(criteria.getMaxPrice()));
                    }
                    return rangeQuery;
                })
            ));
        }

        filterQueries.add(Query.of(q -> q
            .range(r -> r
                .field("stockQuantity")
                .gt(co.elastic.clients.json.JsonData.of(0))
            )
        ));

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        if (!mustQueries.isEmpty()) {
            boolQueryBuilder.must(mustQueries);
        }
        if (!filterQueries.isEmpty()) {
            boolQueryBuilder.filter(filterQueries);
        }

        Query finalQuery;
        if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
            finalQuery = Query.of(q -> q.matchAll(m -> m));
        } else {
            finalQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));
        }

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(finalQuery)
            .withPageable(criteria.getPageable())
            .build();

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
            searchQuery,
            ProductDocument.class
        );

        List<ProductDTO> products = searchHits.stream()
            .map(hit -> mapToDTO(hit.getContent()))
            .collect(Collectors.toList());

        return new PageImpl<>(
            products,
            criteria.getPageable(),
            searchHits.getTotalHits()
        );
    }

    private ProductDTO mapToDTO(ProductDocument doc) {
        ProductDTO dto = new ProductDTO();
        try {
            dto.setId(UUID.fromString(doc.getId()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid UUID format in Elasticsearch document: " + doc.getId(), e);
        }
        dto.setName(doc.getName());
        dto.setDescription(doc.getDescription());
        dto.setPrice(doc.getPrice());
        dto.setCategory(doc.getCategory());
        dto.setStockQuantity(doc.getStockQuantity());
        dto.setCreatedAt(doc.getCreatedAt() != null ? LocalDateTime.parse(doc.getCreatedAt(), FORMATTER) : null);
        dto.setUpdatedAt(doc.getUpdatedAt() != null ? LocalDateTime.parse(doc.getUpdatedAt(), FORMATTER) : null);
        return dto;
    }
}
