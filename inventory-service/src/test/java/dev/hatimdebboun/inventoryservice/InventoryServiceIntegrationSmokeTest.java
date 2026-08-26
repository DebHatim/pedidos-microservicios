package dev.hatimdebboun.inventoryservice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryServiceIntegrationSmokeTest extends AbstractIntegrationTest {

    @Test
    void mysqlContainerEstaCorriendo() {
        assertThat(mysqlContainer.isRunning()).isTrue();
    }

    @Test
    void kafkaContainerEstaCorriendo() {
        assertThat(kafkaContainer.isRunning()).isTrue();
    }

    @Test
    void elContextoDeSpringCargaCorrectamenteContraElContenedorMysql() {
        assertThat(mysqlContainer.getJdbcUrl()).contains("inventory_test_db");
    }
}