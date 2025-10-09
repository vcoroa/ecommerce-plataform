package br.com.vcoroa.ecommerce.platform.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "br.com.vcoroa.ecommerce.platform.infrastructure.repository")
@EnableElasticsearchAuditing
public class ElasticsearchConfig {
}
