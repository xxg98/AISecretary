import json
from urllib.parse import urlparse

import pika

from app.core.config import Settings
from app.mq.base import MessageBroker
from app.mq.message import MqMessage


class RabbitMqBroker(MessageBroker):
    def __init__(self, settings: Settings) -> None:
        self._settings = settings

    async def publish(self, message: MqMessage) -> None:
        parsed_url = urlparse(self._settings.mq_url)
        parameters = pika.URLParameters(parsed_url.geturl())
        connection = pika.BlockingConnection(parameters)
        try:
            channel = connection.channel()
            channel.exchange_declare(
                exchange=self._settings.mq_exchange,
                exchange_type="topic",
                durable=True,
            )
            channel.basic_publish(
                exchange=self._settings.mq_exchange,
                routing_key=message.routing_key,
                body=json.dumps(message.payload, ensure_ascii=False).encode("utf-8"),
                properties=pika.BasicProperties(
                    content_type="application/json",
                    delivery_mode=2,
                    correlation_id=message.trace_id,
                ),
            )
        finally:
            connection.close()
