package ai.mlflow.logging

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Ports
import kotlinx.coroutines.runBlocking
import org.example.ai.mlflow.*
import org.example.ai.model.*
import org.junit.jupiter.api.*
import org.mlflow.api.proto.Service
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.containers.wait.strategy.Wait
import org.mlflow.tracking.MlflowClient
import kotlin.test.assertEquals

private const val MLFLOW_HOST = "0.0.0.0"
private const val MLFLOW_PORT = 8050

private const val MLFLOW_VERSION = "2.20.2"

@Testcontainers
class TestLogging {
    private lateinit var experimentId: String

    @BeforeEach
    fun setupTestData() {
        runBlocking {
            experimentId = createTestExperiment()
        }
    }

    private fun createTestExperiment(): String {
        val experimentName = "Test Experiment ${System.currentTimeMillis()}"
        return mlflowClient.createExperiment(experimentName)
    }

    @Test
    fun testCreateExperiment() {
        runBlocking {
            val experimentId = mlflowClient.createExperiment("Test Experiment")
            val experiment = mlflowClient.getExperiment(experimentId)
            assertEquals(experimentId, experiment.experimentId)

            mlflowClient.deleteExperiment(experimentId)
            assertEquals("deleted", mlflowClient.getExperiment(experimentId).lifecycleStage)
        }
    }

    @Test
    fun testCreateRun() {
        runBlocking {
            val run = mlflowClient.createRun(experimentId)
            val runId = run.runId

            mlflowClient.setTerminated(runId, Service.RunStatus.FINISHED)

            val loggedRun = mlflowClient.getRun(runId)
            assertEquals(runId, loggedRun.info.runId)
            assertEquals(experimentId, loggedRun.info.experimentId)
            assertEquals(Service.RunStatus.FINISHED, loggedRun.info.status)

            mlflowClient.deleteRun(runId)
            assertEquals("deleted", mlflowClient.getRun(runId).info.lifecycleStage)
        }
    }

    @Test
    fun testLogModel() {
        runBlocking {
            val run = mlflowClient.createRun(experimentId)
            val runId = run.runId

            val modelData = ModelData(
                runId = runId,
                artifactPath = "model",
                flavors = Flavors(
                    openai = OpenAI(
                        openaiVersion = "1.60.2",
                        data = "model.yaml",
                        code = "print(0)"
                    )
                ),
                signature = Signature(
                    inputs = "[{\"type\": \"string\", \"required\": true}]",
                    outputs = "[{\"type\": \"string\", \"required\": true}]",
                    params = null
                )
            )

            logModel(runId, modelJson = createModelJson(modelData), "http://$MLFLOW_HOST:$MLFLOW_PORT")

            val artifactUri = run.artifactUri
            assertEquals("mlflow-artifacts:/${experimentId}/${runId}/artifacts", artifactUri)

            // TODO: implement logging model data to artifacts

//            logModelData(artifactUri, modelData)
//
//            logBatch(
//                runId = runId,
//                metrics = listOf(
//                    Metric(
//                        "metric",
//                        1.0
//                    )
//                )
//            )
//
//            setTag(
//                runId,
//                "mlflow.loggedArtifacts",
//                "[{\"path\": \"eval_results_table.json\", \"type\": \"table\"}]"
//            )
//
//            updateRun(runId, RunStatus.FINISHED)
        }
    }

    companion object {
        private lateinit var mlflowClient: MlflowClient

        @Container
        val mlflowContainer: GenericContainer<*> = GenericContainer("ghcr.io/mlflow/mlflow:v$MLFLOW_VERSION")
            .withExposedPorts(MLFLOW_PORT)
            .withCommand("mlflow server --host 0.0.0.0 --port $MLFLOW_PORT")
            .waitingFor(Wait.forListeningPort())
            .withCreateContainerCmdModifier { cmd ->
                cmd.withHostConfig(
                    HostConfig().withPortBindings(
                        Ports.Binding.bindPort(MLFLOW_PORT).let { binding ->
                            Ports().apply { bindings[ExposedPort.tcp(MLFLOW_PORT)] = arrayOf(binding) }
                        }
                    )
                )
            }

        @BeforeAll
        @JvmStatic
        fun setup() {
            mlflowContainer.start()
            mlflowClient = MlflowClient("http://${MLFLOW_HOST}:${MLFLOW_PORT}")
        }

        @AfterAll
        @JvmStatic
        fun teardown() {
            mlflowContainer.stop()
            mlflowClient.close()
        }
    }
}