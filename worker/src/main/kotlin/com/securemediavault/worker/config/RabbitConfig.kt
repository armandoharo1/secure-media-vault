import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    @Bean
    fun exchange(): TopicExchange = TopicExchange("media.file.uploaded")

    @Bean
    fun queue(): Queue = Queue("file-uploaded-queue")

    @Bean
    fun binding(queue: Queue, exchange: TopicExchange): Binding {
        return BindingBuilder.bind(queue).to(exchange).with("file-uploaded-queue")
    }
}
