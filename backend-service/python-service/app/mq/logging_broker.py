import json

from loguru import logger

from app.mq.base import MessageBroker
from app.mq.message import MqMessage


class LoggingMessageBroker(MessageBroker):
    async def publish(self, message: MqMessage) -> None:
        logger.info(
            "MQ is disabled, message logged only: routing_key={}, trace_id={}, payload={}",
            message.routing_key,
            message.trace_id,
            json.dumps(message.payload, ensure_ascii=False),
        )
