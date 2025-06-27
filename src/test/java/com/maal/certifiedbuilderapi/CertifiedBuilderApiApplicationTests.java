package com.maal.certifiedbuilderapi;

import com.maal.certifiedbuilderapi.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de contexto da aplicação
 * Garante que o contexto Spring carrega corretamente em ambiente de teste
 * @ActiveProfiles("test") força o perfil de teste, evitando carregar configurações AWS reais
 * @Import(TestConfig.class) importa configuração específica de teste com mocks
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class CertifiedBuilderApiApplicationTests {

    /**
     * Teste básico que verifica se o contexto Spring carrega sem erros
     * Este teste é fundamental para validar que todas as configurações estão corretas
     */
    @Test
    void contextLoads() {
        // Este teste passa simplesmente se o contexto Spring for carregado com sucesso
        // Qualquer problema de configuração ou dependência causará falha aqui
    }

}