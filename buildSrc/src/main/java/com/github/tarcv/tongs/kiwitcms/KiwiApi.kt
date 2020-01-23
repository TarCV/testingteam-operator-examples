package com.github.tarcv.tongs.kiwitcms

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import com.googlecode.jsonrpc4j.*
import java.net.URL


interface KiwiApi {
    @JsonRpcMethod("TestCase.filter")
    fun testCaseFilter(predicates: List<TestCaseFilter>): List<Any>?

    @JsonRpcMethod("TestPlan.addCase")
    fun testPlanAddCase(planId: Int, caseId: Int): List<Any>

    @JsonRpcMethod("TestRun.addCase")
    fun testRunAddCase(
        @JsonRpcParam("run_id") runId: Int,
        @JsonRpcParam("case_id") caseId: Int
    ): TestExecution

    @JsonRpcMethod("TestExecution.update")
    fun testExecutionUpdate(
        @JsonRpcParam("case_run_id") runId: Int,
        @JsonRpcParam("values") values: TestExecutionValues
    ): TestExecution

    @JsonRpcMethod("Auth.login")
    fun authLogin(login: String, password: String): String

    @JsonRpcMethod("Auth.logout")
    fun authLogout()

    @JsonRpcMethod("Build.filter")
    fun buildFilter(filter: ProductBuildFilter): List<ProductBuild>

    @JsonRpcMethod("Build.create")
    fun buildCreate(buildInfo: ProductBuildFilter): ProductBuild

    @JsonRpcMethod("TestRun.create")
    fun testRunCreate(info: TestRunFilter): TestRun

    class TestRun(
        var id: Int
    )

    @JsonRpcMethod("Product.filter")
    fun productFilter(filter: ProductFilter): List<Product>

    @JsonRpcMethod("Classification.filter")
    fun classificationFilter(filter: ClassificationFilter): List<Classification>

    @JsonRpcMethod("Product.create")
    fun productCreate(productFilter: ProductFilter): Product

    @JsonRpcMethod("Version.filter")
    fun versionFilter(filter: ProductVersionFilter): List<ProductVersion>

    @JsonRpcMethod("Version.create")
    fun versionCreate(info: ProductVersionFilter): ProductVersion

    @JsonRpcMethod("TestPlan.filter")
    fun testPlanFilter(filter: TestPlanFilter): List<TestPlan>

    @JsonRpcMethod("PlanType.filter")
    fun planTypeFilter(filter: TestPlanTypeFilter): List<TestPlanType>

    @JsonRpcMethod("TestPlan.create")
    fun testPlanCreate(info: TestPlanCreateTemplate): TestPlan

    class TestPlanType(
        var id: Int
    )

    class Classification(
        var id: Int
    )

    class TestCaseFilter(
        @JsonProperty("pk") val caseId: Int,
        val planId: Int
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    class TestExecution (
        @JsonProperty("run_id")
        var runId: Int = 0
    )

    class TestExecutionValues(var status: TestExecutionStatus)

    class ProductBuild(
        var id: Int
    )

    class ProductBuildFilter(
        var product: Int,
        var name: String
    )

    class TestRunFilter(
        @JsonProperty("build") var buildId: Int,
        var manager: String,
        @JsonProperty("plan") var planId: Int,
        var summary: String
    )

    class ProductFilter(
        var name: String,
        @JsonProperty("classification_id") var classificationId: Int? = null
    )

    class Product(
        var id: Int
    )

    class ClassificationFilter {

    }

    class ProductVersionFilter(
        @JsonProperty("product") productId: Int,
        value: String
    )

    class ProductVersion(
        var id: Int
    )

    class TestPlanFilter(
        var name: String,
        @JsonProperty("product") var productId: Int
    )

    class TestPlan (
        var id: Int
    )

    open class TestPlanTypeFilter(var name: String)

    class TestPlanCreateTemplate(
        @JsonProperty("product") var productId: Int,
        @JsonProperty("type") var typeId: Int,
        @JsonProperty("default_product_version") var defaultProductVersion: Int,
        @JsonProperty("product_version") var productVersion: Int,
        var text: String,
        @JsonProperty("is_active") var isActive: Boolean,
        name: String
    ): TestPlanTypeFilter(name)

    enum class TestExecutionStatus(@get:JsonValue val value: Int) {
        PASS(4),
        FAIL(5),
        IDLE(1);
    }
}

class KiwiService(
    login: String,
    password: String,
    baseUrl: String = "http://example.com/UserService.json"
) {
    private val client = JsonRpcHttpClient(
        URL(baseUrl)
    )

    private val service: KiwiApi = ProxyUtil.createClientProxy<KiwiApi>(
        javaClass.classLoader,
        KiwiApi::class.java,
        client
    )

    private var sessionId: String

    init {
        sessionId = service.authLogin(login, password)
    }

    fun close() {
        service.authLogout()
    }

    fun addTestCaseToPlan(planId: Int, caseId: Int) {
        val results = service.testCaseFilter(listOf(KiwiApi.TestCaseFilter(caseId, planId)))

        if (results != null && results.isNotEmpty()) {
            service.testPlanAddCase(planId, caseId)
        }
    }

    fun addTestCaseToRunId(testRunId: Int, testCaseId: Int): KiwiApi.TestExecution {
        return service.testRunAddCase(testRunId, testCaseId)
    }

    fun updateTestExecution(runId: Int, status: KiwiApi.TestExecutionStatus): KiwiApi.TestExecution {
        val values = KiwiApi.TestExecutionValues(status)
        return service.testExecutionUpdate(runId, values)
    }

    fun getBuilds(filter: KiwiApi.ProductBuildFilter): List<KiwiApi.ProductBuild> {
        return service.buildFilter(filter)
    }

    fun createBuild(name: String, productId: Int): KiwiApi.ProductBuild {
        return service.buildCreate(KiwiApi.ProductBuildFilter(productId, name))
    }

    fun createNewRun(buildId: Int, managerLogin: String, planId: Int, summary: String): KiwiApi.TestRun {
        val info = KiwiApi.TestRunFilter(
            buildId,
            managerLogin,
            planId,
            summary
        )
        return service.testRunCreate(info)
    }

    fun getProductId(name: String): Int? {
        val product = service.productFilter(KiwiApi.ProductFilter(name))
        return if (product.isEmpty()) {
            null
        } else {
            product[0].id
        }
    }

    fun createNewProduct(name: String): KiwiApi.Product {
        val classificationFilter = KiwiApi.ClassificationFilter()

        // TODO: move classification filtering in emitter
        // get the first possible classification
        val classificationId = service.classificationFilter(classificationFilter)[0].id

        return service.productCreate(KiwiApi.ProductFilter(
            name,
            classificationId
        ))
    }

    fun getVersions(filter: KiwiApi.ProductVersionFilter): List<KiwiApi.ProductVersion> {
        return service.versionFilter(filter)
    }

    fun createProductVersion(versionName: String, productId: Int): KiwiApi.ProductVersion {
        return service.versionCreate(KiwiApi.ProductVersionFilter(
            productId,
            versionName
        ))
    }

    fun getTestPlanId(name: String, productId: Int): Int {
        val testPlans = service.testPlanFilter(
            KiwiApi.TestPlanFilter(
                name,
                productId
            )
        )
        return if (testPlans.isEmpty()) {
            -1
        } else {
            testPlans[0].id
        }
    }

    fun createNewTP(productId: Int, name: String, versionId: Int): KiwiApi.TestPlan {
        val planTypeFilter = KiwiApi.TestPlanTypeFilter(name = "Automation")
        val typeId = service.planTypeFilter(planTypeFilter)[0].id

        return service.testPlanCreate(KiwiApi.TestPlanCreateTemplate(
            productId,
            typeId,
            0,
            versionId,
            "WIP",
            true,
            name
        ))
    }
}