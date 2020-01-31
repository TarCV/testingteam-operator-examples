package com.github.tarcv.tongs.kiwitcms

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.googlecode.jsonrpc4j.JsonRpcHttpClient
import com.googlecode.jsonrpc4j.JsonRpcMethod
import com.googlecode.jsonrpc4j.JsonRpcParam
import com.googlecode.jsonrpc4j.ProxyUtil
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


interface KiwiApi {
    @JsonRpcMethod("TestCase.filter")
    fun testCaseFilter(predicates: TestCaseFilter): List<TestCase>

    @JsonRpcMethod("TestPlan.add_case")
    fun testPlanAddCase(planId: Int, caseId: Int)

    @JsonRpcMethod("TestRun.add_case")
    fun testRunAddCase(
        @JsonRpcParam("run_id") runId: Int,
        @JsonRpcParam("case_id") caseId: Int
    ): TestExecution

    @JsonRpcMethod("TestExecution.filter")
    fun testExecutionFilter(
        predicates: TestExecutionFilter
    ): List<TestExecution>

    @JsonRpcMethod("TestExecution.update")
    fun testExecutionUpdate(
        @JsonRpcParam("case_run_id") runId: Int,
        @JsonRpcParam("values") values: TestExecutionValues
    ): TestExecution

    @JsonRpcMethod("TestExecution.add_comment")
    fun testExecutionAddComment(
        @JsonRpcParam("execution_id") runId: Int,
        @JsonRpcParam("comment") comment: String
    )

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

    @JsonRpcMethod("TestRun.update")
    fun testRunUpdate(
        @JsonRpcParam("run_id") runId: Int,
        @JsonRpcParam("values") values: TestRun
    ): TestRun

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestCase(
        @get:JsonProperty("case_id") var id: Int?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestRun(
        @get:JsonProperty("run_id") var id: Int?,
        @get:JsonProperty("stop_date") var stopDate: String?
    ) {
        constructor(): this(null, null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestPlanType(
        var id: Int?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class Classification(
        var id: Int?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestCaseFilter(
        @get:JsonProperty("pk") val caseId: Int?,
        @get:JsonProperty("plan") val planId: Int?
    ) {
        constructor(): this(null, null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestExecution(
        @get:JsonProperty("case_run_id") var caseRunId: Int? = 0
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestExecutionValues(
        var status: TestExecutionStatus?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class ProductBuild(
        @get:JsonProperty("build_id") var id: Int?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class ProductBuildFilter(
        var product: Int?,
        var name: String?
    ) {
        constructor(): this(null, null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestRunFilter(
        @get:JsonProperty("build") var buildId: Int?,
        var manager: String?,
        @get:JsonProperty("plan") var planId: Int?,
        var summary: String?
    ) {
        constructor(): this(null, null, null, null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class ProductFilter(
        var name: String?,
        @get:JsonProperty("classification_id") var classificationId: Int?
    ) {
        constructor(): this(null, null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class Product(
        var id: Int?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class ClassificationFilter {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class ProductVersionFilter(
        @get:JsonProperty("product") var productId: Int?,
        var value: String?
    ) {
        constructor(): this(null, null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class ProductVersion(
        var id: Int?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestPlanFilter(
        var name: String?,
        @get:JsonProperty("product") var productId: Int?
    ) {
        constructor(): this(null, null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestPlan(
        @get:JsonProperty("plan_id") var id: Int?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    open class TestPlanTypeFilter(
        var name: String?
    ) {
        constructor(): this(null)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class TestPlanCreateTemplate(
        @get:JsonProperty("product") var productId: Int,
        @get:JsonProperty("type") var typeId: Int,
        @get:JsonProperty("default_product_version") var defaultProductVersion: Int,
        @get:JsonProperty("product_version") var productVersion: Int,
        var text: String,
        @get:JsonProperty("is_active") var isActive: Boolean,
        name: String
    ): TestPlanTypeFilter(name)

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    class TestExecutionFilter (
        @get:JsonProperty("run_id") var runId: Int?,
        @get:JsonProperty("case_id") var caseId: Int?
    ) {
        constructor(): this(null, null)
    }

    enum class TestExecutionStatus(@get:JsonValue val value: Int) {
        IDLE(1),
        PASS(4),
        FAIL(5);
    }
}

class KiwiService(
    login: String,
    password: String,
    baseUrl: String
) {
    private val client by lazy {
        val url = baseUrl
            .let {
                if (!it.endsWith("/")) {
                    "$it/"
                } else {
                    it
                }
            }
            .let { "${it}json-rpc/" }

        JsonRpcHttpClient(
            URL(url)
        )
    }

    val service: KiwiApi = ProxyUtil.createClientProxy<KiwiApi>(
        javaClass.classLoader,
        KiwiApi::class.java,
        client
    )

    init {
        val sessionId = service.authLogin(login, password)
        client.headers = mapOf("Cookie" to "sessionid=$sessionId")
    }

    fun close() {
        service.authLogout()
    }

    fun addTestCaseToPlan(planId: Int, caseId: Int) {
        val results = service.testCaseFilter(KiwiApi.TestCaseFilter(caseId, planId))

        if (results.isNotEmpty()) {
            service.testPlanAddCase(planId, caseId)
        }
    }

    fun addTestCaseToRunId(testRunId: Int, testCaseId: Int): KiwiApi.TestExecution {
        return service.testRunAddCase(testRunId, testCaseId)
    }

    fun getExecutions(
        runId: Int,
        testCaseId: Int
    ): List<KiwiApi.TestExecution> {
        return service.testExecutionFilter(
            KiwiApi.TestExecutionFilter(
                runId = runId,
                caseId = testCaseId
            )
        )
    }

    fun updateTestExecution(
        runId: Int,
        status: KiwiApi.TestExecutionStatus,
        comment: String
    ) {
        val values = KiwiApi.TestExecutionValues(status)
        service.testExecutionUpdate(runId, values)
        if (comment.isNotEmpty()) {
            service.testExecutionAddComment(runId, comment)
        }
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
        val product = service.productFilter(KiwiApi.ProductFilter(name, null))
        return if (product.isEmpty()) {
            null
        } else {
            product[0].id!!
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
            testPlans[0].id!!
        }
    }

    fun createNewTP(productId: Int, name: String, versionId: Int): KiwiApi.TestPlan {
        val planTypeFilter = KiwiApi.TestPlanTypeFilter(name = "Acceptance")
        val typeId = service.planTypeFilter(planTypeFilter)[0].id!!

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

    fun completeRun(runId: Int) {
        service.testRunUpdate(runId, KiwiApi.TestRun(
            id = null,
            stopDate = OffsetDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ))
    }
}