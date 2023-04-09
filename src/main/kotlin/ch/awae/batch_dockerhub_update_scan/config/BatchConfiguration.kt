package ch.awae.batch_dockerhub_update_scan.config

import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import ch.awae.batch_dockerhub_update_scan.model.UpdatedEntryState
import ch.awae.batch_dockerhub_update_scan.processor.UpdateProcessor
import ch.awae.batch_dockerhub_update_scan.reader.EntryStateReader
import ch.awae.batch_dockerhub_update_scan.writer.UpdatedStateWriter
import org.springframework.batch.core.*
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.util.*

@Configuration
class BatchConfiguration : DefaultBatchConfiguration() {



    @Bean
    fun job(
        jobRepo: JobRepository,
        updateStep: Step,
    ) = JobBuilder("updateJob", jobRepo)
        .start(updateStep)
        .build()

    @Bean
    fun updateStep(
        jobRepo: JobRepository,
        transactionManager: PlatformTransactionManager,
        reader: EntryStateReader,
        processor: UpdateProcessor,
        writer: UpdatedStateWriter,
    ) = StepBuilder("updateStep", jobRepo)
        .chunk<CurrentEntryState, UpdatedEntryState>(1, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build()

    @Bean
    fun jobRunner(jobLauncher: JobLauncher, jobExplorer: JobExplorer, jobRepository: JobRepository) =
        JobLauncherApplicationRunner(jobLauncher, jobExplorer, jobRepository)

    /**
     * inject a UUID as a batch parameter ("uuid") to enable arbitrary re-runs
     */
    override fun jobLauncher(): JobLauncher {
        val launcher = object : TaskExecutorJobLauncher() {
            override fun run(job: Job, jobParameters: JobParameters): JobExecution {

                val map = jobParameters.parameters.toMutableMap()
                map["uuid"] = JobParameter(UUID.randomUUID().toString(), String::class.java)

                return super.run(job, JobParameters(map))
            }
        }
        launcher.setJobRepository(jobRepository())
        launcher.setTaskExecutor(taskExecutor)
        return launcher
    }

}