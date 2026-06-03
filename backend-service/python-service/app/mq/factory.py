from app.core.config import Settings
from app.mq.base import MessageBroker
from app.mq.logging_broker import LoggingMessageBroker
from app.mq.rabbitmq_broker import RabbitMqBroker


def create_message_broker(settings: Settings) -> MessageBroker:
    if not settings.mq_enabled:
        return LoggingMessageBroker()

    if settings.mq_broker == "rabbitmq":
        return RabbitMqBroker(settings)

    return LoggingMessageBroker()
