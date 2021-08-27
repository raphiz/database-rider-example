package com.example.demo

import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.exporter.ExportDataSet
import com.github.database.rider.spring.api.DBRider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.testcontainers.containers.PostgreSQLContainer

@JooqTest
@DBRider
@TestMethodOrder(
    MethodOrderer.OrderAnnotation::class
)
class DemoApplicationTests {

    @Autowired
    lateinit var dsl: DSLContext


    private fun countPets() = dsl.select().from("example.pet").fetch().count()

    private fun insertPet(name: String) = dsl.insertInto(table("example.pet"))
        .set(field("name"), name)
        .execute()

    @Test
    @Order(1)
    fun `1 - first test, for which spring will roll back the db`() {
        assertThat(countPets(), `is`(0))

        insertPet("first test pet")

        assertThat(countPets(), `is`(1))
    }


    @Test
    @Order(2)
    fun `2 - second test, starting without existing data`() {
        assertThat(countPets(), `is`(0))

        insertPet("second test pet")

        assertThat(countPets(), `is`(1))
    }

    @Test
    @Order(3)
    @DataSet(value = ["empty.yml"])
    fun `3 - third test with database rider, without existing data using an empty data set`() {
        assertThat(countPets(), `is`(0))

        insertPet("third test pet")

        assertThat(countPets(), `is`(1))
    }

    @Test
    @Order(4)
    @DataSet(value = ["pets.yml"]) // expected behaviour: cleanAfter = true
    @ExportDataSet(outputName = "output.yml")
    fun `4 - fourth test with database rider, without existing data using a non-empty data set`() {
        assertThat(countPets(), `is`(1))

        insertPet("fourth test pet")

        assertThat(countPets(), `is`(2))
    }

    @Test
    @Order(5)
    fun `5 - fifth test expected to start without existing data`() {
        assertThat(countPets(), `is`(0)) // DB is not clean here!

        insertPet("fifth test pet")

        assertThat(countPets(), `is`(1))
    }


    companion object {

        val postgresTestContainer = KPostgresContainer(
            "postgres:13.2"
        )

        init {
            startAndConfigurePostgresContainer()
        }

        private fun startAndConfigurePostgresContainer() {
            postgresTestContainer.start()
            System.setProperty("spring.datasource.url", postgresTestContainer.jdbcUrl)
            System.setProperty("spring.datasource.username", postgresTestContainer.username)
            System.setProperty("spring.datasource.password", postgresTestContainer.password)
        }
    }
}


// https://github.com/testcontainers/testcontainers-java/issues/318
class KPostgresContainer(imageName: String) : PostgreSQLContainer<KPostgresContainer>(imageName)
