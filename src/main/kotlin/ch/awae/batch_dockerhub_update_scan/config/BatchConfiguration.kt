package ch.awae.batch_dockerhub_update_scan.config

import ch.awae.batch_dockerhub_update_scan.model.CurrentEntryState
import ch.awae.batch_dockerhub_update_scan.model.UpdatedEntryState
import ch.awae.batch_dockerhub_update_scan.processor.UpdateProcessor
import ch.awae.batch_dockerhub_update_scan.reader.EntryStateReader
import ch.awae.batch_dockerhub_update_scan.writer.UpdatedStateWriter
import ch.awae.spring.batch.AwaeBatchConfigurationBase
import ch.awae.spring.batch.AwaeSkipPolicy
import ch.awae.spring.batch.FailJobWithSkipsListener
import org.springframework.batch.core.*
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchConfiguration : AwaeBatchConfigurationBase() {
    @Bean
    fun job(
        jobRepo: JobRepository,
        updateStep: Step,
    ) = JobBuilder("updateJob", jobRepo)
        .start(updateStep)
        .listener(FailJobWithSkipsListener())
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
        .faultTolerant()
        .skipPolicy(AwaeSkipPolicy())
        .build()

}