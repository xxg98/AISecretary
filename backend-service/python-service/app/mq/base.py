from abc import ABC, abstractmethod

from app.mq.message import MqMessage


class MessageBroker(ABC):
    @abstractmethod
    async def publish(self, message: MqMessage) -> None:
        raise NotImplementedError
